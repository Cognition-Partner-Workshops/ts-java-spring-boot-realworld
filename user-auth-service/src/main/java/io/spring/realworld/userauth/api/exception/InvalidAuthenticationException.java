package io.spring.realworld.userauth.api.exception;

public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException() {
    super("invalid email or password");
  }
}
