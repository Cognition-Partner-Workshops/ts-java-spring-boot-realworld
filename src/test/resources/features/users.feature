Feature: Users API
  As a user of the RealWorld application
  I want to be able to register and login
  So that I can access the application features

  Scenario: Register a new user successfully
    Given I have a valid registration request with email "newuser@test.com" and username "newuser" and password "password123"
    When I send a POST request to "/users" with the registration data
    Then the response status should be 201
    And the response should contain a user with email "newuser@test.com"
    And the response should contain a token

  Scenario: Register a user with invalid email
    Given I have a registration request with invalid email "invalid-email" and username "testuser" and password "password123"
    When I send a POST request to "/users" with the registration data
    Then the response status should be 422

  Scenario: Register a user with duplicate email
    Given a user exists with email "existing@test.com" and username "existinguser" and password "password123"
    And I have a valid registration request with email "existing@test.com" and username "newuser2" and password "password123"
    When I send a POST request to "/users" with the registration data
    Then the response status should be 422

  Scenario: Register a user with duplicate username
    Given a user exists with email "user1@test.com" and username "duplicateuser" and password "password123"
    And I have a valid registration request with email "user2@test.com" and username "duplicateuser" and password "password123"
    When I send a POST request to "/users" with the registration data
    Then the response status should be 422

  Scenario: Login with valid credentials
    Given a user exists with email "loginuser@test.com" and username "loginuser" and password "password123"
    And I have a login request with email "loginuser@test.com" and password "password123"
    When I send a POST request to "/users/login" with the login data
    Then the response status should be 200
    And the response should contain a user with email "loginuser@test.com"
    And the response should contain a token

  Scenario: Login with invalid password
    Given a user exists with email "wrongpass@test.com" and username "wrongpassuser" and password "password123"
    And I have a login request with email "wrongpass@test.com" and password "wrongpassword"
    When I send a POST request to "/users/login" with the login data
    Then the response status should be 422

  Scenario: Login with non-existent email
    Given I have a login request with email "nonexistent@test.com" and password "password123"
    When I send a POST request to "/users/login" with the login data
    Then the response status should be 422
