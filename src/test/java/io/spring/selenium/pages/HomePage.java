package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {
  private static final By BANNER = By.cssSelector(".banner h1.logo-font");
  private static final By GLOBAL_FEED_TAB = By.linkText("Global Feed");
  private static final By ARTICLE_PREVIEWS = By.cssSelector(".article-preview");
  private static final By SIGN_IN_LINK = By.linkText("Sign in");
  private static final By SIGN_UP_LINK = By.linkText("Sign up");
  private static final By NEW_ARTICLE_LINK = By.cssSelector("a[href='/editor/new']");
  private static final By SETTINGS_LINK = By.cssSelector("a[href='/user/settings']");

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public HomePage open(String baseUrl) {
    driver.get(baseUrl + "/");
    wait.until(ExpectedConditions.visibilityOfElementLocated(BANNER));
    return this;
  }

  public boolean isBannerDisplayed() {
    return isDisplayed(driver.findElement(BANNER));
  }

  public boolean isGlobalFeedTabDisplayed() {
    return isDisplayed(wait.until(ExpectedConditions.visibilityOfElementLocated(GLOBAL_FEED_TAB)));
  }

  public int waitForArticlePreviews() {
    wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(ARTICLE_PREVIEWS, 0));
    return driver.findElements(ARTICLE_PREVIEWS).size();
  }

  public List<WebElement> articlePreviews() {
    waitForArticlePreviews();
    return driver.findElements(ARTICLE_PREVIEWS);
  }

  public WebElement previewForTitle(String title) {
    wait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                "//div[contains(@class,'article-preview')]//h1[normalize-space()="
                    + quote(title)
                    + "]")));
    return driver.findElement(
        By.xpath(
            "//div[contains(@class,'article-preview')][.//h1[normalize-space()="
                + quote(title)
                + "]]"));
  }

  public int favoriteCountForTitle(String title) {
    WebElement button = previewForTitle(title).findElement(By.cssSelector("button"));
    return Integer.parseInt(button.getText().trim().replaceAll("[^0-9]", ""));
  }

  public String readingTimeForTitle(String title) {
    WebElement badge = previewForTitle(title).findElement(By.cssSelector(".reading-time-badge"));
    return getText(wait.until(ExpectedConditions.visibilityOf(badge)));
  }

  public HomePage favoriteArticle(String title) {
    int before = favoriteCountForTitle(title);
    click(previewForTitle(title).findElement(By.cssSelector("button")));
    wait.until(d -> favoriteCountForTitle(title) != before);
    return this;
  }

  public ArticlePage openArticle(String title) {
    click(previewForTitle(title).findElement(By.cssSelector("a.preview-link")));
    return new ArticlePage(driver).waitForTitle(title);
  }

  public boolean isSignInLinkDisplayed() {
    return !driver.findElements(SIGN_IN_LINK).isEmpty();
  }

  public boolean isSignUpLinkDisplayed() {
    return !driver.findElements(SIGN_UP_LINK).isEmpty();
  }

  public boolean isLoggedInNavDisplayed() {
    return !driver.findElements(NEW_ARTICLE_LINK).isEmpty()
        && !driver.findElements(SETTINGS_LINK).isEmpty();
  }

  public HomePage waitForLoggedInNav() {
    wait.until(ExpectedConditions.visibilityOfElementLocated(NEW_ARTICLE_LINK));
    return this;
  }

  public HomePage waitForLoggedOutNav() {
    wait.until(ExpectedConditions.visibilityOfElementLocated(SIGN_IN_LINK));
    return this;
  }

  static String quote(String value) {
    return "\"" + value.replace("\"", "") + "\"";
  }
}
