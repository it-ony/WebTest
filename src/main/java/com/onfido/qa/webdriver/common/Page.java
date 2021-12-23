package com.onfido.qa.webdriver.common;

import com.onfido.qa.webdriver.Driver;

public abstract class Page extends Component {

    protected Page(Driver driver) {
        super(driver);

        verifyPage(driver);
    }

    protected void verifyPage(Driver driver) {
    }

}
