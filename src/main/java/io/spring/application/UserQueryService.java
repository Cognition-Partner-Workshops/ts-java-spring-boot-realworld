package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserQueryService {
  private static final Logger log = LoggerFactory.getLogger(UserQueryService.class);
  private UserReadService userReadService;

  public Optional<UserData> findById(String id) {
    log.info("Entering findById() with id={}", id);
    Optional<UserData> result = Optional.ofNullable(userReadService.findById(id));
    log.info("Exiting findById()");
    return result;
  }
}
