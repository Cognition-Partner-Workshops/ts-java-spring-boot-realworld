package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.application.data.UserWithToken;
import io.spring.application.facade.UserApiFacade;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import javax.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class UserMutation {

  private UserApiFacade userApiFacade;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
  public DataFetcherResult<UserResult> createUser(@InputArgument("input") CreateUserInput input) {
    UserWithToken userWithToken;
    try {
      userWithToken =
          userApiFacade.registerUser(input.getEmail(), input.getUsername(), input.getPassword());
    } catch (ConstraintViolationException cve) {
      return DataFetcherResult.<UserResult>newResult()
          .data(GraphQLCustomizeExceptionHandler.getErrorsAsData(cve))
          .build();
    }

    return DataFetcherResult.<UserResult>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(userWithToken)
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.Login)
  public DataFetcherResult<UserPayload> login(
      @InputArgument("password") String password, @InputArgument("email") String email) {
    UserWithToken userWithToken = userApiFacade.login(email, password);
    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(userWithToken)
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateUser)
  public DataFetcherResult<UserPayload> updateUser(
      @InputArgument("changes") UpdateUserInput updateUserInput) {
    User currentUser = SecurityUtil.getCurrentUser().orElse(null);
    if (currentUser == null) {
      return null;
    }
    UserWithToken userWithToken =
        userApiFacade.updateUser(
            currentUser,
            updateUserInput.getEmail(),
            updateUserInput.getUsername(),
            updateUserInput.getPassword(),
            updateUserInput.getBio(),
            updateUserInput.getImage(),
            null);
    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(userWithToken)
        .build();
  }
}
