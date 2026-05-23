package ru.sber.dealservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.dealservice.dto.CalculationRequest;
import ru.sber.dealservice.dto.CalculationResponse;
import ru.sber.dealservice.entity.Client;
import ru.sber.dealservice.entity.Deal;
import ru.sber.dealservice.exception.DealNotFoundException;
import ru.sber.dealservice.exception.InvalidCalculationDateException;
import ru.sber.dealservice.integration.kafka.RiskServiceGateway;
import ru.sber.dealservice.repository.ClientRepository;
import ru.sber.dealservice.repository.DealRepository;
import ru.sber.dealservice.service.impl.DefaultDealCalculationService;
import ru.sber.proto.CbrCurrencyServiceGrpc;
import ru.sber.proto.ConvertRequest;
import ru.sber.proto.ConvertResponse;
import ru.sber.proto.ExchangeRateInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultDealCalculationServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RiskServiceGateway riskServiceGateway;

    @Mock
    private CbrCurrencyServiceGrpc.CbrCurrencyServiceBlockingStub currencyStub;

    @InjectMocks
    private DefaultDealCalculationService calculationService;

    private Deal testDeal;
    private Client testClient;
    private ConvertResponse mockConvertResponse;

    @BeforeEach
    void setUp() {
        testDeal = Deal.builder()
                .dealNumber("CRD-TEST-001")
                .loanAmountRub(new BigDecimal("120000"))
                .interestRate(BigDecimal.ZERO)
                .issueDate(LocalDate.of(2024, 1, 1))
                .loanTermMonths(12)
                .repaymentMethod("Дифференцированный")
                .build();

        testClient = Client.builder()
                .id("CLT-TEST-001")
                .fullName("Иванов Иван Иванович")
                .inn("123456789012")
                .build();

        ExchangeRateInfo rateInfo = ExchangeRateInfo.newBuilder()
                .setRateUsdRub("90.0")
                .setRateEurRub("100.0")
                .setDate("2024-07-01")
                .build();
        mockConvertResponse = ConvertResponse.newBuilder()
                .setExchangeRateInfo(rateInfo)
                .build();
    }

    @Test
    void calculate_ShouldReturnRubResponse_WhenCurrencyIsRub() {
        when(dealRepository.findByDealNumber("CRD-TEST-001")).thenReturn(Optional.of(testDeal));
        when(clientRepository.findByDealId("CRD-TEST-001")).thenReturn(Optional.of(testClient));
        when(riskServiceGateway.getCreditHistory("123456789012")).thenReturn("Хорошая");
        when(currencyStub.convert(any(ConvertRequest.class))).thenReturn(mockConvertResponse);

        CalculationRequest request = new CalculationRequest(
                "CRD-TEST-001", LocalDate.of(2024, 7, 1), "RUB");

        CalculationResponse response = calculationService.calculate(request);

        assertThat(response.success()).isTrue();
        assertThat(response.data().borrowerFullName()).isEqualTo("Иванов Иван Иванович");
        assertThat(response.data().creditAmountCurrency()).isEqualTo("RUB");
        // 120000 * (12 - 6) / 12 = 60000 on date; 18 months elapsed > 12 months term → 0 in one year
        assertThat(response.data().creditAmount()).isEqualByComparingTo(new BigDecimal("120000"));
        assertThat(response.data().balanceOnDate()).isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(response.data().balanceInOneYear()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.data().creditHistory()).isEqualTo("Хорошая");
    }

    @Test
    void calculate_ShouldConvertToUsd_WhenCurrencyIsUsd() {
        when(dealRepository.findByDealNumber("CRD-TEST-001")).thenReturn(Optional.of(testDeal));
        when(clientRepository.findByDealId("CRD-TEST-001")).thenReturn(Optional.of(testClient));
        when(riskServiceGateway.getCreditHistory("123456789012")).thenReturn("Хорошая");
        when(currencyStub.convert(any(ConvertRequest.class))).thenReturn(mockConvertResponse);

        CalculationRequest request = new CalculationRequest(
                "CRD-TEST-001", LocalDate.of(2024, 7, 1), "USD");

        CalculationResponse response = calculationService.calculate(request);

        assertThat(response.success()).isTrue();
        assertThat(response.data().creditAmountCurrency()).isEqualTo("USD");
        // 120000 / 90 = 1333.33
        assertThat(response.data().creditAmount()).isEqualByComparingTo(new BigDecimal("1333.33"));
        // 60000 / 90 = 666.67
        assertThat(response.data().balanceOnDate()).isEqualByComparingTo(new BigDecimal("666.67"));
        assertThat(response.data().balanceInOneYear()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculate_ShouldThrowDealNotFoundException_WhenDealNotFound() {
        when(dealRepository.findByDealNumber("UNKNOWN")).thenReturn(Optional.empty());

        CalculationRequest request = new CalculationRequest(
                "UNKNOWN", LocalDate.of(2024, 7, 1), "RUB");

        assertThatThrownBy(() -> calculationService.calculate(request))
                .isInstanceOf(DealNotFoundException.class)
                .hasMessageContaining("Deal not found: UNKNOWN");
    }

    @Test
    void calculate_ShouldThrowInvalidCalculationDateException_WhenDateBeforeIssueDate() {
        when(dealRepository.findByDealNumber("CRD-TEST-001")).thenReturn(Optional.of(testDeal));

        CalculationRequest request = new CalculationRequest(
                "CRD-TEST-001", LocalDate.of(2023, 12, 31), "RUB");

        assertThatThrownBy(() -> calculationService.calculate(request))
                .isInstanceOf(InvalidCalculationDateException.class)
                .hasMessageContaining("calculation_date");
    }

    @Test
    void calculate_ShouldThrowDealNotFoundException_WhenClientNotFound() {
        when(dealRepository.findByDealNumber("CRD-TEST-001")).thenReturn(Optional.of(testDeal));
        when(clientRepository.findByDealId("CRD-TEST-001")).thenReturn(Optional.empty());

        CalculationRequest request = new CalculationRequest(
                "CRD-TEST-001", LocalDate.of(2024, 7, 1), "RUB");

        assertThatThrownBy(() -> calculationService.calculate(request))
                .isInstanceOf(DealNotFoundException.class);
    }
}