package com.onfido.qa.webdriver.backend;

import com.onfido.qa.webdriver.Driver;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import static net.lightbody.bmp.proxy.CaptureType.REQUEST_CONTENT;
import static net.lightbody.bmp.proxy.CaptureType.REQUEST_COOKIES;
import static net.lightbody.bmp.proxy.CaptureType.REQUEST_HEADERS;
import static net.lightbody.bmp.proxy.CaptureType.RESPONSE_CONTENT;
import static net.lightbody.bmp.proxy.CaptureType.RESPONSE_COOKIES;
import static net.lightbody.bmp.proxy.CaptureType.RESPONSE_HEADERS;

public abstract class Backend implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(Backend.class);
    private final Properties properties;

    protected Driver driver;
    protected BrowserMobProxyServer proxyServer;

    // TODO: add proxy support

    protected Backend(DesiredCapabilities capabilities, Properties properties) throws Exception {
        this.properties = properties;

        if (Boolean.parseBoolean(properties.getProperty("useBrowserMobProxy"))) {
            addBrowserMobProxy(capabilities);
        }

        //noinspection AbstractMethodCallInConstructor, OverridableMethodCallDuringObjectConstruction, OverriddenMethodCallDuringObjectConstruction
        driver = new Driver(createDriver(capabilities, properties));
    }

    public Driver getDriver() {
        return driver;
    }

    public BrowserMobProxyServer getProxyServer() {
        return proxyServer;
    }

    protected abstract RemoteWebDriver createDriver(DesiredCapabilities capabilities, Properties properties) throws Exception;
    
    public void quit() {
        if (driver != null) {
            log.info("Quitting driver {}", System.identityHashCode(driver));
            driver.quit();
        }

        if (proxyServer != null) {
            proxyServer.stop();
        }
    }

    private void addBrowserMobProxy(DesiredCapabilities capability) {

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

    @Override
    public void close() {
        quit();
    }
}
