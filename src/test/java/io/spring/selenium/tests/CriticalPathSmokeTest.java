package io.spring.selenium.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.spring.selenium.pages.ArticlePage;
import io.spring.selenium.pages.AuthPage;
import io.spring.selenium.pages.EditorPage;
import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.SettingsPage;
import java.util.UUID;
import org.testng.annotations.Test;

/**
 * Critical path through the running frontend (port 3000) + backend (port 8080): home feed loads,
 * register -> login -> create article -> view article -> add comment -> favorite -> logout.
 */
public class CriticalPathSmokeTest extends BaseTest {

  private String baseUrl() {
    return config.getProperty("base.url", "http://localhost:3000");
  }

  @Test(description = "Home page renders the banner, Global Feed tab and seeded article previews")
  public void homeFeedLoads() {
    test = createTest("homeFeedLoads", "Home feed loads with seeded articles");
    HomePage home = new HomePage(driver).open(baseUrl());
    assertTrue(home.isBannerDisplayed(), "banner should be visible");
    assertTrue(home.isGlobalFeedTabDisplayed(), "Global Feed tab should be visible");
    assertTrue(home.waitForArticlePreviews() >= 1, "seeded articles should be listed");
    assertTrue(home.isSignInLinkDisplayed(), "anonymous nav shows Sign in");
    assertTrue(home.isSignUpLinkDisplayed(), "anonymous nav shows Sign up");
    test.pass("Home feed loaded with " + home.articlePreviews().size() + " previews");
  }

  @Test(
      description = "Register -> login -> create article -> view -> comment -> favorite -> logout")
  public void registerLoginCreateArticleCommentFavoriteLogout() {
    test = createTest("criticalPath", "End-to-end critical user journey");
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    String username = "e2e" + suffix;
    String email = username + "@example.com";
    String password = "Passw0rd!" + suffix;
    String title = "E2E Article " + suffix;

    HomePage home =
        new AuthPage(driver).openRegister(baseUrl()).register(username, email, password);
    assertTrue(home.isLoggedInNavDisplayed(), "registration should log the user in");
    test.info("registered " + username);

    new SettingsPage(driver).open(baseUrl()).logout();
    home = new AuthPage(driver).openLogin(baseUrl()).login(email, password);
    assertTrue(home.isLoggedInNavDisplayed(), "login should show authenticated nav");
    test.info("logged in as " + username);

    home =
        new EditorPage(driver)
            .open(baseUrl())
            .publish(title, "e2e description", "e2e **markdown** body", "e2e", "selenium");
    assertEquals(home.favoriteCountForTitle(title), 0, "new article starts unfavorited");
    ArticlePage article = home.openArticle(title);
    assertEquals(article.title(), title);
    assertTrue(article.slugFromUrl().startsWith("e2e-article-"), article.slugFromUrl());
    assertTrue(article.bodyText().contains("markdown"), "rendered body should contain text");
    assertTrue(article.tags().contains("e2e"), "tags: " + article.tags());
    test.info("created article " + article.slugFromUrl());

    article.addComment("Great write-up " + suffix);
    assertTrue(
        article.commentBodies().stream().anyMatch(c -> c.contains("Great write-up " + suffix)),
        "new comment should appear: " + article.commentBodies());
    test.info("comment added");

    home = new HomePage(driver).open(baseUrl());
    int before = home.favoriteCountForTitle(title);
    home.favoriteArticle(title);
    assertEquals(home.favoriteCountForTitle(title), before + 1, "favorite count increments");
    test.info("favorited article");

    home = new SettingsPage(driver).open(baseUrl()).logout();
    assertTrue(home.isSignInLinkDisplayed(), "logout should show Sign in link");
    assertFalse(home.isLoggedInNavDisplayed(), "logout should hide authenticated nav");
    test.pass("critical path completed for " + username);
  }

  @Test(description = "Login with a wrong password shows a validation error")
  public void loginWithWrongPasswordShowsError() {
    test = createTest("loginWrongPassword", "Invalid credentials are rejected in the UI");
    AuthPage login = new AuthPage(driver).openLogin(baseUrl());
    login.loginExpectingError("john@example.com", "definitely-wrong");
    assertTrue(login.errorCount() >= 1, "an error message should be displayed");
    assertEquals(login.heading(), "Sign in");
    test.pass("error displayed");
  }
}
