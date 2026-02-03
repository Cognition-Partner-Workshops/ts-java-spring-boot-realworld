package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest webRequest;

  @BeforeEach
  public void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = mock(WebRequest.class);
  }

  @Test
  public void should_handle_invalid_request_exception() {
    Errors errors = mock(BindingResult.class);
    FieldError fieldError = new FieldError("object", "field", "error message");
    when(errors.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
    
    InvalidRequestException exception = new InvalidRequestException(errors);
    
    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);
    
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    
    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, webRequest);
    
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
  }

  @Test
  public void should_handle_constraint_violation_exception_with_empty_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    
    ErrorResource response = handler.handleConstraintViolation(exception, webRequest);
    
    assertThat(response, is(notNullValue()));
    assertThat(response.getFieldErrors().size(), is(0));
  }

  @Test
  public void should_handle_invalid_request_with_multiple_field_errors() {
    Errors errors = mock(BindingResult.class);
    FieldError fieldError1 = new FieldError("object", "field1", "error message 1");
    FieldError fieldError2 = new FieldError("object", "field2", "error message 2");
    when(errors.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));
    
    InvalidRequestException exception = new InvalidRequestException(errors);
    
    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);
    
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
  }

  @Test
  public void should_handle_invalid_request_with_empty_field_errors() {
    Errors errors = mock(BindingResult.class);
    when(errors.getFieldErrors()).thenReturn(Arrays.asList());
    
    InvalidRequestException exception = new InvalidRequestException(errors);
    
    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);
    
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatusCode(), is(HttpStatus.UNPROCESSABLE_ENTITY));
  }
}
