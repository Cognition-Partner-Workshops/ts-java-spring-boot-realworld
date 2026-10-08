package io.spring.regression.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.api.exception.ErrorResource;
import io.spring.api.exception.FieldErrorResource;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.api.exception.InvalidRequestException;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.graphql.exception.AuthenticationException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;

class ApiExceptionTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void error_resource_serializes_field_errors_grouped_by_field() throws Exception {
    ErrorResource resource =
        new ErrorResource(
            Arrays.asList(
                new FieldErrorResource("user", "email", "NotBlank", "can't be empty"),
                new FieldErrorResource("user", "email", "Email", "should be an email"),
                new FieldErrorResource("user", "username", "NotBlank", "can't be empty")));
    assertEquals(3, resource.getFieldErrors().size());
    JsonNode json = mapper.readTree(mapper.writeValueAsString(resource));
    assertEquals(2, json.get("errors").size());
    assertEquals("can't be empty", json.get("errors").get("email").get(0).asText());
    assertEquals("should be an email", json.get("errors").get("email").get(1).asText());
    assertEquals("can't be empty", json.get("errors").get("username").get(0).asText());
  }

  @Test
  void error_resource_with_no_errors_serializes_empty_object() throws Exception {
    JsonNode json = mapper.readTree(mapper.writeValueAsString(new ErrorResource(Arrays.asList())));
    assertTrue(json.get("errors").isObject());
    assertEquals(0, json.get("errors").size());
  }

  @Test
  void field_error_resource_getters() {
    FieldErrorResource f = new FieldErrorResource("r", "f", "c", "m");
    assertEquals("r", f.getResource());
    assertEquals("f", f.getField());
    assertEquals("c", f.getCode());
    assertEquals("m", f.getMessage());
  }

  @Test
  void exception_types_carry_expected_status_and_messages() {
    assertEquals("invalid email or password", new InvalidAuthenticationException().getMessage());
    assertEquals(
        HttpStatus.FORBIDDEN,
        AnnotatedElementUtils.findMergedAnnotation(
                NoAuthorizationException.class, ResponseStatus.class)
            .value());
    assertEquals(
        HttpStatus.NOT_FOUND,
        AnnotatedElementUtils.findMergedAnnotation(
                ResourceNotFoundException.class, ResponseStatus.class)
            .value());
    assertNotNull(new AuthenticationException());
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "target");
    errors.reject("code");
    InvalidRequestException ire = new InvalidRequestException(errors);
    assertEquals(errors, ire.getErrors());
    assertEquals("", ire.getMessage());
  }
}
