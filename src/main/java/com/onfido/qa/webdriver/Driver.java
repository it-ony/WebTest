package com.onfido.qa.webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@SuppressWarnings("PublicField")
public class Driver implements WebDriver, JavascriptExecutor {

    public final RemoteWebDriver driver;

    private static final Logger logger = LoggerFactory.getLogger(Driver.class);

    public final WaitFor waitFor;

    public Driver(RemoteWebDriver driver) {
        this.driver = driver;
        waitFor = new WaitFor(driver);
    }

    public SessionId getSessionId() {
        return driver.getSessionId();
    }


    public WaitFor waitFor() {
        return waitFor;
    }

    public WaitFor waitFor(int timeout) {
        return new WaitFor(driver).timeout(timeout);
    }

    public void waitFor(ExpectedCondition condition) {
        waitFor().waitFor(condition);
    }

    public WebElement findElement(By... by) {
        return findElement(new ByChained(by));
    }

    public boolean isInDom(By by) {
        return !findElements(by).isEmpty();
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return driver.executeScript(script, args);
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        return driver.executeAsyncScript(script, args);
    }

    @Override
    public void get(String url) {
        driver.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return driver.findElement(by);
    }

    @Override
    public String getPageSource() {
        return driver.getPageSource();
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public void quit() {
        driver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    @Override
    public WebDriver.TargetLocator switchTo() {
        return driver.switchTo();
    }

    @Override
    public WebDriver.Navigation navigate() {
        return driver.navigate();
    }

    @Override
    public WebDriver.Options manage() {
        return driver.manage();
    }

    public void maximize() {
        var width = Integer.parseInt(this.executeScript("return window.screen.width").toString());
        var height = Integer.parseInt(this.executeScript("return window.screen.height").toString());

        this.manage().window().setSize(new Dimension(width, height));
    }

    public Actions actions () {
        return new Actions(driver);
    }

}
