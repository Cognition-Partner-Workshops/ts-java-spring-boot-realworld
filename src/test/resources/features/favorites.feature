Feature: Article Favorites API
  As a user of the RealWorld application
  I want to be able to favorite and unfavorite articles
  So that I can save articles I like

  Scenario: Favorite an article
    Given I am authenticated as a user with email "favoriter@test.com" and username "favoriter"
    And an article exists with title "Favorite Article" by user "favoriteauthor"
    When I send a POST request to favorite the current article with authentication
    Then the response status should be 200
    And the response should contain an article with favorited true
    And the response should contain an article with favoritesCount greater than 0

  Scenario: Favorite an article without authentication
    Given an article exists with title "No Auth Favorite" by user "noauthfavauthor"
    When I send a POST request to favorite the current article without authentication
    Then the response status should be 401

  Scenario: Favorite a non-existent article
    Given I am authenticated as a user with email "favoriter2@test.com" and username "favoriter2"
    When I send a POST request to "/articles/non-existent-favorite-xyz/favorite" with authentication
    Then the response status should be 404

  Scenario: Unfavorite an article
    Given I am authenticated as a user with email "unfavoriter@test.com" and username "unfavoriter"
    And an article exists with title "Unfavorite Article" by user "unfavoriteauthor"
    And I have favorited the article
    When I send a DELETE request to unfavorite the current article with authentication
    Then the response status should be 200
    And the response should contain an article with favorited false

  Scenario: Unfavorite an article without authentication
    Given an article exists with title "No Auth Unfavorite" by user "noauthunfavauthor"
    When I send a DELETE request to unfavorite the current article without authentication
    Then the response status should be 401

  Scenario: Unfavorite a non-existent article
    Given I am authenticated as a user with email "unfavoriter2@test.com" and username "unfavoriter2"
    When I send a DELETE request to "/articles/non-existent-unfavorite-xyz/favorite" with authentication
    Then the response status should be 404

  Scenario: Get articles favorited by user
    Given I am authenticated as a user with email "favlister@test.com" and username "favlister"
    And I have favorited multiple articles
    When I send a GET request to "/articles?favorited=favlister"
    Then the response status should be 200
    And the response should contain articles favorited by "favlister"
