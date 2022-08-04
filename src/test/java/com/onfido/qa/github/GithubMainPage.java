package com.onfido.qa.github;

import com.onfido.qa.webdriver.Driver;
import com.onfido.qa.webdriver.common.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class GithubMainPage extends Page {

    public static final By SEARCH = By.cssSelector(".header-search-input");

    public GithubMainPage(Driver driver) {
        super(driver);
    }

    public GithubSearchPage search(String searchTerm) {
        driver.waitFor.clickable(SEARCH).sendKeys(searchTerm + Keys.ENTER);
        return new GithubSearchPage(driver);
    }

    @Override
    protected void verifyPage(Driver driver) {
        super.verifyPage(driver);

        driver.waitFor.visibility(SEARCH);
    }
}