Feature: Comments API
  As a user of the RealWorld application
  I want to be able to create and manage comments on articles
  So that I can engage with content

  Scenario: Create a comment on an article
    Given I am authenticated as a user with email "commenter@test.com" and username "commenter"
    And an article exists with title "Comment Article" by user "commentarticleauthor"
    And I have a new comment request with body "This is a great article!"
    When I send a POST request to comment on the current article with the comment data
    Then the response status should be 201
    And the response should contain a comment with body "This is a great article!"

  Scenario: Create a comment without authentication
    Given an article exists with title "No Auth Comment Article" by user "noauthcommentauthor"
    And I have a new comment request with body "Anonymous comment"
    When I send a POST request to comment on the current article without authentication
    Then the response status should be 401

  Scenario: Create a comment on non-existent article
    Given I am authenticated as a user with email "commenter2@test.com" and username "commenter2"
    And I have a new comment request with body "Comment on nothing"
    When I send a POST request to "/articles/non-existent-article-xyz/comments" with the comment data
    Then the response status should be 404

  Scenario: Get comments for an article
    Given an article exists with title "Get Comments Article" by user "getcommentsauthor"
    And the article has comments
    When I send a GET request to get comments on the current article
    Then the response status should be 200
    And the response should contain a comments list

  Scenario: Get comments for article with no comments
    Given an article exists with title "No Comments Article" by user "nocommentsauthor"
    When I send a GET request to get comments on the current article
    Then the response status should be 200
    And the response should contain an empty comments list

  Scenario: Get comments for non-existent article
    When I send a GET request to "/articles/non-existent-comments-xyz/comments"
    Then the response status should be 404

  Scenario: Delete own comment
    Given I am authenticated as a user with email "deletecommenter@test.com" and username "deletecommenter"
    And an article exists with title "Delete Comment Article" by user "deletecommentauthor"
    And I have created a comment with body "Delete this comment" on the article
    When I send a DELETE request to delete my comment with authentication
    Then the response status should be 204

  Scenario: Delete comment without authentication
    Given an article exists with title "No Auth Delete Comment" by user "noauthdeletecauthor"
    And a comment exists on the article by user "somecommenter"
    When I send a DELETE request to delete the comment without authentication
    Then the response status should be 401

  Scenario: Delete comment by non-author
    Given I am authenticated as a user with email "wrongdeleter@test.com" and username "wrongdeleter"
    And an article exists with title "Wrong Delete Comment" by user "wrongdeleteauthor"
    And a comment exists on the article by user "realcommenter"
    When I send a DELETE request to delete the comment with authentication
    Then the response status should be 403

  Scenario: Delete non-existent comment
    Given I am authenticated as a user with email "deletenonexistc@test.com" and username "deletenonexistc"
    And an article exists with title "Delete Nonexist Comment" by user "deletenonexistcauthor"
    When I send a DELETE request to "/articles/delete-nonexist-comment/comments/nonexistent-id" with authentication
    Then the response status should be 404
