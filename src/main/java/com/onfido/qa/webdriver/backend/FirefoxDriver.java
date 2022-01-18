package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.util.Properties;

public class FirefoxDriver implements DriverServiceFactory {
    @Override
    public DriverService createDriverService(Properties properties) {

        return new GeckoDriverService.Builder()
                .usingAnyFreePort()
                .build();
    }
}
