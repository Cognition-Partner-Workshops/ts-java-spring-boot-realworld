package io.spring.api.exception;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import io.spring.api.shared.ApiResponse;
import io.spring.api.shared.ExceptionUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for REST API. Uses shared ExceptionUtils for consistent error handling
 * across REST and GraphQL APIs.
 */
@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({InvalidRequestException.class})
  public ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
    InvalidRequestException ire = (InvalidRequestException) e;

    List<FieldErrorResource> errorResources =
        ire.getErrors().getFieldErrors().stream()
            .map(
                fieldError ->
                    new FieldErrorResource(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getCode(),
                        fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

    ErrorResource error = new ErrorResource(errorResources);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return handleExceptionInternal(e, error, headers, UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler(InvalidAuthenticationException.class)
  public ResponseEntity<Object> handleInvalidAuthentication(
      InvalidAuthenticationException e, WebRequest request) {
    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(ApiResponse.wrap("message", e.getMessage()));
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    List<FieldErrorResource> errorResources =
        e.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new FieldErrorResource(
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getCode(),
                        fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(new ErrorResource(errorResources));
  }

  @ExceptionHandler({ConstraintViolationException.class})
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ResponseBody
  public ErrorResource handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    List<FieldErrorResource> errors = ExceptionUtils.extractFieldErrors(ex);
    return new ErrorResource(errors);
  }
}
