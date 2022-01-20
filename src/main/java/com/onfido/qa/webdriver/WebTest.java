package com.onfido.qa.webdriver;


import com.onfido.qa.configuration.Property;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Properties;

public class WebTest extends WebTestBase {


    @Override
    protected Properties properties() {
        return Property.properties();
    }

    @Override
    protected DesiredCapabilities extendCapabilities(DesiredCapabilities capabilities) {
        return capabilities;
    }

}
