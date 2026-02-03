package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.validation.Errors;

public class InvalidRequestExceptionTest {

  @Test
  public void should_create_exception_with_errors() {
    Errors errors = mock(Errors.class);
    InvalidRequestException exception = new InvalidRequestException(errors);
    
    assertThat(exception, is(notNullValue()));
    assertThat(exception.getErrors(), is(errors));
  }

  @Test
  public void should_have_empty_message() {
    Errors errors = mock(Errors.class);
    InvalidRequestException exception = new InvalidRequestException(errors);
    
    assertThat(exception.getMessage(), is(""));
  }

  @Test
  public void should_be_runtime_exception() {
    Errors errors = mock(Errors.class);
    InvalidRequestException exception = new InvalidRequestException(errors);
    
    assertThat(exception instanceof RuntimeException, is(true));
  }
}
