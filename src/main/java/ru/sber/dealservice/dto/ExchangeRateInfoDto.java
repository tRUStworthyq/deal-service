package ru.sber.dealservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ExchangeRateInfoDto(
        @JsonProperty("rate_usd_rub") BigDecimal rateUsdRub,
        @JsonProperty("rate_eur_rub") BigDecimal rateEurRub,
        @JsonProperty("date")         String date
) {
}