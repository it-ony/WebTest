package com.onfido.qa;

import com.onfido.qa.github.GithubMainPage;
import com.onfido.qa.webdriver.WebTest;
import org.testng.annotations.Test;

public class GithubIT extends WebTest {
    @Test
    public void testGithubCodeSearch() {
        driver().get("https://github.com/");

        verifyPage(GithubMainPage.class)
                .search("it-ony/webtest")
                .clickFirstResult();
    }
}