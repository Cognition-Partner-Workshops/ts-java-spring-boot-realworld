package io.spring.api.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class JwtTokenFilterTest {

  @Mock private UserRepository userRepository;

  @Mock private JwtService jwtService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  private JwtTokenFilter jwtTokenFilter;

  @BeforeEach
  public void setUp() throws Exception {
    SecurityContextHolder.clearContext();
    jwtTokenFilter = new JwtTokenFilter();

    java.lang.reflect.Field userRepoField = JwtTokenFilter.class.getDeclaredField("userRepository");
    userRepoField.setAccessible(true);
    userRepoField.set(jwtTokenFilter, userRepository);

    java.lang.reflect.Field jwtServiceField = JwtTokenFilter.class.getDeclaredField("jwtService");
    jwtServiceField.setAccessible(true);
    jwtServiceField.set(jwtTokenFilter, jwtService);
  }

  @Test
  public void should_continue_filter_chain_when_no_authorization_header() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void should_continue_filter_chain_when_authorization_header_is_invalid() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("InvalidToken");

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void should_set_authentication_when_valid_token() throws Exception {
    String token = "valid-jwt-token";
    String userId = "user-123";
    User user = new User("test@example.com", "testuser", "password", "bio", "image");

    when(request.getHeader("Authorization")).thenReturn("Token " + token);
    when(jwtService.getSubFromToken(token)).thenReturn(Optional.of(userId));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertEquals(user, authentication.getPrincipal());
  }

  @Test
  public void should_not_set_authentication_when_token_is_invalid() throws Exception {
    String token = "invalid-jwt-token";

    when(request.getHeader("Authorization")).thenReturn("Token " + token);
    when(jwtService.getSubFromToken(token)).thenReturn(Optional.empty());

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void should_not_set_authentication_when_user_not_found() throws Exception {
    String token = "valid-jwt-token";
    String userId = "non-existent-user";

    when(request.getHeader("Authorization")).thenReturn("Token " + token);
    when(jwtService.getSubFromToken(token)).thenReturn(Optional.of(userId));
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  public void should_handle_bearer_prefix() throws Exception {
    String token = "valid-jwt-token";
    String userId = "user-123";
    User user = new User("test@example.com", "testuser", "password", "bio", "image");

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.getSubFromToken(token)).thenReturn(Optional.of(userId));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
  }

  @Test
  public void should_not_override_existing_authentication() throws Exception {
    String token = "valid-jwt-token";
    String userId = "user-123";
    User user = new User("test@example.com", "testuser", "password", "bio", "image");

    org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "existing-user", null, java.util.Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(request.getHeader("Authorization")).thenReturn("Token " + token);
    when(jwtService.getSubFromToken(token)).thenReturn(Optional.of(userId));

    jwtTokenFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(userRepository, never()).findById(anyString());
    assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
  }
}
