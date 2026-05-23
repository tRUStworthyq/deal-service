package ru.sber.dealservice.exception;

public class DealNotFoundException extends RuntimeException {
    public DealNotFoundException(String dealId) {
        super("Deal not found: " + dealId);
    }
}