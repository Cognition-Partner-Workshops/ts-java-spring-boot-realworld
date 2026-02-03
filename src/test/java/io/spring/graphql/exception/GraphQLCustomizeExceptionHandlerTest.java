package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @Mock private DataFetcherExceptionHandlerParameters handlerParameters;

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    when(handlerParameters.getException()).thenReturn(exception);
    when(handlerParameters.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(handlerParameters);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("Some error");
    when(handlerParameters.getException()).thenReturn(exception);
    when(handlerParameters.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(handlerParameters);

    assertNotNull(result);
  }
}
