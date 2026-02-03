package io.spring.api.exception;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FieldErrorResourceTest {

  @Test
  public void should_create_field_error_resource_with_all_fields() {
    FieldErrorResource resource = new FieldErrorResource("resource", "field", "code", "message");
    
    assertThat(resource, is(notNullValue()));
    assertThat(resource.getResource(), is("resource"));
    assertThat(resource.getField(), is("field"));
    assertThat(resource.getCode(), is("code"));
    assertThat(resource.getMessage(), is("message"));
  }

  @Test
  public void should_handle_null_values() {
    FieldErrorResource resource = new FieldErrorResource(null, null, null, null);
    
    assertThat(resource, is(notNullValue()));
    assertThat(resource.getResource() == null, is(true));
    assertThat(resource.getField() == null, is(true));
    assertThat(resource.getCode() == null, is(true));
    assertThat(resource.getMessage() == null, is(true));
  }

  @Test
  public void should_handle_empty_strings() {
    FieldErrorResource resource = new FieldErrorResource("", "", "", "");
    
    assertThat(resource.getResource(), is(""));
    assertThat(resource.getField(), is(""));
    assertThat(resource.getCode(), is(""));
    assertThat(resource.getMessage(), is(""));
  }
}
