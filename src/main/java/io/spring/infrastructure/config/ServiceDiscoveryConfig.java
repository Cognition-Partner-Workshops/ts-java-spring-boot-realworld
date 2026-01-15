package io.spring.infrastructure.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for service discovery using Eureka. This configuration enables the
 * application to register with a Eureka service registry and discover other services.
 *
 * <p>The discovery client is enabled only when the 'eureka' profile is active, allowing the
 * application to run without Eureka in development mode.
 */
@Configuration
@EnableDiscoveryClient
@Profile("eureka")
public class ServiceDiscoveryConfig {}
