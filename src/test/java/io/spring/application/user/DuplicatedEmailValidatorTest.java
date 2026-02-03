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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuplicatedEmailValidatorTest {

  @Mock private UserRepository userRepository;

  @Mock private ConstraintValidatorContext context;

  private DuplicatedEmailValidator validator;

  @BeforeEach
  public void setUp() {
    validator = new DuplicatedEmailValidator();
    try {
      java.lang.reflect.Field field =
          DuplicatedEmailValidator.class.getDeclaredField("userRepository");
      field.setAccessible(true);
      field.set(validator, userRepository);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void should_return_true_when_email_is_null() {
    boolean result = validator.isValid(null, context);

    assertTrue(result);
    verifyNoInteractions(userRepository);
  }

  @Test
  public void should_return_true_when_email_is_empty() {
    boolean result = validator.isValid("", context);

    assertTrue(result);
    verifyNoInteractions(userRepository);
  }

  @Test
  public void should_return_true_when_email_does_not_exist() {
    String email = "new@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    boolean result = validator.isValid(email, context);

    assertTrue(result);
    verify(userRepository).findByEmail(email);
  }

  @Test
  public void should_return_false_when_email_already_exists() {
    String email = "existing@example.com";
    User existingUser = new User(email, "existinguser", "password", "bio", "image");
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    boolean result = validator.isValid(email, context);

    assertFalse(result);
    verify(userRepository).findByEmail(email);
  }
}
