package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.graphql.types.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class MeDatafetcherTest {

  private UserQueryService userQueryService;
  private JwtService jwtService;
  private MeDatafetcher meDatafetcher;
  private io.spring.core.user.User coreUser;
  private DataFetchingEnvironment dataFetchingEnvironment;

  @BeforeEach
  public void setUp() {
    userQueryService = mock(UserQueryService.class);
    jwtService = mock(JwtService.class);
    meDatafetcher = new MeDatafetcher(userQueryService, jwtService);
    coreUser = new io.spring.core.user.User("test@test.com", "testuser", "password", "bio", "image");
    dataFetchingEnvironment = mock(DataFetchingEnvironment.class);
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(io.spring.core.user.User user) {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_return_null_when_anonymous_authentication() {
    SecurityContextHolder.getContext().setAuthentication(
        mock(AnonymousAuthenticationToken.class));

    DataFetcherResult<User> result = meDatafetcher.getMe("Bearer token", dataFetchingEnvironment);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void should_return_null_when_null_principal() {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    DataFetcherResult<User> result = meDatafetcher.getMe("Bearer token", dataFetchingEnvironment);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void should_return_user_when_authenticated() {
    setAuthenticatedUser(coreUser);
    UserData userData = new UserData(coreUser.getId(), coreUser.getEmail(), 
        coreUser.getUsername(), coreUser.getBio(), coreUser.getImage());
    when(userQueryService.findById(coreUser.getId())).thenReturn(Optional.of(userData));

    DataFetcherResult<User> result = meDatafetcher.getMe("Bearer testtoken", dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().getEmail(), is("test@test.com"));
    assertThat(result.getData().getUsername(), is("testuser"));
    assertThat(result.getData().getToken(), is("testtoken"));
  }

  @Test
  public void should_throw_exception_when_user_not_found() {
    setAuthenticatedUser(coreUser);
    when(userQueryService.findById(coreUser.getId())).thenReturn(Optional.empty());

    try {
      meDatafetcher.getMe("Bearer testtoken", dataFetchingEnvironment);
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_return_user_payload_user() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreUser);
    when(jwtService.toToken(coreUser)).thenReturn("generated-token");

    DataFetcherResult<User> result = meDatafetcher.getUserPayloadUser(dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().getEmail(), is("test@test.com"));
    assertThat(result.getData().getUsername(), is("testuser"));
    assertThat(result.getData().getToken(), is("generated-token"));
  }
}
