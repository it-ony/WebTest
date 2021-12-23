package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class LocalBackend extends Backend {

    private ChromeDriverService service;

    public LocalBackend(DesiredCapabilities capabilities, Properties properties) throws Exception {
        super(capabilities, properties);
    }

    protected RemoteWebDriver createDriver(DesiredCapabilities capabilities, Properties properties) throws IOException {


        // TODO: make the local backend available for all browsers

        var browserPath = properties.getProperty("browserPath");

        capabilities.setCapability("chrome.binary", browserPath);

        service = new ChromeDriverService
                .Builder()
                .usingDriverExecutable(new File(browserPath))
                .usingAnyFreePort()
                .build();

        service.start();

        return new ChromeDriver(service, new ChromeOptions().merge(capabilities));

    }

    @Override
    public void quit() {
        try {
            super.quit();
        } finally {
            if (service != null) {
                service.stop();
            }
        }

    }
}
