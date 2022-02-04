package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.util.Properties;

public class BrowserFactoryBase implements BrowserFactory {
    @Override
    public RemoteWebDriver createDriver(DriverService service, Capabilities capabilities) {
        return null;
    }

    @Override
    public MutableCapabilities getOptions(DesiredCapabilities capabilities, Config config, Properties properties) {

        if (config.acceptInsecureCertificates) {
            capabilities.acceptInsecureCerts();
        }

        return capabilities;
    }
}
