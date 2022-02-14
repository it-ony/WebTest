package com.onfido.qa.webdriver.backend;

import com.browserstack.local.Local;
import com.onfido.qa.webdriver.Driver;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static java.lang.Boolean.parseBoolean;
import static java.util.Objects.requireNonNull;
import static net.lightbody.bmp.proxy.CaptureType.REQUEST_CONTENT;
import static net.lightbody.bmp.proxy.CaptureType.REQUEST_COOKIES;
import static net.lightbody.bmp.proxy.CaptureType.REQUEST_HEADERS;
import static net.lightbody.bmp.proxy.CaptureType.RESPONSE_CONTENT;
import static net.lightbody.bmp.proxy.CaptureType.RESPONSE_COOKIES;
import static net.lightbody.bmp.proxy.CaptureType.RESPONSE_HEADERS;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@SuppressWarnings("StaticCollection")
public class Backend implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(Backend.class);
    public static final String DEFAULT_BROWSER = "chrome";

    protected Driver driver;
    protected DriverService service = null;
    protected Local local;
    protected BrowserMobProxyServer proxyServer;

    private static final Map<String, BrowserFactory> BROWSER_FACTORIES = new HashMap<>();
    private static final Map<String, DriverServiceFactory> DRIVER_SERVICE_FACTORY = new HashMap<>();
    public static final BrowserFactoryBase BROWSER_FACTORY_BASE = new BrowserFactoryBase();

    static {
        BROWSER_FACTORIES.put("chrome", new Chrome());
        BROWSER_FACTORIES.put("firefox", new Firefox());

        DRIVER_SERVICE_FACTORY.put("chrome", new ChromeDriver());
        DRIVER_SERVICE_FACTORY.put("firefox", new FirefoxDriver());
    }

    public Backend(DesiredCapabilities capabilities, Properties properties, Config config) throws Exception {

        setupProxy(capabilities, properties);
        var runLocal = Boolean.parseBoolean(properties.getProperty("local", "false"));
        var browser = getBrowser(capabilities);

        if (runLocal) {
            var message = String.format("Cannot create browser '%s' for local testing. Implementation missing.", browser);

            var driverServiceFactory = requireNonNull(DRIVER_SERVICE_FACTORY.get(browser), message);
            service = startDriverService(properties, driverServiceFactory);
        }

        BrowserFactory factory = BROWSER_FACTORIES.getOrDefault(browser, BROWSER_FACTORY_BASE);

        var realCapabilities = factory.getOptions(capabilities, config, properties);

        if (runLocal) {
            driver = new Driver(requireNonNull(factory).createDriver(service, realCapabilities));
        } else {
            driver = new Driver(createRemoteDriver(realCapabilities, properties, config));
        }

    }

    private DriverService startDriverService(Properties properties, DriverServiceFactory driverServiceFactory) throws IOException {

        WebDriverException exception = null;
        var maxRetries = Integer.parseInt(properties.getProperty("driver.maxRetries", "2"));

        for (int i = 0; i < maxRetries; i++) {
            try {
                var service = driverServiceFactory.createDriverService(properties, i >= 1);
                service.start();

                return service;
            } catch (WebDriverException e) {
                log.warn("Exception while connecting to grid", e);
                exception = e;
            }
        }

        throw Objects.requireNonNull(exception);


    }

    @SuppressWarnings("MethodWithMoreThanThreeNegations")
    private RemoteWebDriver createRemoteDriver(MutableCapabilities capabilities, Properties properties, Config config) throws Exception {

        var browserStackHub = setupBrowserStack(properties, capabilities);
        var gridUrl = properties.getProperty("gridUrl");

        var isBrowserStack = !isEmpty(browserStackHub);

        if (isBrowserStack && !isEmpty(gridUrl)) {
            throw new InvalidArgumentException("both, browserstack and grid urls are defined");
        }

        if (config.requiresMobileDevice && isBrowserStack) {

            var device = properties.getProperty("mobile.device", config.mobileDevice.device());
            var osVersion = properties.getProperty("mobile.osVersion", config.mobileDevice.osVersion());

            if (!isEmpty(device)) {
                capabilities.setCapability("device", device);
                capabilities.setCapability("real_mobile", "true");
            }

            if (!isEmpty(osVersion)) {
                capabilities.setCapability("os_version", osVersion);
            }

        }

        var hubUrl = requireNonNull(Optional.ofNullable(browserStackHub).orElse(gridUrl));

        logGrid(capabilities, hubUrl);

        var maxRetries = Integer.parseInt(properties.getProperty("grid.maxRetries", "1"));

        RemoteWebDriver driver = getRemoteWebDriver(capabilities, hubUrl, maxRetries);
        driver.setFileDetector(new LocalFileDetector());

        return driver;
    }

    private RemoteWebDriver getRemoteWebDriver(MutableCapabilities capabilities, String hubUrl, int maxRetries) throws MalformedURLException {

        SessionNotCreatedException exception = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                return new RemoteWebDriver(new URL(hubUrl), capabilities);
            } catch (SessionNotCreatedException e) {
                log.warn("Exception while connecting to grid", e);
                exception = e;
            }
        }

        throw Objects.requireNonNull(exception);

    }

    private void logGrid(MutableCapabilities capabilities, String hubUrl) {
        if (!System.getenv().containsKey("CI")) {
            log.info("Connected to grid: '{}' with the following capabilities {}", hubUrl, capabilities);
        }
    }

    @SuppressWarnings("CallToSystemGetenv")
    private String setupBrowserStack(Properties properties, MutableCapabilities capabilities) throws Exception {

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

    private String getBrowser(DesiredCapabilities capabilities) {
        var browserName = capabilities.getBrowserName().toLowerCase(Locale.ROOT);

        if (isEmpty(browserName)) {
            return DEFAULT_BROWSER;
        }

        return browserName;
    }

    public Driver getDriver() {
        return driver;
    }

    public BrowserMobProxyServer getProxyServer() {
        return proxyServer;
    }

    private void setupProxy(DesiredCapabilities capability, Properties properties) {

        if (!Boolean.parseBoolean(properties.getProperty("useBrowserMobProxy", "false"))) {
            return;
        }

        proxyServer = new BrowserMobProxyServer();
        proxyServer.setTrustAllServers(true);
        proxyServer.enableHarCaptureTypes(REQUEST_HEADERS, REQUEST_COOKIES, REQUEST_CONTENT, RESPONSE_HEADERS, RESPONSE_COOKIES, RESPONSE_CONTENT);
        proxyServer.newHar();
        proxyServer.start();

        var proxy = ClientUtil.createSeleniumProxy(proxyServer, getLocalAddress());

        capability.setCapability(CapabilityType.PROXY, proxy);
        log.info("Added proxy on port: {} with http proxy settings {}", proxyServer.getPort(), proxy.getHttpProxy());

    }

    private InetAddress getLocalAddress() {
        try {
            return InetAddress.getByAddress(InetAddress.getLocalHost().getAddress());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("MethodWithMoreThanThreeNegations")
    public void quit() {
        var exceptions = new ArrayList<Exception>();

        if (driver != null) {
            log.debug("Quitting driver {}", System.identityHashCode(driver));
            try {
                driver.quit();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if (proxyServer != null) {
            log.debug("Stopping proxy server");
            try {
                proxyServer.stop();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if (service != null) {
            log.debug("Stopping driver service.");
            try {
                service.stop();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if (local != null) {
            log.debug("Stop browserstack local");
            try {
                local.stop();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if (!exceptions.isEmpty()) {
            throw new RuntimeException(exceptions.get(0));
        }

    }

    @Override
    public void close() {
        quit();
    }

}
