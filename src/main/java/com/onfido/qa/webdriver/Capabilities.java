package com.onfido.qa.webdriver;

public class Capabilities {

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
        return capabilities.asMap().entrySet().stream().map(e -> new Object[]{e.getKey(), e.getValue()}).toArray();
    }
}
