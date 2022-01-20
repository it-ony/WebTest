package com.onfido.qa.webdriver.listener;


import com.onfido.qa.webdriver.Driver;
import com.onfido.qa.webdriver.WebTestBase;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;
import org.apache.commons.io.FileUtils;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;


@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unchecked", "AccessOfSystemProperties", "HardcodedLineSeparator"})
public class ScreenshotListener extends TestListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotListener.class);
    private static final String PATHNAME = "target/screenShot";

    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);

        if (!isEnabled()) {
            return;
        }

        try {

            createDirectoryForReportsIfDoesNotExist(PATHNAME);
            var testResultPath = getTestResultPath(result);

            var driver = getRemoteWebDriver();

            createContentReport(driver.driver, testResultPath);
            createScreenshotReport(driver.driver, result, testResultPath);
            createProxyReport(testResultPath);

        } catch (Exception e) {
            log.error("Exception in the Screenshot Listener: ", e);
        }

    }

    private boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty("screenshotListener.enabled", "true"));
    }

    @Override
    public void onFinish(ITestContext testContext) {
        super.onFinish(testContext);

        if (isEnabled() && testContext.getFailedTests().size() > 0) {
            log.error("\n\nFind your webdriver reports under {}\n\n", new File(PATHNAME).getAbsoluteFile());

        }

    }

    private Driver getRemoteWebDriver() {
        try {
            return WebTestBase.d();
        } catch (Exception e) {
            log.error("Error getting the driver", e);
            throw e;
        }

    }

    private void createScreenshotReport(org.openqa.selenium.remote.RemoteWebDriver driver, ITestResult result, String testResultPath) {

        var model = JtwigModel.newModel();

        model.with("testMethod", result.getMethod().getMethodName())
             .with("testClass", result.getMethod().getRealClass())
             .with("component", System.getProperty("component", ""))
             .with("testParameter", result.getParameters())
             .with("throwableMessage", getThrowableMessage(result))
             .with("stackTrace", getStackTrace(result))
             .with("capabilities", getCapabilities(driver))
             .with("requests", getRequests());

        if (driver != null) {

            model.with("sessionId", driver.getSessionId())
                 .with("currentUrl", driver.getCurrentUrl())
                 .with("screenshot", getScreenshot(driver))
                 .with("logs", getLogEntries(driver));

        }

        try {
            FileUtils.writeStringToFile(new File(testResultPath + "_report.html"),
                    JtwigTemplate.classpathTemplate("ScreenshotReportTemplate.twig")
                                 .render(model));
        } catch (IOException e) {
            log.error("File report with screenshot could not be created: ", e);
        }
    }

    private Object getCapabilities(RemoteWebDriver driver) {

        try {
            return new Capabilities(driver.getCapabilities());
        } catch (Exception e) {
            log.error("Error reading capabilities", e);
        }

        return null;
    }

    private Object getLogEntries(RemoteWebDriver driver) {

        var logs = driver.manage().logs();

        return logs.getAvailableLogTypes()
                   .stream()
                   .filter(t -> !t.equals(LogType.SERVER))
                   .map(logType ->
                           new BrowserLog(logType, logs.get(logType)
                                                       .getAll()
                                                       .stream()
                                                       .map(log -> new BrowserLogEntry(log, logType))
                                                       .collect(Collectors.toList())))
                   .collect(Collectors.toList());


    }

    private Object getRequests() {
        var proxy = WebTestBase.proxy();

        if (proxy == null) {
            return null;
        }

        return proxy.getHar()
                    .getLog()
                    .getEntries()
                    .stream()
                    .map(RequestLogEntry::new)
                    .collect(Collectors.toList());

    }

    private StackTraceElement[] getStackTrace(ITestResult result) {
        return result.getThrowable().getStackTrace();
    }

    private String getScreenshot(RemoteWebDriver driver) {
        return driver.getScreenshotAs(OutputType.BASE64);
    }

    private String getThrowableMessage(ITestResult result) {

        var throwable = result.getThrowable();
        if (throwable == null) {
            return null;
        }

        return throwable.toString();
    }

    private void createDirectoryForReportsIfDoesNotExist(String pathname) {
        var mainDir = new File(pathname);
        if (!mainDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            mainDir.mkdirs();
        }
    }

    private void createProxyReport(String testResultPath) {
        var proxy = WebTestBase.proxy();

        var filterHar = Boolean.parseBoolean(System.getProperty("screenshotListener.filterHar", "true"));

        if (proxy != null) {
            try {
                var har = proxy.getHar();
                har.getLog().getEntries()
                   .removeIf(x -> {

                       if (!filterHar) {
                           return false;
                       }

                       var contentTypeHeader = x.getResponse()
                                                .getHeaders()
                                                .stream()
                                                .filter(y -> y.getName().equalsIgnoreCase("content-type"))
                                                .findFirst();

                       var success = x.getResponse().getStatus() / 100 == 2;
                       var isGet = x.getRequest().getMethod().equalsIgnoreCase("GET");
                       var isJson = contentTypeHeader.isPresent() && contentTypeHeader.get().getName().toLowerCase().contains("json");

                       return isGet && success && !isJson;
                       
                   });
                har.writeTo(new File(testResultPath + "_proxy.har"));
            } catch (IOException e) {
                log.error("File proxy.har could not be created: ", e);
            }
        }
    }

    private void createContentReport(org.openqa.selenium.remote.RemoteWebDriver driver, String testResultPath) {

        if (driver == null) {
            return;
        }

        try {
            var source = driver.getPageSource();

            if (!source.contains("<base")) {
                // insert a base tag to mark the relative urls pointing to the correct server

                var baseUrl = driver.getCurrentUrl().replaceFirst("\\?.*", "");
                source = source.replace("<head>", String.format("<head><base href=\"%s\" />", baseUrl));
            }

            source = source.replaceAll("<script[\\s\\S]*?</script>", "");

            FileUtils.writeStringToFile(new File(testResultPath + "_content.html"), source);
        } catch (IOException e) {
            log.error("File with the page content could not be created: ", e);
        }
    }

    private String getTestResultPath(ITestResult result) {

        var testName = result.getMethod().getMethodName();
        var realClass = result.getMethod().getRealClass();
        var parameters = Arrays.toString(result.getParameters());

        var parametersFileName = parameters.replaceAll("[\\\\/]*", "");
        if (parameters.length() > 60) {
            parametersFileName = parametersFileName.substring(0, 60);
        }

        return String.format("%s/%s_%s%s", PATHNAME, realClass.getSimpleName(), testName, parametersFileName);

    }

    private static class RequestLogEntry {
        public HarRequest request;
        public HarResponse response;

        public RequestLogEntry(HarEntry entry) {
            request = entry.getRequest();
            response = entry.getResponse();
        }

        @SuppressWarnings("unused")
        public boolean success() {
            return response.getStatus() / 100 == 2;
        }

        public String url() {
            return request.getUrl();
        }

    }

    @SuppressWarnings("unused")
    private static class BrowserLogEntry {
        private final LogEntry logEntry;

        public BrowserLogEntry(LogEntry logEntry, String logType) {
            this.logEntry = logEntry;
        }

        public boolean success() {
            return !logEntry.getLevel().equals(Level.SEVERE);
        }

        public Date date() {
            return new Date(logEntry.getTimestamp());
        }

        public String level() {
            return logEntry.getLevel().getName();
        }

        public String message() {
            return logEntry.getMessage();
        }

    }

    private static class BrowserLog {
        public final String type;
        public final List<BrowserLogEntry> logEntries;

        public BrowserLog(String type, List<BrowserLogEntry> logEntries) {
            this.type = type;
            this.logEntries = logEntries;
        }

        @SuppressWarnings("unused")
        public boolean hasEntries() {
            return logEntries.size() > 0;
        }
    }

    @SuppressWarnings("unused")
    private static class Capabilities {

        private final org.openqa.selenium.Capabilities capabilities;

        public Capabilities(org.openqa.selenium.Capabilities capabilities) {
            this.capabilities = capabilities;
        }

        public String browser() {
            return this.capabilities.getBrowserName();
        }

        public String version() {
            return this.capabilities.getVersion();
        }

        public String platform() {
            return this.capabilities.getPlatform().toString();
        }

        public Object[] capabilities() {
            return capabilities.asMap().entrySet().stream().map(e -> new Object[] {e.getKey(), e.getValue()}).toArray();
        }
    }

}