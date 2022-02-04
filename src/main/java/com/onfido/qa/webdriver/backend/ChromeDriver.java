package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ChromeDriver implements DriverServiceFactory {

    @Override
    public DriverService createDriverService(Properties properties, boolean forceLog) {

        var browserPath = properties.getProperty("browserPath");

        var builder = new ChromeDriverService
                .Builder()
                .usingAnyFreePort();

        if (forceLog) {
            var dir = new File("target/chromedriver");
            if (!dir.mkdirs()) {
                throw new RuntimeException("Cannot create logfile directory " + dir.getAbsolutePath());
            }

            builder.withVerbose(true)
                   .withLogLevel(ChromeDriverLogLevel.DEBUG)
                   .withLogFile(new File(dir, UUID.randomUUID() + ".log"));
        }

        if (!isEmpty(browserPath)) {
            builder.usingDriverExecutable(new File(browserPath));
        }

        return builder.build();

    }
}
