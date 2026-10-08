package io.spring.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page object for both /user/register and /user/login (identical form markup). */
public class AuthPage extends BasePage {
  private static final By HEADING = By.cssSelector("h1.text-xs-center");
  private static final By USERNAME = By.cssSelector("input[placeholder='Username']");
  private static final By EMAIL = By.cssSelector("input[placeholder='Email']");
  private static final By PASSWORD = By.cssSelector("input[placeholder='Password']");
  private static final By SUBMIT = By.cssSelector("form button[type='submit']");
  private static final By ERRORS = By.cssSelector(".error-messages li");

  public AuthPage(WebDriver driver) {
    super(driver);
  }

  public AuthPage openRegister(String baseUrl) {
    driver.get(baseUrl + "/user/register");
    wait.until(ExpectedConditions.textToBePresentInElementLocated(HEADING, "Sign Up"));
    return this;
  }

  public AuthPage openLogin(String baseUrl) {
    driver.get(baseUrl + "/user/login");
    wait.until(ExpectedConditions.textToBePresentInElementLocated(HEADING, "Sign in"));
    return this;
  }

  public String heading() {
    return getText(driver.findElement(HEADING));
  }

  public HomePage register(String username, String email, String password) {
    type(driver.findElement(USERNAME), username);
    type(driver.findElement(EMAIL), email);
    type(driver.findElement(PASSWORD), password);
    click(driver.findElement(SUBMIT));
    return new HomePage(driver).waitForLoggedInNav();
  }

  public HomePage login(String email, String password) {
    type(driver.findElement(EMAIL), email);
    type(driver.findElement(PASSWORD), password);
    click(driver.findElement(SUBMIT));
    return new HomePage(driver).waitForLoggedInNav();
  }

  public AuthPage loginExpectingError(String email, String password) {
    type(driver.findElement(EMAIL), email);
    type(driver.findElement(PASSWORD), password);
    click(driver.findElement(SUBMIT));
    wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(ERRORS, 0));
    return this;
  }

  public int errorCount() {
    return driver.findElements(ERRORS).size();
  }
}
