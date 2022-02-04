package com.onfido.qa.webdriver.backend;

import org.openqa.selenium.remote.service.DriverService;

import java.util.Properties;

public interface DriverServiceFactory {

    DriverService createDriverService(Properties properties, boolean forceLog);

}
