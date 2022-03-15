package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class FirefoxDriver implements DriverServiceFactory {
    @Override
    public DriverService createDriverService(Properties properties, boolean forceLog) {

        var builder = new GeckoDriverService.Builder()
                .usingAnyFreePort();

        var browserPath = properties.getProperty("geckoDriverPath");

        if (!isEmpty(browserPath)) {
            builder.usingDriverExecutable(new File(browserPath));
        }

        return builder.build();
    }
}
