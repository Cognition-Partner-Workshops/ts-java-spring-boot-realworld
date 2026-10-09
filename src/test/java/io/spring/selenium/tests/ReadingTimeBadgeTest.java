package io.spring.selenium.tests;

import static org.testng.Assert.assertEquals;

import io.spring.selenium.pages.ArticlePage;
import io.spring.selenium.pages.AuthPage;
import io.spring.selenium.pages.EditorPage;
import io.spring.selenium.pages.HomePage;
import java.util.UUID;
import org.testng.annotations.Test;

public class ReadingTimeBadgeTest extends BaseTest {

  @Test(description = "Reading time appears on article pages and home feed previews")
  public void readingTimeBadgeAppearsOnArticleAndPreview() {
    test = createTest("readingTimeBadge", "Estimated reading time is shown for long articles");
    String baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    String username = "reading" + suffix;
    String title = "Reading Time " + suffix;
    String body = String.join(" ", java.util.Collections.nCopies(650, "word"));
    int expectedMinutes = (int) Math.ceil(body.trim().split("\\s+").length / 200.0);

    new AuthPage(driver)
        .openRegister(baseUrl)
        .register(username, username + "@example.com", "Passw0rd!" + suffix);
    EditorPage editor = new EditorPage(driver).open(baseUrl);
    HomePage home =
        editor.publishLongArticle(
            title, "Estimated reading time test", body, "reading-time", "selenium");

    String expectedBadge = expectedMinutes + " min read";
    assertEquals(home.readingTimeForTitle(title), expectedBadge);
    ArticlePage article = home.openArticle(title);
    assertEquals(article.readingTimeBadge(), expectedBadge);
    test.pass("Reading time appears as " + expectedBadge + " on article and feed preview");
  }
}
