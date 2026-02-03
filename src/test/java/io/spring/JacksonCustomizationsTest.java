package io.spring;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class JacksonCustomizationsTest {

  @Test
  public void should_create_real_world_modules_bean() {
    JacksonCustomizations customizations = new JacksonCustomizations();

    Module module = customizations.realWorldModules();

    assertThat(module, is(notNullValue()));
  }

  @Test
  public void should_serialize_datetime_to_iso_format() throws IOException {
    JacksonCustomizations.DateTimeSerializer serializer = new JacksonCustomizations.DateTimeSerializer();
    JsonGenerator gen = mock(JsonGenerator.class);
    SerializerProvider provider = mock(SerializerProvider.class);
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0);

    serializer.serialize(dateTime, gen, provider);

    verify(gen).writeString(org.mockito.ArgumentMatchers.contains("2023-01-15"));
  }

  @Test
  public void should_serialize_null_datetime() throws IOException {
    JacksonCustomizations.DateTimeSerializer serializer = new JacksonCustomizations.DateTimeSerializer();
    JsonGenerator gen = mock(JsonGenerator.class);
    SerializerProvider provider = mock(SerializerProvider.class);

    serializer.serialize(null, gen, provider);

    verify(gen).writeNull();
  }

  @Test
  public void should_create_real_world_modules_with_datetime_serializer() {
    JacksonCustomizations.RealWorldModules modules = new JacksonCustomizations.RealWorldModules();

    assertThat(modules, is(notNullValue()));
  }
}
