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
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GraphQLCustomizeExceptionHandler implements DataFetcherExceptionHandler {

  private final DefaultDataFetcherExceptionHandler defaultHandler =
      new DefaultDataFetcherExceptionHandler();

  @Override
  public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
      DataFetcherExceptionHandlerParameters handlerParameters) {
    Throwable exception = handlerParameters.getException();
    if (exception instanceof InvalidAuthenticationException
        || exception instanceof AuthenticationException) {
      return completed(
          TypedGraphQLError.newBuilder()
              .errorType(ErrorType.UNAUTHENTICATED)
              .message(messageOf(exception, "authentication required"))
              .path(handlerParameters.getPath())
              .build());
    } else if (exception instanceof ConstraintViolationException) {
      List<FieldErrorResource> errors =
          toFieldErrors(((ConstraintViolationException) exception).getConstraintViolations());
      return completed(
          TypedGraphQLError.newBadRequestBuilder()
              .message(messageOf(exception, "invalid input"))
              .path(handlerParameters.getPath())
              .extensions(errorsToMap(errors))
              .build());
    } else if (exception instanceof ResourceNotFoundException) {
      return completed(
          TypedGraphQLError.newNotFoundBuilder()
              .message(messageOf(exception, "resource not found"))
              .path(handlerParameters.getPath())
              .build());
    } else if (exception instanceof NoAuthorizationException) {
      return completed(
          TypedGraphQLError.newPermissionDeniedBuilder()
              .message(messageOf(exception, "permission denied"))
              .path(handlerParameters.getPath())
              .build());
    } else {
      return defaultHandler.handleException(handlerParameters);
    }
  }

  public static Error getErrorsAsData(ConstraintViolationException cve) {
    List<FieldErrorResource> errors = toFieldErrors(cve.getConstraintViolations());
    Map<String, List<String>> errorMap = new HashMap<>();
    for (FieldErrorResource fieldErrorResource : errors) {
      if (!errorMap.containsKey(fieldErrorResource.getField())) {
        errorMap.put(fieldErrorResource.getField(), new ArrayList<>());
      }
      errorMap.get(fieldErrorResource.getField()).add(fieldErrorResource.getMessage());
    }
    List<ErrorItem> errorItems =
        errorMap.entrySet().stream()
            .map(kv -> ErrorItem.newBuilder().key(kv.getKey()).value(kv.getValue()).build())
            .collect(Collectors.toList());
    return Error.newBuilder().message("BAD_REQUEST").errors(errorItems).build();
  }

  private static CompletableFuture<DataFetcherExceptionHandlerResult> completed(
      GraphQLError graphqlError) {
    return CompletableFuture.completedFuture(
        DataFetcherExceptionHandlerResult.newResult().error(graphqlError).build());
  }

  private static String messageOf(Throwable exception, String fallback) {
    return exception.getMessage() == null ? fallback : exception.getMessage();
  }

  private static List<FieldErrorResource> toFieldErrors(
      java.util.Set<ConstraintViolation<?>> violations) {
    List<FieldErrorResource> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : violations) {
      errors.add(
          new FieldErrorResource(
              violation.getRootBeanClass().getName(),
              getParam(violation.getPropertyPath().toString()),
              violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
              violation.getMessage()));
    }
    return errors;
  }

  private static String getParam(String s) {
    String[] splits = s.split("\\.");
    if (splits.length == 1) {
      return s;
    } else {
      return String.join(".", Arrays.copyOfRange(splits, 2, splits.length));
    }
  }

  private static Map<String, Object> errorsToMap(List<FieldErrorResource> errors) {
    Map<String, Object> json = new HashMap<>();
    for (FieldErrorResource fieldErrorResource : errors) {
      if (!json.containsKey(fieldErrorResource.getField())) {
        json.put(fieldErrorResource.getField(), new ArrayList<String>());
      }
      @SuppressWarnings("unchecked")
      List<String> messages = (List<String>) json.get(fieldErrorResource.getField());
      messages.add(fieldErrorResource.getMessage());
    }
    return json;
  }
}
