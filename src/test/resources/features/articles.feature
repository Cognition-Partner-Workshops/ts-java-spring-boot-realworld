Feature: Articles API
  As a user of the RealWorld application
  I want to be able to create and manage articles
  So that I can share my content with others

  Scenario: Create a new article successfully
    Given I am authenticated as a user with email "author@test.com" and username "author"
    And I have a new article request with title "Test Article" and description "Test Description" and body "Test Body"
    When I send a POST request to "/articles" with the article data
    Then the response status should be 200
    And the response should contain an article with title "Test Article"
    And the response should contain an article with slug "test-article"

  Scenario: Create an article with tags
    Given I am authenticated as a user with email "author2@test.com" and username "author2"
    And I have a new article request with title "Tagged Article" and description "Description" and body "Body" and tags "java,spring"
    When I send a POST request to "/articles" with the article data
    Then the response status should be 200
    And the response should contain an article with tags "java,spring"

  Scenario: Create an article without authentication
    Given I have a new article request with title "Test Article" and description "Test Description" and body "Test Body"
    When I send a POST request to "/articles" without authentication
    Then the response status should be 401

  Scenario: Get articles list
    Given an article exists with title "List Article" by user "listauthor"
    When I send a GET request to "/articles"
    Then the response status should be 200
    And the response should contain articles list

  Scenario: Get articles filtered by tag
    Given an article exists with title "Java Article" by user "javaauthor" with tags "java"
    When I send a GET request to "/articles?tag=java"
    Then the response status should be 200
    And the response should contain articles with tag "java"

  Scenario: Get articles filtered by author
    Given an article exists with title "Author Article" by user "specificauthor"
    When I send a GET request to "/articles?author=specificauthor"
    Then the response status should be 200
    And the response should contain articles by author "specificauthor"

  Scenario: Get articles with pagination
    Given multiple articles exist
    When I send a GET request to "/articles?limit=5&offset=0"
    Then the response status should be 200
    And the response should contain at most 5 articles

  Scenario: Get user feed
    Given I am authenticated as a user with email "feeduser@test.com" and username "feeduser"
    And I follow a user "followedauthor" who has articles
    When I send a GET request to "/articles/feed" with authentication
    Then the response status should be 200
    And the response should contain articles from followed users

  Scenario: Get user feed without authentication
    When I send a GET request to "/articles/feed" without authentication
    Then the response status should be 401
