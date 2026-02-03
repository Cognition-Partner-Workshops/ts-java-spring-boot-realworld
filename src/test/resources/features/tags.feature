Feature: Tags API
  As a user of the RealWorld application
  I want to be able to get all available tags
  So that I can browse and filter articles by tags

  Scenario: Get all tags
    Given tags exist in the system
    When I send a GET request to "/tags"
    Then the response status should be 200
    And the response should contain a tags list

  Scenario: Get tags when no tags exist
    Given no tags exist in the system
    When I send a GET request to "/tags"
    Then the response status should be 200
    And the response should contain an empty tags list

  Scenario: Tags are returned from articles
    Given an article exists with title "Tagged Test Article" by user "taggedauthor" with tags "newtag1,newtag2"
    When I send a GET request to "/tags"
    Then the response status should be 200
    And the response should contain tags including "newtag1"
    And the response should contain tags including "newtag2"
