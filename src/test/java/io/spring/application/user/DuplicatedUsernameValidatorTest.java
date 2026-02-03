package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuplicatedUsernameValidatorTest {

  @Mock private UserRepository userRepository;

  @Mock private ConstraintValidatorContext context;

  @InjectMocks private DuplicatedUsernameValidator validator;

  @BeforeEach
  public void setUp() {
    validator = new DuplicatedUsernameValidator();
    try {
      java.lang.reflect.Field field =
          DuplicatedUsernameValidator.class.getDeclaredField("userRepository");
      field.setAccessible(true);
      field.set(validator, userRepository);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void should_return_true_when_username_is_null() {
    boolean result = validator.isValid(null, context);

    assertTrue(result);
    verifyNoInteractions(userRepository);
  }

  @Test
  public void should_return_true_when_username_is_empty() {
    boolean result = validator.isValid("", context);

    assertTrue(result);
    verifyNoInteractions(userRepository);
  }

  @Test
  public void should_return_true_when_username_does_not_exist() {
    String username = "newuser";
    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

    boolean result = validator.isValid(username, context);

    assertTrue(result);
    verify(userRepository).findByUsername(username);
  }

  @Test
  public void should_return_false_when_username_already_exists() {
    String username = "existinguser";
    User existingUser = new User("test@example.com", username, "password", "bio", "image");
    when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

    boolean result = validator.isValid(username, context);

    assertFalse(result);
    verify(userRepository).findByUsername(username);
  }
}
