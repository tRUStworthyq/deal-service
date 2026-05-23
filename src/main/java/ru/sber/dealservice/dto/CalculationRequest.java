package ru.sber.dealservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CalculationRequest(
        @JsonProperty("deal_id")
        @NotBlank(message = "deal_id must not be blank")
        String dealId,

        @JsonProperty("calculation_date")
        @NotNull(message = "calculation_date must not be null")
        LocalDate calculationDate,

        @JsonProperty("target_currency")
        @NotBlank(message = "target_currency must not be blank")
        @Pattern(regexp = "RUB|USD|EUR", message = "target_currency must be RUB, USD or EUR")
        String targetCurrency
) {
}