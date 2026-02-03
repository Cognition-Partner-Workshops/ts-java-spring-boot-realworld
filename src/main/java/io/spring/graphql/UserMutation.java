package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.application.facade.UserFacade;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.service.AuthContext;
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

  private UserFacade userFacade;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
  public DataFetcherResult<UserResult> createUser(@InputArgument("input") CreateUserInput input) {
    RegisterParam registerParam =
        new RegisterParam(input.getEmail(), input.getUsername(), input.getPassword());
    User user;
    try {
      UserFacade.UserResult result = userFacade.createUserWithEntity(registerParam);
      user = result.getUser();
    } catch (ConstraintViolationException cve) {
      return DataFetcherResult.<UserResult>newResult()
          .data(GraphQLCustomizeExceptionHandler.getErrorsAsData(cve))
          .build();
    }

    return DataFetcherResult.<UserResult>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(user)
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.Login)
  public DataFetcherResult<UserPayload> login(
      @InputArgument("password") String password, @InputArgument("email") String email) {
    UserFacade.UserResult result = userFacade.loginWithEntity(email, password);
    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(result.getUser())
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateUser)
  public DataFetcherResult<UserPayload> updateUser(
      @InputArgument("changes") UpdateUserInput updateUserInput) {
    User currentUser = AuthContext.getCurrentUser().orElse(null);
    if (currentUser == null) {
      return null;
    }
    UpdateUserParam param =
        UpdateUserParam.builder()
            .username(updateUserInput.getUsername())
            .email(updateUserInput.getEmail())
            .bio(updateUserInput.getBio())
            .password(updateUserInput.getPassword())
            .image(updateUserInput.getImage())
            .build();

    userFacade.updateUser(currentUser, param, userFacade.generateToken(currentUser));
    return DataFetcherResult.<UserPayload>newResult()
        .data(UserPayload.newBuilder().build())
        .localContext(currentUser)
        .build();
  }
}
