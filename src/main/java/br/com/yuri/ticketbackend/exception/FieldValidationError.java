package br.com.yuri.ticketbackend.exception;

public record FieldValidationError(
        String field,
        String message
) {
}