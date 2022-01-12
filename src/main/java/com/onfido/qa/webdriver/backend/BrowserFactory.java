package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

public interface BrowserFactory {
    RemoteWebDriver createDriver(DriverService service, Capabilities capabilities);

    MutableCapabilities getOptions(DesiredCapabilities capabilities, Config config);
}
