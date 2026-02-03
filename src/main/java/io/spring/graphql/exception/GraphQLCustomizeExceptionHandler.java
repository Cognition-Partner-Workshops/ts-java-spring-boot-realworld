package io.spring.graphql.exception;

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import io.spring.api.exception.FieldErrorResource;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.api.shared.ExceptionUtils;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.springframework.stereotype.Component;

/**
 * GraphQL exception handler that converts application exceptions to GraphQL errors. Uses shared
 * ExceptionUtils for consistent error handling across REST and GraphQL APIs.
 */
@Component
public class GraphQLCustomizeExceptionHandler implements DataFetcherExceptionHandler {

  private final DefaultDataFetcherExceptionHandler defaultHandler =
      new DefaultDataFetcherExceptionHandler();

  @Override
  public DataFetcherExceptionHandlerResult onException(
      DataFetcherExceptionHandlerParameters handlerParameters) {
    if (handlerParameters.getException() instanceof InvalidAuthenticationException) {
      GraphQLError graphqlError =
          TypedGraphQLError.newBuilder()
              .errorType(ErrorType.UNAUTHENTICATED)
              .message(handlerParameters.getException().getMessage())
              .path(handlerParameters.getPath())
              .build();
      return DataFetcherExceptionHandlerResult.newResult().error(graphqlError).build();
    } else if (handlerParameters.getException() instanceof ConstraintViolationException) {
      ConstraintViolationException cve =
          (ConstraintViolationException) handlerParameters.getException();
      List<FieldErrorResource> errors = ExceptionUtils.extractFieldErrors(cve);
      GraphQLError graphqlError =
          TypedGraphQLError.newBadRequestBuilder()
              .message(handlerParameters.getException().getMessage())
              .path(handlerParameters.getPath())
              .extensions(ExceptionUtils.toGenericErrorMap(errors))
              .build();
      return DataFetcherExceptionHandlerResult.newResult().error(graphqlError).build();
    } else {
      return defaultHandler.onException(handlerParameters);
    }
  }

  public static Error getErrorsAsData(ConstraintViolationException cve) {
    List<FieldErrorResource> errors = ExceptionUtils.extractFieldErrors(cve);
    Map<String, List<String>> errorMap = ExceptionUtils.toErrorMap(errors);
    List<ErrorItem> errorItems =
        errorMap.entrySet().stream()
            .map(kv -> ErrorItem.newBuilder().key(kv.getKey()).value(kv.getValue()).build())
            .collect(Collectors.toList());
    return Error.newBuilder().message("BAD_REQUEST").errors(errorItems).build();
  }
}
