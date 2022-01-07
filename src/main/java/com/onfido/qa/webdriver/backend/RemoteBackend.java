package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class RemoteBackend extends Backend {

    private static final Logger log = LoggerFactory.getLogger(RemoteBackend.class);


    public RemoteBackend(DesiredCapabilities capabilities, Properties properties) throws Exception {
        super(capabilities, properties);
    }

    @Override
    protected RemoteWebDriver createDriver(DesiredCapabilities capabilities, Properties properties) throws Exception {

        var hubUrl = Objects.requireNonNull(properties.getProperty("gridUrl"));


        var runOnCI = Boolean.parseBoolean(System.getenv().getOrDefault("CI", "false"));
        if (!runOnCI) {
            log.info("Connected to grid: '{}' with the following capabilities {}", hubUrl, capabilities);
        }

        RemoteWebDriver driver = Objects.requireNonNull(getRemoteWebDriver(capabilities, new URL(hubUrl)));
        driver.setFileDetector(new LocalFileDetector());

        return driver;
    }

    private RemoteWebDriver getRemoteWebDriver(DesiredCapabilities capabilities, URL hubUrl) {
        for (int i = 0; i < 3; i++) {
            try {
                return new RemoteWebDriver(hubUrl, capabilities);
            } catch (Exception e) {
                log.warn("Cannot create session", e);
            }
        }

        throw new RuntimeException("Unable to create remote webdriver");
    }

}
