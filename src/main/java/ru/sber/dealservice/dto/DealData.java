package ru.sber.dealservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record DealData(
        @JsonProperty("borrower_full_name")        String borrowerFullName,
        @JsonProperty("credit_amount")             BigDecimal creditAmount,
        @JsonProperty("credit_amount_currency")    String creditAmountCurrency,
        @JsonProperty("balance_on_date")           BigDecimal balanceOnDate,
        @JsonProperty("balance_on_date_currency")  String balanceOnDateCurrency,
        @JsonProperty("balance_in_one_year")       BigDecimal balanceInOneYear,
        @JsonProperty("balance_in_one_year_currency") String balanceInOneYearCurrency,
        @JsonProperty("repayment_method")          String repaymentMethod,
        @JsonProperty("credit_history")            String creditHistory,
        @JsonProperty("exchange_rate_info")        ExchangeRateInfoDto exchangeRateInfo
) {
}