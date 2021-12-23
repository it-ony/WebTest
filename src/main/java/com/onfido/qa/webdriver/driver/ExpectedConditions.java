package com.onfido.qa.webdriver.driver;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;

public final class ExpectedConditions {

    private ExpectedConditions() {
    }

    public static ExpectedCondition pageReady() {
        return o -> ((JavascriptExecutor) o).executeScript("return document.readyState").equals("complete");
    }
}
