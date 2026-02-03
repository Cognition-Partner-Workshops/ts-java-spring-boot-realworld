Feature: Single Article API
  As a user of the RealWorld application
  I want to be able to view, update, and delete individual articles
  So that I can manage my content

  Scenario: Get article by slug
    Given an article exists with title "Get Article" by user "getauthor"
    When I send a GET request to the current article
    Then the response status should be 200
    And the response should contain an article with title "Get Article"

  Scenario: Get non-existent article
    When I send a GET request to "/articles/non-existent-article-xyz"
    Then the response status should be 404

  Scenario: Update article title
    Given I am authenticated as a user with email "updateauthor@test.com" and username "updateauthor"
    And I have created an article with title "Original Title" and description "Description" and body "Body"
    And I have an update article request with title "Updated Title"
    When I send a PUT request to the current article with the update data
    Then the response status should be 200
    And the response should contain an article with title "Updated Title"

  Scenario: Update article description
    Given I am authenticated as a user with email "updateauthor2@test.com" and username "updateauthor2"
    And I have created an article with title "Update Desc Article" and description "Original Description" and body "Body"
    And I have an update article request with description "Updated Description"
    When I send a PUT request to the current article with the update data
    Then the response status should be 200
    And the response should contain an article with description "Updated Description"

  Scenario: Update article body
    Given I am authenticated as a user with email "updateauthor3@test.com" and username "updateauthor3"
    And I have created an article with title "Update Body Article" and description "Description" and body "Original Body"
    And I have an update article request with body "Updated Body"
    When I send a PUT request to the current article with the update data
    Then the response status should be 200
    And the response should contain an article with body "Updated Body"

  Scenario: Update article without authentication
    Given an article exists with title "No Auth Update" by user "noauthauthor"
    And I have an update article request with title "Updated Title"
    When I send a PUT request to the current article without authentication
    Then the response status should be 401

  Scenario: Update article by non-author
    Given I am authenticated as a user with email "nonauthor@test.com" and username "nonauthor"
    And an article exists with title "Other Author Article" by user "otherauthor"
    And I have an update article request with title "Hacked Title"
    When I send a PUT request to the current article with the update data
    Then the response status should be 403

  Scenario: Delete article successfully
    Given I am authenticated as a user with email "deleteauthor@test.com" and username "deleteauthor"
    And I have created an article with title "Delete Me Article" and description "Description" and body "Body"
    When I send a DELETE request to the current article with authentication
    Then the response status should be 204

  Scenario: Delete article without authentication
    Given an article exists with title "No Auth Delete" by user "noauthdeleteauthor"
    When I send a DELETE request to the current article without authentication
    Then the response status should be 401

  Scenario: Delete article by non-author
    Given I am authenticated as a user with email "nondeleteauthor@test.com" and username "nondeleteauthor"
    And an article exists with title "Other Delete Article" by user "otherdeleteauthor"
    When I send a DELETE request to the current article with authentication
    Then the response status should be 403

  Scenario: Delete non-existent article
    Given I am authenticated as a user with email "deletenonexist@test.com" and username "deletenonexist"
    When I send a DELETE request to "/articles/non-existent-delete-xyz" with authentication
    Then the response status should be 404
