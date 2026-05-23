package ru.sber.dealservice.exception;

public class InvalidCalculationDateException extends RuntimeException {
    public InvalidCalculationDateException(String message) {
        super(message);
    }
}