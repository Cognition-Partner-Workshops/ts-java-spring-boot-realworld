package io.spring.graphql.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;
  private DataFetcherExceptionHandlerParameters params;

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getPath()).thenReturn(ResultPath.rootPath());
    when(params.getSourceLocation()).thenReturn(new SourceLocation(1, 1));
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    ConstraintViolationException exception = new ConstraintViolationException(new HashSet<ConstraintViolation<?>>());
    when(params.getException()).thenReturn(exception);

    CompletableFuture<DataFetcherExceptionHandlerResult> result = handler.handleException(params);

    assertThat(result, is(notNullValue()));
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    when(params.getException()).thenReturn(exception);

    CompletableFuture<DataFetcherExceptionHandlerResult> result = handler.handleException(params);

    assertThat(result, is(notNullValue()));
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void should_handle_no_authorization_exception() {
    NoAuthorizationException exception = new NoAuthorizationException();
    when(params.getException()).thenReturn(exception);

    CompletableFuture<DataFetcherExceptionHandlerResult> result = handler.handleException(params);

    assertThat(result, is(notNullValue()));
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void should_handle_resource_not_found_exception() {
    ResourceNotFoundException exception = new ResourceNotFoundException();
    when(params.getException()).thenReturn(exception);

    CompletableFuture<DataFetcherExceptionHandlerResult> result = handler.handleException(params);

    assertThat(result, is(notNullValue()));
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void should_handle_authentication_exception() {
    AuthenticationException exception = new AuthenticationException();
    when(params.getException()).thenReturn(exception);

    CompletableFuture<DataFetcherExceptionHandlerResult> result = handler.handleException(params);

    assertThat(result, is(notNullValue()));
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void should_handle_generic_exception() {
    RuntimeException exception = new RuntimeException("Generic error");
    when(params.getException()).thenReturn(exception);

    CompletableFuture<DataFetcherExceptionHandlerResult> result = handler.handleException(params);

    assertThat(result, is(notNullValue()));
    assertThat(result.isDone(), is(true));
  }

  @Test
  public void should_get_errors_as_data_from_constraint_violation() {
    ConstraintViolationException exception = new ConstraintViolationException(new HashSet<ConstraintViolation<?>>());
    
    Object result = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertThat(result, is(notNullValue()));
  }
}
