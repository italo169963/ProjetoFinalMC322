package com.stockmarket.exception;

public class InsufficientStockException extends Exception {
    public InsufficientStockException() {
        super("Quantidade insuficiente de ações para realizar esta operação.");
    }

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}