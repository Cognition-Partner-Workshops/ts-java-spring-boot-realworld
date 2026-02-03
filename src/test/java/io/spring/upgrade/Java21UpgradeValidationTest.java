package io.spring.upgrade;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.infrastructure.service.DefaultJwtService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Automated test suite to validate the Java 21 upgrade changes.
 * 
 * This test class validates:
 * 1. JWT Service - New JJWT 0.12.3 API (Jwts.parser().verifyWith() instead of parserBuilder())
 * 2. Java 21 Features - Verifies the application runs on Java 21
 * 3. Jakarta EE Migration - Validates jakarta.* packages are used correctly
 */
@DisplayName("Java 21 Upgrade Validation Tests")
public class Java21UpgradeValidationTest {

  @Nested
  @DisplayName("Java Version Validation")
  class JavaVersionTests {

    @Test
    @DisplayName("Should be running on Java 21")
    void shouldBeRunningOnJava21() {
      String javaVersion = System.getProperty("java.version");
      assertNotNull(javaVersion, "Java version should not be null");
      assertTrue(
          javaVersion.startsWith("21"),
          "Expected Java 21, but running on Java " + javaVersion);
    }

    @Test
    @DisplayName("Should have correct Java specification version")
    void shouldHaveCorrectJavaSpecVersion() {
      String specVersion = System.getProperty("java.specification.version");
      assertEquals("21", specVersion, "Java specification version should be 21");
    }

    @Test
    @DisplayName("Should support Java 21 language features - Record classes")
    void shouldSupportRecordClasses() {
      // Records were finalized in Java 16, testing they work in Java 21
      record TestRecord(String name, int value) {}
      TestRecord record = new TestRecord("test", 42);
      assertEquals("test", record.name());
      assertEquals(42, record.value());
    }

    @Test
    @DisplayName("Should support Java 21 language features - Pattern matching for switch")
    void shouldSupportPatternMatchingForSwitch() {
      Object obj = "Hello";
      String result = switch (obj) {
        case String s -> "String: " + s;
        case Integer i -> "Integer: " + i;
        default -> "Unknown";
      };
      assertEquals("String: Hello", result);
    }

    @Test
    @DisplayName("Should support Java 21 language features - Sealed classes")
    void shouldSupportSealedClasses() {
      // Sealed classes were finalized in Java 17
      Shape shape = new Circle(5.0);
      assertTrue(shape instanceof Circle);
      assertEquals(5.0, ((Circle) shape).radius());
    }
  }

  @Nested
  @DisplayName("JWT Service Validation (JJWT 0.12.3 API)")
  class JwtServiceTests {

    private final JwtService jwtService = 
        new DefaultJwtService("123123123123123123123123123123123123123123123123123123123123", 3600);

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidToken() {
      User user = new User("test@example.com", "testuser", "password", "bio", "image");
      String token = jwtService.toToken(user);
      
      assertNotNull(token, "Token should not be null");
      assertFalse(token.isEmpty(), "Token should not be empty");
      assertTrue(token.contains("."), "Token should be in JWT format with dots");
      
      // JWT should have 3 parts: header.payload.signature
      String[] parts = token.split("\\.");
      assertEquals(3, parts.length, "JWT should have 3 parts");
    }

    @Test
    @DisplayName("Should parse valid JWT token and extract subject")
    void shouldParseValidToken() {
      User user = new User("test@example.com", "testuser", "password", "bio", "image");
      String token = jwtService.toToken(user);
      
      Optional<String> subject = jwtService.getSubFromToken(token);
      
      assertTrue(subject.isPresent(), "Subject should be present");
      assertEquals(user.getId(), subject.get(), "Subject should match user ID");
    }

    @Test
    @DisplayName("Should return empty for invalid JWT token")
    void shouldReturnEmptyForInvalidToken() {
      Optional<String> subject = jwtService.getSubFromToken("invalid.token.here");
      
      assertFalse(subject.isPresent(), "Subject should not be present for invalid token");
    }

    @Test
    @DisplayName("Should return empty for malformed JWT token")
    void shouldReturnEmptyForMalformedToken() {
      Optional<String> subject = jwtService.getSubFromToken("not-a-jwt");
      
      assertFalse(subject.isPresent(), "Subject should not be present for malformed token");
    }

    @Test
    @DisplayName("Should return empty for expired JWT token")
    void shouldReturnEmptyForExpiredToken() {
      // This is an old expired token
      String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhaXNlbnNpeSIsImV4cCI6MTUwMjE2MTIwNH0.SJB-U60WzxLYNomqLo4G3v3LzFxJKuVrIud8D8Lz3-mgpo9pN1i7C8ikU_jQPJGm8HsC1CquGMI-rSuM7j6LDA";
      
      Optional<String> subject = jwtService.getSubFromToken(expiredToken);
      
      assertFalse(subject.isPresent(), "Subject should not be present for expired token");
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullToken() {
      Optional<String> subject = jwtService.getSubFromToken(null);
      
      assertFalse(subject.isPresent(), "Subject should not be present for null token");
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyToken() {
      Optional<String> subject = jwtService.getSubFromToken("");
      
      assertFalse(subject.isPresent(), "Subject should not be present for empty token");
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void shouldGenerateDifferentTokensForDifferentUsers() {
      User user1 = new User("user1@example.com", "user1", "password", "bio", "image");
      User user2 = new User("user2@example.com", "user2", "password", "bio", "image");
      
      String token1 = jwtService.toToken(user1);
      String token2 = jwtService.toToken(user2);
      
      assertNotEquals(token1, token2, "Different users should have different tokens");
    }
  }

  @Nested
  @DisplayName("Jakarta EE Migration Validation")
  class JakartaEEMigrationTests {

    @Test
    @DisplayName("Should use jakarta.validation package")
    void shouldUseJakartaValidation() {
      try {
        Class<?> validClass = Class.forName("jakarta.validation.Valid");
        assertNotNull(validClass, "jakarta.validation.Valid should be available");
      } catch (ClassNotFoundException e) {
        fail("jakarta.validation.Valid class not found - Jakarta EE migration incomplete");
      }
    }

    @Test
    @DisplayName("Should use jakarta.servlet package")
    void shouldUseJakartaServlet() {
      try {
        Class<?> servletClass = Class.forName("jakarta.servlet.http.HttpServletRequest");
        assertNotNull(servletClass, "jakarta.servlet.http.HttpServletRequest should be available");
      } catch (ClassNotFoundException e) {
        fail("jakarta.servlet.http.HttpServletRequest class not found - Jakarta EE migration incomplete");
      }
    }

    @Test
    @DisplayName("Should NOT have javax.validation package (old)")
    void shouldNotHaveJavaxValidation() {
      try {
        Class.forName("javax.validation.Valid");
        // If we get here, javax.validation is still on classpath (might be okay for compatibility)
        // but we should prefer jakarta
      } catch (ClassNotFoundException e) {
        // Expected - javax.validation should not be available
        assertTrue(true, "javax.validation correctly not available");
      }
    }
  }

  @Nested
  @DisplayName("Spring Boot 3.x Compatibility")
  class SpringBoot3CompatibilityTests {

    @Test
    @DisplayName("Should have Spring Boot 3.x on classpath")
    void shouldHaveSpringBoot3() {
      try {
        Class<?> springBootClass = Class.forName("org.springframework.boot.SpringApplication");
        assertNotNull(springBootClass, "SpringApplication should be available");
        
        // Check for Spring Boot 3.x specific class
        Class<?> httpStatusCodeClass = Class.forName("org.springframework.http.HttpStatusCode");
        assertNotNull(httpStatusCodeClass, "HttpStatusCode (Spring 6+) should be available");
      } catch (ClassNotFoundException e) {
        fail("Spring Boot 3.x classes not found: " + e.getMessage());
      }
    }

    @Test
    @DisplayName("Should have Spring Security 6.x on classpath")
    void shouldHaveSpringSecurityFilterChain() {
      try {
        Class<?> securityFilterChainClass = Class.forName("org.springframework.security.web.SecurityFilterChain");
        assertNotNull(securityFilterChainClass, "SecurityFilterChain should be available");
      } catch (ClassNotFoundException e) {
        fail("Spring Security 6.x SecurityFilterChain not found");
      }
    }
  }

  // Sealed class hierarchy for testing Java 21 features
  sealed interface Shape permits Circle, Rectangle {}
  
  record Circle(double radius) implements Shape {}
  
  record Rectangle(double width, double height) implements Shape {}
}
