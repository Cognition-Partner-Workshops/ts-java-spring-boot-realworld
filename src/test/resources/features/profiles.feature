Feature: Profiles API
  As a user of the RealWorld application
  I want to be able to view and follow other users
  So that I can connect with other users

  Scenario: Get user profile
    Given a user exists with email "profileuser@test.com" and username "profileuser" and password "password123"
    When I send a GET request to "/profiles/profileuser"
    Then the response status should be 200
    And the response should contain a profile with username "profileuser"

  Scenario: Get user profile while authenticated
    Given I am authenticated as a user with email "viewer@test.com" and username "viewer"
    And a user exists with email "vieweduser@test.com" and username "vieweduser" and password "password123"
    When I send a GET request to "/profiles/vieweduser" with authentication
    Then the response status should be 200
    And the response should contain a profile with username "vieweduser"
    And the response should contain following status

  Scenario: Get non-existent user profile
    When I send a GET request to "/profiles/nonexistentuser"
    Then the response status should be 404

  Scenario: Follow a user
    Given I am authenticated as a user with email "follower@test.com" and username "follower"
    And a user exists with email "followee@test.com" and username "followee" and password "password123"
    When I send a POST request to "/profiles/followee/follow" with authentication
    Then the response status should be 200
    And the response should contain a profile with username "followee"
    And the response should indicate following is true

  Scenario: Follow a user without authentication
    Given a user exists with email "followee2@test.com" and username "followee2" and password "password123"
    When I send a POST request to "/profiles/followee2/follow" without authentication
    Then the response status should be 401

  Scenario: Follow a non-existent user
    Given I am authenticated as a user with email "follower2@test.com" and username "follower2"
    When I send a POST request to "/profiles/nonexistentfollow/follow" with authentication
    Then the response status should be 404

  Scenario: Unfollow a user
    Given I am authenticated as a user with email "unfollower@test.com" and username "unfollower"
    And a user exists with email "unfollowee@test.com" and username "unfollowee" and password "password123"
    And I am following the user "unfollowee"
    When I send a DELETE request to "/profiles/unfollowee/follow" with authentication
    Then the response status should be 200
    And the response should contain a profile with username "unfollowee"
    And the response should indicate following is false

  Scenario: Unfollow a user without authentication
    Given a user exists with email "unfollowee2@test.com" and username "unfollowee2" and password "password123"
    When I send a DELETE request to "/profiles/unfollowee2/follow" without authentication
    Then the response status should be 401

  Scenario: Unfollow a user not being followed
    Given I am authenticated as a user with email "unfollower2@test.com" and username "unfollower2"
    And a user exists with email "notfollowed@test.com" and username "notfollowed" and password "password123"
    When I send a DELETE request to "/profiles/notfollowed/follow" with authentication
    Then the response status should be 404

  Scenario: Unfollow a non-existent user
    Given I am authenticated as a user with email "unfollower3@test.com" and username "unfollower3"
    When I send a DELETE request to "/profiles/nonexistentunfollow/follow" with authentication
    Then the response status should be 404
