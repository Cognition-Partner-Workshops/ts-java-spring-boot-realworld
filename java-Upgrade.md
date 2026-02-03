# Java 21 Migration Guide

This document outlines the steps taken to upgrade the spring-boot-realworld-example-app project from Java 11 to Java 21, including all configuration changes, dependency updates, and code modifications required.

## Overview

The migration involved upgrading from Java 11 to Java 21 LTS, which required updating Spring Boot from version 2.6.3 to 3.2.5. Spring Boot 3.x brings significant changes including the migration from Java EE (javax.*) to Jakarta EE (jakarta.*) namespaces, Spring Security 6.x API changes, and updated dependency requirements.

## Prerequisites

Before starting the migration, ensure you have Java 21 JDK installed on your development machine. You can verify your Java version by running `java -version` in your terminal.

## Step 1: Update Gradle Wrapper

The Gradle wrapper must be updated to support Java 21. Gradle 7.x does not support Java 21 (class file version 65), so we upgraded to Gradle 8.7.

In `gradle/wrapper/gradle-wrapper.properties`, change the distribution URL from `gradle-7.4-bin.zip` to `gradle-8.7-bin.zip`. The updated property should read `distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip`.

## Step 2: Update Java Version Configuration

Update the `.java-version` file in the project root from `11` to `21`.

Update the GitHub Actions workflow in `.github/workflows/gradle.yml` to use Java 21 instead of Java 11. Change the java-version parameter from `'11'` to `'21'`.

## Step 3: Update build.gradle

The build.gradle file requires several updates to support Java 21 and Spring Boot 3.x.

### Plugin Updates

Update the following plugins to their latest compatible versions. The Spring Boot plugin should be updated from version 2.6.3 to 3.2.5. The Spring dependency management plugin should be updated from 1.0.11.RELEASE to 1.1.4. The DGS codegen plugin should be updated from 5.0.6 to 6.2.1. The Spotless plugin should be updated from 6.2.1 to 6.25.0.

### Java Compatibility

Update both sourceCompatibility and targetCompatibility from '11' to '21'.

### JaCoCo Version

Update the JaCoCo toolVersion from 0.8.7 to 0.8.11.

### Dependency Updates

The following dependencies need to be updated for Java 21 and Spring Boot 3.x compatibility:

MyBatis Spring Boot Starter should be updated from 2.2.2 to 3.0.3. The Netflix DGS starter should be changed from graphql-dgs-spring-boot-starter version 4.9.21 to graphql-dgs-spring-boot-starter version 8.5.5. Note that you should use graphql-dgs-spring-boot-starter, not graphql-dgs-spring-graphql-starter, to avoid conflicts with Spring's GraphQL auto-configuration.

The JJWT library (jjwt-api, jjwt-impl, jjwt-jackson) should be updated from 0.11.2 to 0.12.5. Joda-Time should be updated from 2.10.13 to 2.12.7. SQLite JDBC should be updated from 3.36.0.3 to 3.45.2.0.

For test dependencies, Rest-Assured (rest-assured, json-path, xml-path, spring-mock-mvc) should be updated from 4.5.1 to 5.4.0. Mockito-inline should be updated from 4.0.0 to 5.2.0. Selenium should be updated from 4.15.0 to 4.18.1. WebDriver Manager should be updated from 5.6.2 to 5.7.0. TestNG should be updated from 7.8.0 to 7.9.0. HttpClient5 should be updated from 5.2.1 to 5.3.1.

## Step 4: Migrate javax.* to jakarta.* Namespaces

Spring Boot 3.x requires Jakarta EE 9+, which uses the jakarta.* namespace instead of javax.*. All imports must be updated accordingly.

### Validation Imports

Replace all occurrences of `javax.validation.Valid` with `jakarta.validation.Valid`. Replace `javax.validation.constraints.*` with `jakarta.validation.constraints.*`. Replace `javax.validation.ConstraintValidator` with `jakarta.validation.ConstraintValidator`. Replace `javax.validation.ConstraintValidatorContext` with `jakarta.validation.ConstraintValidatorContext`. Replace `javax.validation.Constraint` with `jakarta.validation.Constraint`. Replace `javax.validation.Payload` with `jakarta.validation.Payload`. Replace `javax.validation.ConstraintViolation` with `jakarta.validation.ConstraintViolation`. Replace `javax.validation.ConstraintViolationException` with `jakarta.validation.ConstraintViolationException`.

### Servlet Imports

Replace `javax.servlet.FilterChain` with `jakarta.servlet.FilterChain`. Replace `javax.servlet.ServletException` with `jakarta.servlet.ServletException`. Replace `javax.servlet.http.HttpServletRequest` with `jakarta.servlet.http.HttpServletRequest`. Replace `javax.servlet.http.HttpServletResponse` with `jakarta.servlet.http.HttpServletResponse`.

### Files Requiring Updates

The following files require javax to jakarta migration: UsersApi.java, ArticlesApi.java, ArticleApi.java, CommentsApi.java, CurrentUserApi.java, UserService.java, RegisterParam.java, UpdateUserParam.java, DuplicatedEmailConstraint.java, DuplicatedUsernameConstraint.java, DuplicatedEmailValidator.java, DuplicatedUsernameValidator.java, ArticleCommandService.java, NewArticleParam.java, DuplicatedArticleConstraint.java, DuplicatedArticleValidator.java, JwtTokenFilter.java, CustomizeExceptionHandler.java, GraphQLCustomizeExceptionHandler.java, and UserMutation.java.

## Step 5: Refactor Spring Security Configuration

Spring Security 6.x removed the WebSecurityConfigurerAdapter class. The security configuration must be refactored to use the SecurityFilterChain bean pattern.

### Before (Spring Security 5.x)

The old approach extended WebSecurityConfigurerAdapter and overrode the configure(HttpSecurity http) method.

### After (Spring Security 6.x)

Create a @Bean method that returns SecurityFilterChain. Use the new lambda-based DSL for configuration. Replace antMatchers() with requestMatchers(). Replace authorizeRequests() with authorizeHttpRequests().

The WebSecurityConfig class should define a SecurityFilterChain bean method that configures CSRF, CORS, exception handling, session management, and authorization rules using the new lambda-based syntax.

## Step 6: Update JJWT Library Usage

The JJWT library 0.12.x has significant API changes from 0.11.x.

### Token Generation Changes

Replace setSubject() with subject(). Replace setExpiration() with expiration(). The signWith() method remains the same but SignatureAlgorithm enum is deprecated in favor of using Keys.hmacShaKeyFor().

### Token Parsing Changes

Replace Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token) with Jwts.parser().verifyWith(key).build().parseSignedClaims(token). Replace claimsJws.getBody() with claimsJws.getPayload().

### Key Generation

Replace new SecretKeySpec(secret.getBytes(), signatureAlgorithm.getJcaName()) with Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)).

## Step 7: Update GraphQL Exception Handler

The Netflix DGS 8.x changed the DataFetcherExceptionHandler interface.

### Method Signature Change

The onException method has been replaced with handleException. The return type changed from DataFetcherExceptionHandlerResult to CompletableFuture<DataFetcherExceptionHandlerResult>.

### Implementation Changes

Wrap return values with CompletableFuture.completedFuture(). Call defaultHandler.handleException() instead of defaultHandler.onException().

## Step 8: Update GraphQL PageInfo Types

The GraphQL datafetchers need to use the generated PageInfo type instead of graphql.relay.DefaultPageInfo.

### Import Changes

Replace graphql.relay.DefaultPageInfo with io.spring.graphql.types.PageInfo. Remove the import for graphql.relay.DefaultConnectionCursor if no longer needed.

### Builder Pattern Changes

Replace the DefaultPageInfo constructor with the PageInfo.newBuilder() pattern. Use startCursor(), endCursor(), hasPreviousPage(), and hasNextPage() builder methods.

## Step 9: Update CustomizeExceptionHandler

The ResponseEntityExceptionHandler method signature changed in Spring 6.x.

Replace HttpStatus with HttpStatusCode in the handleMethodArgumentNotValid method signature. The method now receives HttpStatusCode instead of HttpStatus as the status parameter.

## Step 10: Update Selenium WebDriverWait

Selenium 4.x changed the WebDriverWait constructor to require Duration instead of long.

Replace new WebDriverWait(driver, timeoutSeconds) with new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)). Add the import for java.time.Duration.

## Step 11: Verify the Upgrade

After completing all the changes, run the following commands to verify the upgrade.

First, compile the project by running `./gradlew compileJava --no-daemon`. This should complete without errors.

Then run the tests by executing `./gradlew test --no-daemon`. All tests should pass.

## Common Issues and Solutions

### Issue: Unsupported class file major version 65

This error occurs when using Gradle 7.x with Java 21. The solution is to upgrade Gradle to version 8.7 or later.

### Issue: Package javax.validation does not exist

Spring Boot 3.x uses Jakarta EE 9+ which requires jakarta.* namespaces. Update all javax.validation imports to jakarta.validation.

### Issue: WebSecurityConfigurerAdapter cannot be resolved

This class was removed in Spring Security 6.x. Refactor to use SecurityFilterChain bean pattern.

### Issue: Method parserBuilder() not found in Jwts

JJWT 0.12.x changed the API. Use Jwts.parser().verifyWith(key) instead of Jwts.parserBuilder().setSigningKey(key).

### Issue: No node type for 'ArticlesConnection'

This occurs when using graphql-dgs-spring-graphql-starter which conflicts with Spring's GraphQL auto-configuration. Use graphql-dgs-spring-boot-starter instead.

### Issue: WebDriverWait constructor expects Duration

Selenium 4.x changed the constructor signature. Use Duration.ofSeconds() to wrap the timeout value.

## Summary of Changes

This migration updated the project from Java 11 to Java 21 with Spring Boot 3.2.5. The key changes included upgrading Gradle from 7.4 to 8.7, migrating all javax.* imports to jakarta.*, refactoring Spring Security configuration to use SecurityFilterChain, updating JJWT library usage for version 0.12.x, updating GraphQL exception handler for DGS 8.x, fixing PageInfo type compatibility in GraphQL datafetchers, and updating Selenium WebDriverWait for version 4.x.

All tests pass after the migration, confirming that the application functions correctly with Java 21.
