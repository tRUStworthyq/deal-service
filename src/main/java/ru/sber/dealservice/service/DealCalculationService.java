package ru.sber.dealservice.service;

import ru.sber.dealservice.dto.CalculationRequest;
import ru.sber.dealservice.dto.CalculationResponse;

public interface DealCalculationService {
    CalculationResponse calculate(CalculationRequest request);
}