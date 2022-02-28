package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
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

        var options = new FirefoxOptions();

        options.setAcceptInsecureCerts(config.acceptInsecureCertificates);

        if (config.enableMicrophoneCameraAccess) {
            options.addPreference("media.navigator.streams.fake", true);
            options.addPreference("media.navigator.permission.disabled", true);
        }

        var headless = Boolean.parseBoolean(properties.getProperty("headless", "false"));
        options.setHeadless(headless);

        if (!headless) {
            return options;
        }

        // do not copy logging preferences, if running in headless mode: https://github.com/SeleniumHQ/selenium/issues/10349
        capabilities.getCapabilityNames().stream().filter(x -> {
            return !CapabilityType.LOGGING_PREFS.equals(x);
        }).forEach(x -> {
            options.setCapability(x, capabilities.getCapability(x));
        });

        return options;

    }


}
