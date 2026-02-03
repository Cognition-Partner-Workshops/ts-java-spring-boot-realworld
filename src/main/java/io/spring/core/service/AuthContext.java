package io.spring.core.service;

import io.spring.core.user.User;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthContext {

  public static Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return Optional.empty();
    }
    User currentUser = (User) authentication.getPrincipal();
    return Optional.of(currentUser);
  }
}
