package ru.sber.dealservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.dealservice.dto.*;
import ru.sber.dealservice.entity.Client;
import ru.sber.dealservice.entity.Deal;
import ru.sber.dealservice.exception.DealNotFoundException;
import ru.sber.dealservice.exception.InvalidCalculationDateException;
import ru.sber.dealservice.integration.kafka.RiskServiceGateway;
import ru.sber.dealservice.repository.ClientRepository;
import ru.sber.dealservice.repository.DealRepository;
import ru.sber.dealservice.service.DealCalculationService;
import ru.sber.proto.CbrCurrencyServiceGrpc;
import ru.sber.proto.ConvertRequest;
import ru.sber.proto.ConvertResponse;
import ru.sber.proto.ExchangeRateInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultDealCalculationService implements DealCalculationService {

    private static final String ANNUITY = "Аннуитетный";

    private final DealRepository dealRepository;
    private final ClientRepository clientRepository;
    private final RiskServiceGateway riskServiceGateway;
    private final CbrCurrencyServiceGrpc.CbrCurrencyServiceBlockingStub currencyStub;

    @Override
    @Transactional(readOnly = true)
    public CalculationResponse calculate(CalculationRequest request) {
        log.info("Calculating deal: dealId={}, date={}, currency={}",
                request.dealId(), request.calculationDate(), request.targetCurrency());

        Deal deal = dealRepository.findByDealNumber(request.dealId())
                .orElseThrow(() -> new DealNotFoundException(request.dealId()));

        if (request.calculationDate().isBefore(deal.getIssueDate())) {
            throw new InvalidCalculationDateException(
                    "calculation_date (%s) cannot be earlier than issue_date (%s)"
                            .formatted(request.calculationDate(), deal.getIssueDate()));
        }

        Client client = clientRepository.findByDealId(request.dealId())
                .orElseThrow(() -> new DealNotFoundException(request.dealId()));

        long monthsOnDate = ChronoUnit.MONTHS.between(deal.getIssueDate(), request.calculationDate());
        long monthsInOneYear = ChronoUnit.MONTHS.between(deal.getIssueDate(),
                request.calculationDate().plusDays(365));

        BigDecimal balanceOnDateRub = calculateBalance(deal, monthsOnDate);
        BigDecimal balanceInOneYearRub = calculateBalance(deal, monthsInOneYear);

        String creditHistory = riskServiceGateway.getCreditHistory(client.getInn());

        // If target is RUB, call currency-service with USD just to obtain exchange_rate_info.
        String effectiveCurrency = "RUB".equals(request.targetCurrency()) ? "USD" : request.targetCurrency();
        ConvertResponse conversion = currencyStub.convert(
                ConvertRequest.newBuilder()
                        .setAmount(deal.getLoanAmountRub().toPlainString())
                        .setTargetCurrency(effectiveCurrency)
                        .build());

        ExchangeRateInfo rateInfo = conversion.getExchangeRateInfo();
        ExchangeRateInfoDto exchangeRateInfoDto = new ExchangeRateInfoDto(
                new BigDecimal(rateInfo.getRateUsdRub()),
                new BigDecimal(rateInfo.getRateEurRub()),
                rateInfo.getDate());

        BigDecimal creditAmount;
        BigDecimal balanceOnDate;
        BigDecimal balanceInOneYear;
        String currency = request.targetCurrency();

        if ("RUB".equals(currency)) {
            creditAmount = deal.getLoanAmountRub();
            balanceOnDate = balanceOnDateRub;
            balanceInOneYear = balanceInOneYearRub;
        } else {
            BigDecimal rate = "USD".equals(currency)
                    ? new BigDecimal(rateInfo.getRateUsdRub())
                    : new BigDecimal(rateInfo.getRateEurRub());
            creditAmount = deal.getLoanAmountRub().divide(rate, 2, RoundingMode.HALF_EVEN);
            balanceOnDate = balanceOnDateRub.divide(rate, 2, RoundingMode.HALF_EVEN);
            balanceInOneYear = balanceInOneYearRub.divide(rate, 2, RoundingMode.HALF_EVEN);
        }

        DealData data = new DealData(
                client.getFullName(),
                creditAmount,
                currency,
                balanceOnDate,
                currency,
                balanceInOneYear,
                currency,
                deal.getRepaymentMethod(),
                creditHistory,
                exchangeRateInfoDto);

        return CalculationResponse.ok(data);
    }

    private BigDecimal calculateBalance(Deal deal, long monthsElapsed) {
        int n = deal.getLoanTermMonths();
        if (monthsElapsed >= n) {
            return BigDecimal.ZERO;
        }
        BigDecimal principal = deal.getLoanAmountRub();
        if (ANNUITY.equals(deal.getRepaymentMethod())) {
            return calcAnnuityBalance(principal, deal.getInterestRate(), n, monthsElapsed);
        }
        return calcDifferentiatedBalance(principal, n, monthsElapsed);
    }

    private BigDecimal calcAnnuityBalance(BigDecimal principal, BigDecimal annualRatePct, int n, long k) {
        double r = annualRatePct.doubleValue() / 100.0 / 12.0;
        if (r == 0) {
            return principal.multiply(BigDecimal.valueOf(n - k))
                    .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_EVEN);
        }
        double powN = Math.pow(1 + r, n);
        double powK = Math.pow(1 + r, k);
        double balance = principal.doubleValue() * (powN - powK) / (powN - 1);
        return BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_EVEN);
    }

    private BigDecimal calcDifferentiatedBalance(BigDecimal principal, int n, long k) {
        return principal.multiply(BigDecimal.valueOf(n - k))
                .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_EVEN);
    }
}