package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTest {

  @Mock private UserReadService userReadService;

  private UserQueryService userQueryService;

  @BeforeEach
  public void setUp() {
    userQueryService = new UserQueryService(userReadService);
  }

  @Test
  public void should_find_user_by_id_when_exists() {
    String userId = "user-123";
    UserData expectedUserData = new UserData(userId, "test@example.com", "testuser", "bio", "image");

    when(userReadService.findById(userId)).thenReturn(expectedUserData);

    Optional<UserData> result = userQueryService.findById(userId);

    assertTrue(result.isPresent());
    assertEquals(expectedUserData, result.get());
    assertEquals("test@example.com", result.get().getEmail());
    assertEquals("testuser", result.get().getUsername());
    verify(userReadService).findById(userId);
  }

  @Test
  public void should_return_empty_when_user_not_found() {
    String userId = "non-existent-user";

    when(userReadService.findById(userId)).thenReturn(null);

    Optional<UserData> result = userQueryService.findById(userId);

    assertFalse(result.isPresent());
    verify(userReadService).findById(userId);
  }

  @Test
  public void should_return_empty_for_null_id() {
    when(userReadService.findById(null)).thenReturn(null);

    Optional<UserData> result = userQueryService.findById(null);

    assertFalse(result.isPresent());
    verify(userReadService).findById(null);
  }

  @Test
  public void should_return_user_data_with_all_fields() {
    String userId = "user-456";
    String email = "complete@example.com";
    String username = "completeuser";
    String bio = "This is a complete bio";
    String image = "https://example.com/image.jpg";
    UserData expectedUserData = new UserData(userId, email, username, bio, image);

    when(userReadService.findById(userId)).thenReturn(expectedUserData);

    Optional<UserData> result = userQueryService.findById(userId);

    assertTrue(result.isPresent());
    UserData userData = result.get();
    assertEquals(userId, userData.getId());
    assertEquals(email, userData.getEmail());
    assertEquals(username, userData.getUsername());
    assertEquals(bio, userData.getBio());
    assertEquals(image, userData.getImage());
  }

  @Test
  public void should_return_user_data_with_empty_optional_fields() {
    String userId = "user-789";
    UserData expectedUserData = new UserData(userId, "minimal@example.com", "minimaluser", "", "");

    when(userReadService.findById(userId)).thenReturn(expectedUserData);

    Optional<UserData> result = userQueryService.findById(userId);

    assertTrue(result.isPresent());
    UserData userData = result.get();
    assertEquals("", userData.getBio());
    assertEquals("", userData.getImage());
  }
}
