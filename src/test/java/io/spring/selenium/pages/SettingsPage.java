package io.spring.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SettingsPage extends BasePage {
  private static final By HEADING = By.cssSelector("h1.text-xs-center");
  private static final By LOGOUT = By.xpath("//button[contains(normalize-space(),'logout')]");

  public SettingsPage(WebDriver driver) {
    super(driver);
  }

  private static final By SETTINGS_NAV_LINK = By.cssSelector("a[href='/user/settings']");

  /**
   * The settings page is server-side redirected to "/" when rendered without a client session, so
   * it must be reached through the navbar link (client-side navigation) rather than a direct URL.
   */
  public SettingsPage open(String baseUrl) {
    if (driver.findElements(SETTINGS_NAV_LINK).isEmpty()) {
      new HomePage(driver).open(baseUrl).waitForLoggedInNav();
    }
    click(wait.until(ExpectedConditions.elementToBeClickable(SETTINGS_NAV_LINK)));
    wait.until(ExpectedConditions.textToBePresentInElementLocated(HEADING, "Your Settings"));
    return this;
  }

  public HomePage logout() {
    click(wait.until(ExpectedConditions.elementToBeClickable(LOGOUT)));
    return new HomePage(driver).waitForLoggedOutNav();
  }
}
