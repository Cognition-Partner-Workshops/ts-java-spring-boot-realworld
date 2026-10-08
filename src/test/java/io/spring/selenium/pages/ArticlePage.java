package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ArticlePage extends BasePage {
  private static final By TITLE = By.cssSelector(".article-page h1, .banner h1");
  private static final By BODY = By.cssSelector(".article-content");
  private static final By TAGS = By.cssSelector(".tag-list li");
  private static final By COMMENT_INPUT =
      By.cssSelector("textarea[placeholder='Write a comment...']");
  private static final By POST_COMMENT = By.xpath("//button[normalize-space()='Post Comment']");
  private static final By COMMENTS = By.cssSelector(".card .card-text");

  public ArticlePage(WebDriver driver) {
    super(driver);
  }

  public ArticlePage waitForTitle(String title) {
    wait.until(ExpectedConditions.urlContains("/article/"));
    wait.until(ExpectedConditions.textToBePresentInElementLocated(TITLE, title));
    return this;
  }

  public String title() {
    return getText(driver.findElement(TITLE));
  }

  public String slugFromUrl() {
    String url = driver.getCurrentUrl();
    return url.substring(url.lastIndexOf("/article/") + "/article/".length());
  }

  public String bodyText() {
    return getText(wait.until(ExpectedConditions.visibilityOfElementLocated(BODY)));
  }

  public List<String> tags() {
    return driver.findElements(TAGS).stream()
        .map(e -> e.getText().trim())
        .collect(Collectors.toList());
  }

  public ArticlePage addComment(String body) {
    int before = commentBodies().size();
    type(wait.until(ExpectedConditions.visibilityOfElementLocated(COMMENT_INPUT)), body);
    click(driver.findElement(POST_COMMENT));
    wait.until(d -> commentBodies().size() > before);
    return this;
  }

  public List<String> commentBodies() {
    return driver.findElements(COMMENTS).stream()
        .map(e -> e.getText().trim())
        .collect(Collectors.toList());
  }
}
