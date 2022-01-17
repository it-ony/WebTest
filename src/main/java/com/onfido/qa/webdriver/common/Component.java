package com.onfido.qa.webdriver.common;

import com.onfido.qa.webdriver.Driver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public abstract class Component {

    protected final Driver driver;
    private static final Logger log = LoggerFactory.getLogger(Component.class);

    protected Component(Driver driver) {
        this.driver = driver;
    }

    protected void executeWithinFrame(By frame, FrameExecutorVoid runnable) throws Exception {
        executeWithinFrame(frame, (FrameExecutor<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    protected <T> T executeWithinFrame(By frame, FrameExecutor<T> runnable) throws Exception {

        try {
            var element = driver.waitFor().clickable(frame);
            log.debug("Trying to switch to frame with src '{}'", element.getAttribute("src"));
            driver.switchTo().frame(element);
            log.debug("Switched to frame with url '{}'", driver.executeScript("return location.href"));
            return runnable.run();
        } finally {
            log.info("Switching back to the default Content frame");
            driver.switchTo().defaultContent();
        }

    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected WebElement scrollIntoView(By by) {
        return scrollIntoView(driver.findElement(by));
    }

    protected WebElement scrollIntoView(WebElement element) {
        driver.executeScript("arguments[0].scrollIntoView();", element);
        return element;
    }

    protected WebElement clear(By by) {
        var webElement = driver.waitFor().clickable(by);
        webElement.clear();
        return webElement;
    }

    protected WebElement clear(By by, String value) {
        var webElement = clear(by);
        webElement.sendKeys(value);
        return webElement;
    }

    protected WebElement clear(WebElement webElement, String value) {
        webElement.clear();
        webElement.sendKeys(value);
        return webElement;
    }


    protected String value(By by) {
        return driver.findElement(by).getAttribute("value");
    }

    protected WebElement click(By by) {
        var webElement = driver.waitFor().clickable(by);
        webElement.click();
        return webElement;
    }

    protected WebElement click(WebElement element) {
        driver.waitFor().clickable(element).click();
        return element;
    }

    protected WebElement input(By by, String value) {
        var element = driver.waitFor().clickable(by);

        element.clear();
        element.sendKeys(value);
        return element;
    }

    @FunctionalInterface
    public interface FrameExecutor<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    public interface FrameExecutorVoid {
        void run() throws Exception;
    }

    protected <T extends Component> T createComponent(Class<T> tClass) {
        return createComponent(driver, tClass);
    }

    public static <T> T createComponent(Driver driver, Class<T> tClass) {

        if (tClass == null) {
            return null;
        }

        try {
            return tClass.getConstructor(Driver.class).newInstance(driver);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            log.error("Constructor for passing Driver not found. Does {} has a public constructor?", tClass.getCanonicalName());
            throw new RuntimeException(e);
        }
    }

    protected String text(By selector) {
        return driver.findElement(selector).getText();
    }
}
