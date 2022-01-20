package com.onfido.qa.webdriver;


import com.onfido.qa.annotation.Browser;
import com.onfido.qa.annotation.Mobile;
import com.onfido.qa.webdriver.backend.Backend;
import com.onfido.qa.webdriver.backend.Config;
import net.lightbody.bmp.BrowserMobProxyServer;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.openqa.selenium.logging.LogType.CLIENT;
import static org.openqa.selenium.logging.LogType.DRIVER;
import static org.openqa.selenium.logging.LogType.PERFORMANCE;
import static org.openqa.selenium.logging.LogType.PROFILER;
import static org.openqa.selenium.logging.LogType.SERVER;

public abstract class WebTest {

    private static final InheritableThreadLocal<Backend> threadBackend = new InheritableThreadLocal<>();

    private static final Logger log = LoggerFactory.getLogger(WebTest.class);


    /**
     * @return d the Webdriver instance
     */
    public static Driver d() {
        var backend = threadBackend.get();

        if (backend == null) {
            return null;
        }

        return backend.getDriver();
    }

    public static BrowserMobProxyServer proxy() {
        var backend = threadBackend.get();

        if (backend == null) {
            return null;
        }

        return backend.getProxyServer();
    }

    protected abstract Properties properties();

    protected abstract void extendCapabilities(DesiredCapabilities capabilities);

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method, ITestContext context) throws Exception {

        try {
            var properties = this.properties();

            var capabilities = createCapabilitiesFromProperties(properties);
            var browserAnnotations = getBrowserAnnotations(method);

            var config = createConfiguration(method, browserAnnotations);
            var optionalBrowser = browserAnnotations.stream().filter(x -> x.platform() != Platform.ANY).findFirst();

            if (optionalBrowser.isPresent()) {
                capabilities.setPlatform(optionalBrowser.get().platform());
            }

            capabilities.setCapability("name", String.format("%s::%s", method.getDeclaringClass().getSimpleName(), method.getName()));

            extendCapabilities(capabilities);

            var backend = new Backend(capabilities, properties, config);

            threadBackend.set(backend);

            setWindowSize(backend, properties.getProperty("windowSize"));

        } catch (Exception t) {
            log.error("[Thread {}]. Error in beforeMethod for Test '{}.{}'", Thread.currentThread().getId(), method.getDeclaringClass()
                                                                                                                   .getSimpleName(), method.getName());
            throw t;
        }
    }

    private Config createConfiguration(Method method, List<Browser> annotations) {

        var config = new Config();

        if (method.isAnnotationPresent(Mobile.class)) {
            config.mobile(method.getAnnotation(Mobile.class));
        }

        config.withEnableMicrophoneCameraAccess(annotations.stream().anyMatch(Browser::enableMicrophoneCameraAccess));
        config.withFileForFakeAudioCapture(annotations.stream().map(Browser::fileForFakeAudioCapture)
                                                      .filter(cs -> !StringUtils.isEmpty(cs))
                                                      .findFirst().orElse(null));

        config.withFileForFakeVideoCapture(annotations.stream().map(Browser::fileForFakeVideoCapture)
                                                      .filter(cs -> !StringUtils.isEmpty(cs))
                                                      .findFirst().orElse(null));

        config.withAcceptInsecureCertificates(annotations.stream().anyMatch(Browser::acceptInsureCertificates));

        return config;
    }

    private List<Browser> getBrowserAnnotations(Method method) {
        List<Browser> annotations = new ArrayList<>();
        annotations.addAll(Arrays.asList(method.getAnnotationsByType(Browser.class)));
        annotations.addAll(getAnnotations(method.getDeclaringClass(), Browser.class));
        return annotations;
    }

    private <T extends Annotation> List<T> getAnnotations(Class<?> type, Class<T> annotationClass) {

        var annotations = new ArrayList<Annotation>();

        while (type != null) {
            annotations.addAll(Arrays.asList(type.getAnnotationsByType(annotationClass)));
            type = type.getSuperclass();
        }

        //noinspection unchecked
        return (List<T>) annotations;
    }


    private DesiredCapabilities createCapabilitiesFromProperties(Properties properties) {
        var capabilities = new DesiredCapabilities();

        capabilities.setBrowserName(properties.getProperty("browser"));
        capabilities.setPlatform(Platform.fromString(properties.getProperty("platform", "ANY")));
        capabilities.setCapability(CapabilityType.BROWSER_VERSION, properties.getProperty("browserVersion"));

        setupLoggingPreferences(properties, capabilities);

        return capabilities;
    }

    private void setupLoggingPreferences(Properties properties, DesiredCapabilities capabilities) {
        var loggingPreferences = new LoggingPreferences();
        var logTypes = Arrays.asList(BROWSER, CLIENT, DRIVER, PERFORMANCE, PROFILER, SERVER);

        logTypes.forEach(logType -> {
            loggingPreferences.enable(logType, Level.parse(properties.getProperty("log." + logType, "ALL")));
        });

        capabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
    }

    private void setWindowSize(Backend backend, String windowSize) {

        if (StringUtils.isEmpty(windowSize)) {
            return;
        }

        var split = windowSize.split(",");
        var dimensions = new Dimension(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

        backend.getDriver().manage().window().setSize(dimensions);

    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp(Method method, ITestResult result) {
        var backend = threadBackend.get();

        if (backend == null) {
            return;
        }

        var className = method.getDeclaringClass().getSimpleName();
        var methodName = method.getName();

        try {
            var driver = backend.getDriver();
            var sessionId = driver.getSessionId();
            var threadId = Thread.currentThread().getId();

            log.debug("[Thread {}][SessionId '{}']. Start cleanup for test {}.{}", threadId, sessionId, className, methodName);
            backend.quit();
            log.debug("[Thread {}][SessionId '{}']. Finished cleanup for test {}.{}", threadId, sessionId, className, methodName);
        } finally {
            threadBackend.set(null);
        }

    }


}
