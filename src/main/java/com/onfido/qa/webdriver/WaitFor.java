package com.onfido.qa.webdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitFor {

    private static final Logger log = LoggerFactory.getLogger(WaitFor.class);

    private final RemoteWebDriver driver;
    private int timeout = 10;

    public WaitFor(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public WebElement clickable(By by) {
        waitFor(ExpectedConditions.elementToBeClickable(by));
        return driver.findElement(by);
    }

    public WebElement clickable(WebElement element) {
        waitFor(ExpectedConditions.elementToBeClickable(element));
        return element;
    }

    public WebElement visibility(By by) {
        waitFor(ExpectedConditions.visibilityOfElementLocated(by));
        return driver.findElement(by);
    }

    public WebElement visibility(WebElement element) {
        waitFor(ExpectedConditions.visibilityOf(element));
        return element;
    }

    public WebElement presence(By by) {
        waitFor(ExpectedConditions.presenceOfElementLocated(by));
        return driver.findElement(by);
    }


    public void invisible(By by) {
        waitFor(ExpectedConditions.invisibilityOfElementLocated(by));
    }

    public WaitFor timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public void waitFor(ExpectedCondition condition) {
        log.info("[ThreadId {}][SessionId '{}']. Waiting for {}. Timeout after {} seconds",
                Thread.currentThread().getId(), driver.getSessionId(), condition, timeout);

        new WebDriverWait(driver, timeout).until(condition);
    }

}
