package io.spring.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EditorPage extends BasePage {
  private static final By TITLE = By.cssSelector("input[placeholder='Article Title']");
  private static final By DESCRIPTION =
      By.cssSelector("input[placeholder=\"What's this article about?\"]");
  private static final By BODY =
      By.cssSelector("textarea[placeholder='Write your article (in markdown)']");
  private static final By TAGS = By.cssSelector("input[placeholder='Enter tags']");
  private static final By PUBLISH = By.xpath("//button[normalize-space()='Publish Article']");

  public EditorPage(WebDriver driver) {
    super(driver);
  }

  public EditorPage open(String baseUrl) {
    driver.get(baseUrl + "/editor/new");
    wait.until(ExpectedConditions.visibilityOfElementLocated(TITLE));
    return this;
  }

  /** The editor redirects to the home feed after publishing; the new article is the top preview. */
  public HomePage publish(String title, String description, String body, String... tags) {
    type(driver.findElement(TITLE), title);
    type(driver.findElement(DESCRIPTION), description);
    type(driver.findElement(BODY), body);
    WebElement tagInput = driver.findElement(TAGS);
    for (String tag : tags) {
      tagInput.sendKeys(tag);
      tagInput.sendKeys(Keys.ENTER);
    }
    click(driver.findElement(PUBLISH));
    wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/editor")));
    HomePage home = new HomePage(driver);
    home.previewForTitle(title);
    return home;
  }
}
