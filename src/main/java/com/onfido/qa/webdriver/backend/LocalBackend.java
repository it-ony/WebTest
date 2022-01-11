package com.onfido.qa.webdriver.backend;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class LocalBackend extends Backend {

    private DriverService service;

    private static final Map<String, BrowserFactory> browserFactories = new HashMap<>();

    static {
        browserFactories.put("chrome", new Chrome());
    }

    public LocalBackend(DesiredCapabilities capabilities, Properties properties, Config config) throws Exception {
        super(capabilities, properties, config);
    }

    protected RemoteWebDriver createDriver(DesiredCapabilities capabilities, Properties properties, Config config) throws IOException {
        // TODO: make the local backend available for all browsers

        String browserName = getBrowser(capabilities).toLowerCase(Locale.ROOT);

        if (!browserFactories.containsKey(browserName)) {
            throw new RuntimeException(String.format("Cannot create browser '%s' for local testing. Implementation missing.", browserName));
        }

        var factory = browserFactories.get(browserName);

        service = factory.createDriverService(properties);
        service.start();

        return factory.createDriver(service, capabilities, properties, config);

    }

    private String getBrowser(DesiredCapabilities capabilities) {
        var browserName = capabilities.getBrowserName();

        if (StringUtils.isEmpty(browserName)) {
            return "chrome";
        }

        return browserName;
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

    protected static abstract class BrowserFactory {

        abstract DriverService createDriverService(Properties properties);

        abstract RemoteWebDriver createDriver(DriverService service, DesiredCapabilities capabilities, Properties properties, Config config);
    }

    protected static class Chrome extends BrowserFactory {

        public static final int DEVICE_WIDTH = 360;
        public static final int DEVICE_HEIGHT = 640;
        public static final float DEVICE_PIXEL_RATIO = 3.0F;

        @Override
        DriverService createDriverService(Properties properties) {

            var browserPath = properties.getProperty("browserPath");

            var builder = new ChromeDriverService
                    .Builder()
                    .usingAnyFreePort();

            if (!StringUtils.isEmpty(browserPath)) {
                builder.usingDriverExecutable(new File(browserPath));
            }

            return builder.build();

        }

        @Override
        RemoteWebDriver createDriver(DriverService service,
                                     DesiredCapabilities capabilities,
                                     Properties properties,
                                     Config config) {

            var chromeOptions = new ChromeOptions();

            if (config.fakeDeviceForMediaStream) {
                chromeOptions.addArguments("use-fake-device-for-media-stream");
            }

            if (config.fakeUiForMediaStream) {
                chromeOptions.addArguments("use-fake-ui-for-media-stream");
            }

            if (config.requiresMobileDevice) {
                setupMobileDevice(chromeOptions, config);
            }

            return new ChromeDriver((ChromeDriverService) service, chromeOptions.merge(capabilities));
        }

        protected void setupMobileDevice(ChromeOptions chromeOptions, Config config) {
            var mobile = config.mobileDevice;

            var deviceMetrics = new HashMap<String, Object>();
            deviceMetrics.put("width", mobile.width() == 0 ? DEVICE_WIDTH : mobile.width());
            deviceMetrics.put("height", mobile.height() == 0 ? DEVICE_HEIGHT : mobile.height());
            deviceMetrics.put("pixelRatio", mobile.pixelRation() == 0 ? DEVICE_PIXEL_RATIO : mobile.pixelRation());

            var mobileEmulation = new HashMap<String, Object>();
            mobileEmulation.put("deviceMetrics", deviceMetrics);
            mobileEmulation.put("userAgent", "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");

            chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        }
    }
}
