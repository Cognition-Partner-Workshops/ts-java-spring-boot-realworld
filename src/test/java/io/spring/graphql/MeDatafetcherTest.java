package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class MeDatafetcherTest {

  @Mock private UserQueryService userQueryService;

  @Mock private JwtService jwtService;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private MeDatafetcher meDatafetcher;

  private User testUser;
  private UserData testUserData;

  @BeforeEach
  void setUp() {
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    testUserData =
        new UserData(testUser.getId(), testUser.getEmail(), testUser.getUsername(), "bio", "image");
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));
  }

  @Test
  void getMe_success() {
    authenticateUser();
    String authorization = "Bearer token123";

    when(userQueryService.findById(eq(testUser.getId()))).thenReturn(Optional.of(testUserData));

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe(authorization, dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testUser.getEmail(), result.getData().getEmail());
    assertEquals(testUser.getUsername(), result.getData().getUsername());
    assertEquals("token123", result.getData().getToken());
    assertEquals(testUser, result.getLocalContext());
  }

  @Test
  void getMe_notAuthenticated() {
    String authorization = "Bearer token123";

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe(authorization, dataFetchingEnvironment);

    assertNull(result);
  }

  @Test
  void getMe_anonymousAuthentication() {
    AnonymousAuthenticationToken anonymousToken =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousToken);

    String authorization = "Bearer token123";

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getMe(authorization, dataFetchingEnvironment);

    assertNull(result);
  }

  @Test
  void getUserPayloadUser_success() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(testUser);
    when(jwtService.toToken(eq(testUser))).thenReturn("generatedToken");

    DataFetcherResult<io.spring.graphql.types.User> result =
        meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testUser.getEmail(), result.getData().getEmail());
    assertEquals(testUser.getUsername(), result.getData().getUsername());
    assertEquals("generatedToken", result.getData().getToken());
    assertEquals(testUser, result.getLocalContext());
    verify(jwtService).toToken(eq(testUser));
  }
}
