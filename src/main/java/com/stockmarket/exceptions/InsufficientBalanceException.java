package com.stockmarket.exception;

public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException() {
        super("Saldo insuficiente para realizar esta operação.");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}