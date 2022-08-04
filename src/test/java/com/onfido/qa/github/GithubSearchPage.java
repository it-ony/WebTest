package com.onfido.qa.github;


import com.onfido.qa.webdriver.Driver;
import com.onfido.qa.webdriver.common.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.ByChained;

public class GithubSearchPage extends Page {

    public static final By CODE_SEARCH_RESULTS = By.cssSelector(".codesearch-results");

    public GithubSearchPage(Driver driver) {
        super(driver);
    }

    @Override
    protected void verifyPage(Driver driver) {
        super.verifyPage(driver);

        driver.waitFor.visibility(CODE_SEARCH_RESULTS);
    }

    public void clickFirstResult() {
        driver.waitFor.clickable(new ByChained(CODE_SEARCH_RESULTS, By.cssSelector("li a"))).click();
    }
}
