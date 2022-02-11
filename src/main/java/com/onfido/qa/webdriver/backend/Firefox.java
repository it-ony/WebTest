package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.util.Properties;

public class Firefox implements BrowserFactory {
    @Override
    public RemoteWebDriver createDriver(DriverService service, Capabilities capabilities) {
        return new RemoteWebDriver(service.getUrl(), capabilities);
    }

    @Override
    public MutableCapabilities getOptions(DesiredCapabilities capabilities, Config config, Properties properties) {

        var options = new FirefoxOptions(capabilities);
        options.setAcceptInsecureCerts(config.acceptInsecureCertificates);

        if (config.enableMicrophoneCameraAccess) {
            options.addPreference("media.navigator.permission.disabled", true);
            options.addPreference("permissions.default.microphone", 1);
            options.addPreference("permissions.default.camera", 1);
        }

        options.setHeadless(Boolean.parseBoolean(properties.getProperty("headless", "false")));

        return options;

    }
}
