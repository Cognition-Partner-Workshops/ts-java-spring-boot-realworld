package io.spring.api.shared;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.api.exception.FieldErrorResource;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.constraints.NotBlank;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;

public class ExceptionUtilsTest {

  @Test
  public void should_extract_field_errors_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = createMockViolation("field1", "must not be blank");
    violations.add(violation);

    ConstraintViolationException ex = new ConstraintViolationException(violations);

    List<FieldErrorResource> errors = ExceptionUtils.extractFieldErrors(ex);

    assertEquals(1, errors.size());
    assertEquals("field1", errors.get(0).getField());
    assertEquals("must not be blank", errors.get(0).getMessage());
  }

  @Test
  public void should_extract_multiple_field_errors() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("field1", "must not be blank"));
    violations.add(createMockViolation("field2", "must be valid"));

    ConstraintViolationException ex = new ConstraintViolationException(violations);

    List<FieldErrorResource> errors = ExceptionUtils.extractFieldErrors(ex);

    assertEquals(2, errors.size());
  }

  @Test
  public void should_convert_errors_to_map() {
    List<FieldErrorResource> errors =
        List.of(
            new FieldErrorResource("Object", "field1", "NotBlank", "must not be blank"),
            new FieldErrorResource("Object", "field1", "Size", "must be at least 3 characters"),
            new FieldErrorResource("Object", "field2", "Email", "must be a valid email"));

    Map<String, List<String>> errorMap = ExceptionUtils.toErrorMap(errors);

    assertEquals(2, errorMap.size());
    assertEquals(2, errorMap.get("field1").size());
    assertTrue(errorMap.get("field1").contains("must not be blank"));
    assertTrue(errorMap.get("field1").contains("must be at least 3 characters"));
    assertEquals(1, errorMap.get("field2").size());
    assertTrue(errorMap.get("field2").contains("must be a valid email"));
  }

  @Test
  public void should_convert_errors_to_generic_map() {
    List<FieldErrorResource> errors =
        List.of(
            new FieldErrorResource("Object", "field1", "NotBlank", "must not be blank"),
            new FieldErrorResource("Object", "field2", "Email", "must be a valid email"));

    Map<String, Object> errorMap = ExceptionUtils.toGenericErrorMap(errors);

    assertEquals(2, errorMap.size());
    assertTrue(errorMap.containsKey("field1"));
    assertTrue(errorMap.containsKey("field2"));
  }

  @Test
  public void should_extract_simple_field_name() {
    String result = ExceptionUtils.extractFieldName("fieldName");
    assertEquals("fieldName", result);
  }

  @Test
  public void should_extract_nested_field_name() {
    String result = ExceptionUtils.extractFieldName("method.param.fieldName");
    assertEquals("fieldName", result);
  }

  @Test
  public void should_extract_deeply_nested_field_name() {
    String result = ExceptionUtils.extractFieldName("method.param.nested.fieldName");
    assertEquals("nested.fieldName", result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String fieldName, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("method.param." + fieldName);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation =
        new NotBlank() {
          @Override
          public String message() {
            return message;
          }

          @Override
          public Class<?>[] groups() {
            return new Class[0];
          }

          @Override
          public Class<? extends javax.validation.Payload>[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends Annotation> annotationType() {
            return NotBlank.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
