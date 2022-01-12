package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ChromeDriver implements DriverServiceFactory {

    @Override
    public DriverService createDriverService(Properties properties) {

        var browserPath = properties.getProperty("browserPath");

        var builder = new ChromeDriverService
                .Builder()
                .usingAnyFreePort();

        if (!isEmpty(browserPath)) {
            builder.usingDriverExecutable(new File(browserPath));
        }

        return builder.build();

    }
}
