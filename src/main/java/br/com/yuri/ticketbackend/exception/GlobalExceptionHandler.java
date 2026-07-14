package br.com.yuri.ticketbackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn(
                "Data integrity violation while processing request {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "The request conflicts with the current database state",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Unexpected error while processing request {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred",
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<FieldValidationError> fieldErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError -> new FieldValidationError(
                                fieldError.getField(),
                                getFieldErrorMessage(
                                        fieldError.getDefaultMessage()
                                )
                        ))
                        .toList();

        ApiErrorResponse response = createResponse(
                status,
                "Request validation failed",
                getRequestPath(request),
                fieldErrors
        );

        return super.handleExceptionInternal(
                exception,
                response,
                headers,
                status,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ApiErrorResponse response = createResponse(
                status,
                "Request body is invalid or malformed",
                getRequestPath(request),
                List.of()
        );

        return super.handleExceptionInternal(
                exception,
                response,
                headers,
                status,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ApiErrorResponse response = createResponse(
                status,
                "Resource not found",
                getRequestPath(request),
                List.of()
        );

        return super.handleExceptionInternal(
                exception,
                response,
                headers,
                status,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String message = "HTTP method "
                + exception.getMethod()
                + " is not supported for this endpoint";

        ApiErrorResponse response = createResponse(
                status,
                message,
                getRequestPath(request),
                List.of()
        );

        return super.handleExceptionInternal(
                exception,
                response,
                headers,
                status,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        if (body instanceof ApiErrorResponse) {
            return super.handleExceptionInternal(
                    exception,
                    body,
                    headers,
                    status,
                    request
            );
        }

        ApiErrorResponse response = createResponse(
                status,
                getFrameworkErrorMessage(body, status),
                getRequestPath(request),
                List.of()
        );

        return super.handleExceptionInternal(
                exception,
                response,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = createResponse(
                status,
                getMessageOrDefault(message, status.getReasonPhrase()),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private ApiErrorResponse createResponse(
            HttpStatusCode status,
            String message,
            String path,
            List<FieldValidationError> fieldErrors
    ) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                getReasonPhrase(status),
                getMessageOrDefault(
                        message,
                        getReasonPhrase(status)
                ),
                path,
                fieldErrors
        );
    }

    private String getFrameworkErrorMessage(
            Object body,
            HttpStatusCode status
    ) {
        if (body instanceof ProblemDetail problemDetail) {
            return getMessageOrDefault(
                    problemDetail.getDetail(),
                    getReasonPhrase(status)
            );
        }

        if (body instanceof String message) {
            return getMessageOrDefault(
                    message,
                    getReasonPhrase(status)
            );
        }

        return getReasonPhrase(status);
    }

    private String getReasonPhrase(HttpStatusCode status) {
        HttpStatus resolvedStatus =
                HttpStatus.resolve(status.value());

        if (resolvedStatus == null) {
            return "HTTP " + status.value();
        }

        return resolvedStatus.getReasonPhrase();
    }

    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest
                    .getRequest()
                    .getRequestURI();
        }

        return "";
    }

    private String getFieldErrorMessage(String message) {
        return getMessageOrDefault(
                message,
                "Invalid value"
        );
    }

    private String getMessageOrDefault(
            String message,
            String defaultMessage
    ) {
        if (message == null || message.isBlank()) {
            return defaultMessage;
        }

        return message;
    }
}