package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;
  @Mock private JwtService jwtService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private MeDatafetcher meDatafetcher;
  private User user;
  private UserData userData;

  @BeforeEach
  void setUp() {
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
    SecurityContextHolder.clearContext();
  }

  @Test
  void getMe_success() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getMe("Bearer token123", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user.getEmail(), result.getData().getEmail());
    assertEquals(user.getUsername(), result.getData().getUsername());
    assertEquals("token123", result.getData().getToken());
  }

  @Test
  void getMe_withAnonymousUser() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getMe("Bearer token123", dataFetchingEnvironment);

    assertNull(result);
  }

  @Test
  void getMe_withNullPrincipal() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(null, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getMe("Bearer token123", dataFetchingEnvironment);

    assertNull(result);
  }

  @Test
  void getMe_userNotFound() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> meDatafetcher.getMe("Bearer token123", dataFetchingEnvironment));
  }

  @Test
  void getUserPayloadUser_success() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(jwtService.toToken(eq(user))).thenReturn("generated-token");

    DataFetcherResult<io.spring.graphql.types.User> result = meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user.getEmail(), result.getData().getEmail());
    assertEquals(user.getUsername(), result.getData().getUsername());
    assertEquals("generated-token", result.getData().getToken());
  }
}
