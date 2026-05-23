package ru.sber.dealservice.dto;

public record CalculationResponse(
        boolean success,
        DealData data
) {
    public static CalculationResponse ok(DealData data) {
        return new CalculationResponse(true, data);
    }
}