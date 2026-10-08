package io.spring;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonCustomizations {

  @Bean
  public Module realWorldModules() {
    return new RealWorldModules();
  }

  public static class RealWorldModules extends SimpleModule {
    public RealWorldModules() {
      addSerializer(DateTime.class, new DateTimeSerializer());
      addSerializer(Instant.class, new InstantSerializer());
    }
  }

  /**
   * Serializes timestamps as ISO-8601 UTC with milliseconds, e.g. {@code 2024-01-05T12:34:56.789Z}.
   */
  public static class InstantSerializer extends StdSerializer<Instant> {

    static final DateTimeFormatter ISO_UTC_MILLIS =
        DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC);

    protected InstantSerializer() {
      super(Instant.class);
    }

    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        gen.writeString(ISO_UTC_MILLIS.format(value));
      }
    }
  }

  public static class DateTimeSerializer extends StdSerializer<DateTime> {

    protected DateTimeSerializer() {
      super(DateTime.class);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      if (value == null) {
        gen.writeNull();
      } else {
        gen.writeString(ISODateTimeFormat.dateTime().withZoneUTC().print(value));
      }
    }
  }
}
