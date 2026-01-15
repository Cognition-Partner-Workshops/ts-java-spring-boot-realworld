package io.spring.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Resilience4j circuit breakers. This configuration provides circuit
 * breaker patterns for resilient inter-service communication.
 */
@Configuration
public class CircuitBreakerConfig {

  /**
   * Customizes the default circuit breaker factory with specific settings for inter-service
   * communication.
   */
  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory ->
        factory.configureDefault(
            id ->
                new Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(
                        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
                    .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .minimumNumberOfCalls(5)
                            .permittedNumberOfCallsInHalfOpenState(3)
                            .automaticTransitionFromOpenToHalfOpenEnabled(true)
                            .waitDurationInOpenState(Duration.ofSeconds(5))
                            .failureRateThreshold(50)
                            .build())
                    .build());
  }

  /**
   * Creates a circuit breaker specifically for the User Service with custom settings.
   */
  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> userServiceCustomizer() {
    return factory ->
        factory.configure(
            builder ->
                builder
                    .timeLimiterConfig(
                        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(3)).build())
                    .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .minimumNumberOfCalls(5)
                            .failureRateThreshold(50)
                            .waitDurationInOpenState(Duration.ofSeconds(10))
                            .build()),
            "user-service");
  }

  /**
   * Creates a circuit breaker specifically for the Article Service with custom settings.
   */
  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> articleServiceCustomizer() {
    return factory ->
        factory.configure(
            builder ->
                builder
                    .timeLimiterConfig(
                        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(5)).build())
                    .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .minimumNumberOfCalls(5)
                            .failureRateThreshold(50)
                            .waitDurationInOpenState(Duration.ofSeconds(10))
                            .build()),
            "article-service");
  }

  /**
   * Creates a circuit breaker specifically for the Comment Service with custom settings.
   */
  @Bean
  public Customizer<Resilience4JCircuitBreakerFactory> commentServiceCustomizer() {
    return factory ->
        factory.configure(
            builder ->
                builder
                    .timeLimiterConfig(
                        TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(3)).build())
                    .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .minimumNumberOfCalls(5)
                            .failureRateThreshold(50)
                            .waitDurationInOpenState(Duration.ofSeconds(10))
                            .build()),
            "comment-service");
  }

  /**
   * Provides access to the circuit breaker registry for monitoring and management.
   */
  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    return CircuitBreakerRegistry.ofDefaults();
  }

  /**
   * Creates a default circuit breaker instance for general use.
   */
  @Bean
  public CircuitBreaker defaultCircuitBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("default");
  }
}
