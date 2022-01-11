package com.onfido.qa.webdriver.backend;

import com.browserstack.local.Local;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static java.lang.Boolean.parseBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class RemoteBackend extends Backend {

    private static final Logger log = LoggerFactory.getLogger(RemoteBackend.class);
    private Local local;

    public RemoteBackend(DesiredCapabilities capabilities, Properties properties, Config config) throws Exception {
        super(capabilities, properties, config);
    }

    @Override
    protected RemoteWebDriver createDriver(DesiredCapabilities capabilities, Properties properties, Config config) throws Exception {

        var browserStackHub = setupBrowserStack(properties, capabilities);
        var gridUrl = properties.getProperty("gridUrl");

        if (!StringUtils.isEmpty(browserStackHub) && !StringUtils.isEmpty(gridUrl)) {
            throw new InvalidArgumentException("both, browserstack and grid urls are defined");
        }

        if (config.requiresMobileDevice) {
            if (browserStackHub != null) {
                // running against browser stack
                capabilities.setCapability("device", Optional.ofNullable(config.mobileDevice.device()).orElse(properties.getProperty("device", "Pixel 3a")));
            } else {
                throw new RuntimeException("I don't know how to bootstrap the mobile device");
            }
        }

        var hubUrl = Objects.requireNonNull(Optional.ofNullable(browserStackHub).orElse(gridUrl));
        
        logGrid(capabilities, hubUrl);

        var driver = new RemoteWebDriver(new URL(hubUrl), capabilities);
        driver.setFileDetector(new LocalFileDetector());

        return driver;
    }

    @SuppressWarnings("CallToSystemGetenv")
    private String setupBrowserStack(Properties properties, DesiredCapabilities capabilities) throws Exception {

        var username = properties.getProperty("browserstack.username", System.getenv("BROWSERSTACK_USERNAME"));
        var accessKey = properties.getProperty("browserstack.accessKey", System.getenv("BROWSERSTACK_ACCESS_KEY"));

        var enableLocalTesting = parseBoolean(Optional.ofNullable(properties.getProperty("browserstack.local", System.getenv("BROWSERSTACK_LOCAL")))
                                                      .orElse("false"));

        if (isEmpty(username) || isEmpty(accessKey)) {
            return null;
        }

        var server = properties.getProperty("browserstack.server", "hub-cloud.browserstack.com");
        var hubUrl = String.format("https://%s:%s@%s/wd/hub", username, accessKey, server);

        log.info("Composing browserstack url based on parameters. Server: {}", server);

        if (enableLocalTesting) {

            var id = UUID.randomUUID().toString();
            capabilities.setCapability("browserstack.localIdentifier", id);

            var map = new HashMap<String, String>();
            map.put("key", accessKey);
            map.put("localIdentifier", id);

            local = new Local();
            local.start(map);
        }

        return hubUrl;

    }

    private void logGrid(DesiredCapabilities capabilities, String hubUrl) {

        if (!System.getenv().containsKey("CI")) {
            log.info("Connected to grid: '{}' with the following capabilities {}", hubUrl, capabilities);
        }

    }

    @Override
    public void quit() {
        super.quit();

        if (local != null) {
            try {
                local.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
