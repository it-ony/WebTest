package com.onfido.qa.webdriver.support;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class Conditions {

    public static ExpectedCondition<Boolean> documentIsReady() {
        return driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
    }
}
