Feature: Current User API
  As an authenticated user
  I want to be able to get and update my profile
  So that I can manage my account information

  Scenario: Get current user profile
    Given I am authenticated as a user with email "currentuser@test.com" and username "currentuser"
    When I send a GET request to "/user" with authentication
    Then the response status should be 200
    And the response should contain a user with email "currentuser@test.com"
    And the response should contain a user with username "currentuser"

  Scenario: Get current user without authentication
    When I send a GET request to "/user" without authentication
    Then the response status should be 401

  Scenario: Update current user email
    Given I am authenticated as a user with email "updateuser@test.com" and username "updateuser"
    And I have an update user request with email "newemail@test.com"
    When I send a PUT request to "/user" with the update data
    Then the response status should be 200
    And the response should contain a user with email "newemail@test.com"

  Scenario: Update current user username
    Given I am authenticated as a user with email "updateuser2@test.com" and username "updateuser2"
    And I have an update user request with username "newusername"
    When I send a PUT request to "/user" with the update data
    Then the response status should be 200
    And the response should contain a user with username "newusername"

  Scenario: Update current user bio
    Given I am authenticated as a user with email "updatebio@test.com" and username "updatebio"
    And I have an update user request with bio "This is my new bio"
    When I send a PUT request to "/user" with the update data
    Then the response status should be 200
    And the response should contain a user with bio "This is my new bio"

  Scenario: Update current user image
    Given I am authenticated as a user with email "updateimage@test.com" and username "updateimage"
    And I have an update user request with image "https://example.com/newimage.jpg"
    When I send a PUT request to "/user" with the update data
    Then the response status should be 200
    And the response should contain a user with image "https://example.com/newimage.jpg"

  Scenario: Update current user without authentication
    Given I have an update user request with email "newemail@test.com"
    When I send a PUT request to "/user" without authentication
    Then the response status should be 401
