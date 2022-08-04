package com.onfido.qa.webdriver.backend;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class FirefoxDriver implements DriverServiceFactory {
    @Override
    public DriverService createDriverService(Properties properties, boolean forceLog) {
        WebDriverManager.firefoxdriver().setup();

        var builder = new GeckoDriverService.Builder()
                .usingAnyFreePort();

        return builder.build();
    }
}
