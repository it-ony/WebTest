package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.remote.service.DriverService;

import java.util.Properties;

public interface DriverServiceFactory {

    abstract DriverService createDriverService(Properties properties);

}
