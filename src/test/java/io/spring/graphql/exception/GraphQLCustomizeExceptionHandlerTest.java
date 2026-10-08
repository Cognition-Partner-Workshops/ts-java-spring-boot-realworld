package io.spring.graphql.exception;

import static org.assertj.core.api.Assertions.assertThat;

import graphql.GraphQLError;
import graphql.Scalars;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.graphql.types.Error;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphQLCustomizeExceptionHandlerTest {

  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  @Test
  void maps_authentication_failures_to_unauthenticated() {
    assertThat(errorTypeOf(new InvalidAuthenticationException())).isEqualTo("UNAUTHENTICATED");
    assertThat(errorTypeOf(new AuthenticationException())).isEqualTo("UNAUTHENTICATED");
  }

  @Test
  void maps_not_found_and_forbidden() {
    assertThat(errorTypeOf(new ResourceNotFoundException())).isEqualTo("NOT_FOUND");
    assertThat(errorTypeOf(new NoAuthorizationException())).isEqualTo("PERMISSION_DENIED");
  }

  @Test
  void maps_constraint_violations_to_bad_request_with_field_errors() {
    GraphQLError error = handle(violation());
    assertThat(error.getExtensions().get("errorType")).isEqualTo("BAD_REQUEST");
    assertThat((List<Object>) error.getExtensions().get("name"))
        .containsExactly("must not be blank");
    assertThat(error.getPath()).containsExactly("createUser");
  }

  @Test
  void delegates_unknown_exceptions_to_default_handler() {
    GraphQLError error = handle(new IllegalStateException("boom"));
    assertThat(error.getExtensions().get("errorType")).isEqualTo("INTERNAL");
    assertThat(error.getMessage()).contains("boom");
  }

  @Test
  void converts_constraint_violations_into_error_payload() {
    Error payload = GraphQLCustomizeExceptionHandler.getErrorsAsData(violation());
    assertThat(payload.getMessage()).isEqualTo("BAD_REQUEST");
    assertThat(payload.getErrors()).hasSize(1);
    assertThat(payload.getErrors().get(0).getKey()).isEqualTo("name");
    assertThat(payload.getErrors().get(0).getValue()).containsExactly("must not be blank");
  }

  private String errorTypeOf(Throwable throwable) {
    return String.valueOf(handle(throwable).getExtensions().get("errorType"));
  }

  private GraphQLError handle(Throwable throwable) {
    DataFetchingEnvironment env =
        DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
            .mergedField(MergedField.newMergedField(Field.newField("createUser").build()).build())
            .executionStepInfo(
                ExecutionStepInfo.newExecutionStepInfo()
                    .type(Scalars.GraphQLString)
                    .path(ResultPath.rootPath().segment("createUser"))
                    .build())
            .build();
    DataFetcherExceptionHandlerResult result =
        handler
            .handleException(
                DataFetcherExceptionHandlerParameters.newExceptionParameters()
                    .exception(throwable)
                    .dataFetchingEnvironment(env)
                    .build())
            .join();
    assertThat(result.getErrors()).hasSize(1);
    return result.getErrors().get(0);
  }

  private static ConstraintViolationException violation() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    return new ConstraintViolationException(validator.validate(new Bean()));
  }

  static class Bean {
    @NotBlank String name = "";
  }
}
