package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserQueryService {
  private UserReadService userReadService;

  public Optional<UserData> findById(String id) {
    log.info("Entering findById with parameters: id={}", id);
    Optional<UserData> result = Optional.ofNullable(userReadService.findById(id));
    log.info("Exiting findById with result: {}", result.isPresent() ? "present" : "empty");
    return result;
  }
}
