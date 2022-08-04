package com.onfido.qa.webdriver.backend;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

public class ChromeDriver implements DriverServiceFactory {

    @Override
    public DriverService createDriverService(Properties properties, boolean forceLog) {
        WebDriverManager.chromedriver().setup();

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

        return builder.build();
    }
}
