package io.spring.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import graphql.ExecutionResult;
import io.spring.core.user.User;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MeQueryTest extends GraphQLTestBase {

  private static final String ME_QUERY =
      "{ me { email username token profile { username bio image following } } }";

  @Test
  void me_is_null_when_anonymous() {
    anonymous();
    ExecutionResult result = execute(ME_QUERY);
    assertThat(result.getErrors()).isEmpty();
    assertThat((Object) data(result, "me")).isNull();
  }

  @Test
  void me_returns_current_user_with_security_context() {
    User user = newUser();
    authenticateAs(user);
    ExecutionResult result = execute(ME_QUERY);
    Map<String, Object> me = data(result, "me");
    assertThat(me).isNotNull();
    assertThat(me.get("username")).isEqualTo(user.getUsername());
  }
}
