package io.spring.api.shared;

import io.spring.api.exception.FieldErrorResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * Utility class for handling exceptions consistently across REST and GraphQL APIs. This
 * consolidates the duplicated exception handling logic that was previously in both
 * CustomizeExceptionHandler and GraphQLCustomizeExceptionHandler.
 */
public final class ExceptionUtils {

  private ExceptionUtils() {}

  /**
   * Extracts field errors from a ConstraintViolationException into a list of FieldErrorResource
   * objects.
   *
   * @param ex the ConstraintViolationException to process
   * @return list of FieldErrorResource objects representing the validation errors
   */
  public static List<FieldErrorResource> extractFieldErrors(ConstraintViolationException ex) {
    List<FieldErrorResource> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      FieldErrorResource fieldErrorResource =
          new FieldErrorResource(
              violation.getRootBeanClass().getName(),
              extractFieldName(violation.getPropertyPath().toString()),
              violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
              violation.getMessage());
      errors.add(fieldErrorResource);
    }
    return errors;
  }

  /**
   * Converts a list of FieldErrorResource objects to a map format suitable for API responses.
   *
   * @param errors the list of field errors
   * @return a map where keys are field names and values are lists of error messages
   */
  public static Map<String, List<String>> toErrorMap(List<FieldErrorResource> errors) {
    Map<String, List<String>> errorMap = new HashMap<>();
    for (FieldErrorResource fieldErrorResource : errors) {
      errorMap
          .computeIfAbsent(fieldErrorResource.getField(), k -> new ArrayList<>())
          .add(fieldErrorResource.getMessage());
    }
    return errorMap;
  }

  /**
   * Converts a list of FieldErrorResource objects to a generic object map format.
   *
   * @param errors the list of field errors
   * @return a map where keys are field names and values are lists of error messages as Objects
   */
  public static Map<String, Object> toGenericErrorMap(List<FieldErrorResource> errors) {
    Map<String, Object> json = new HashMap<>();
    for (FieldErrorResource fieldErrorResource : errors) {
      if (!json.containsKey(fieldErrorResource.getField())) {
        json.put(fieldErrorResource.getField(), new ArrayList<String>());
      }
      @SuppressWarnings("unchecked")
      List<String> fieldErrors = (List<String>) json.get(fieldErrorResource.getField());
      fieldErrors.add(fieldErrorResource.getMessage());
    }
    return json;
  }

  /**
   * Extracts the field name from a property path string. For nested paths like
   * "methodName.paramName.fieldName", this returns "fieldName".
   *
   * @param propertyPath the full property path
   * @return the extracted field name
   */
  public static String extractFieldName(String propertyPath) {
    String[] splits = propertyPath.split("\\.");
    if (splits.length == 1) {
      return propertyPath;
    } else {
      return String.join(".", Arrays.copyOfRange(splits, 2, splits.length));
    }
  }
}
