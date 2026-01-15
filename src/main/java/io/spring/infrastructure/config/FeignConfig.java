package io.spring.infrastructure.config;

import feign.Logger;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Feign clients. This configuration enables Feign clients and provides
 * custom settings for inter-service communication.
 */
@Configuration
@EnableFeignClients(basePackages = "io.spring.infrastructure.client")
public class FeignConfig {

  /**
   * Configures the Feign logger level. FULL level logs headers, body, and metadata for debugging.
   */
  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }

  /**
   * Configures the Feign retryer for automatic retry on failures. Retries up to 3 times with
   * exponential backoff starting at 100ms.
   */
  @Bean
  public Retryer retryer() {
    return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
  }
}
