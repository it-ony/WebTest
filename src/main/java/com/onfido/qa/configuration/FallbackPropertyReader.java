package com.onfido.qa.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

public class FallbackPropertyReader {
    private static final Logger log = LoggerFactory.getLogger(FallbackPropertyReader.class);

    private Properties loadPropertiesFromResource(String resourcePath) {
        try {

            //noinspection NestedTryStatement
            try (var resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath)) {

                var properties = new Properties();

                if (resourceAsStream != null) {
                    properties.load(resourceAsStream);
                }
                return properties;
            }

        } catch (IOException e) {
            log.info("Property File {} could not be loaded. Skipping.", resourcePath);
            return new Properties();
        }
    }

    @SuppressWarnings("AccessOfSystemProperties")
    public Properties resolveProperties() {

        var defaultEnvironment = "DEV";
        var defaultRegion = "EU";

        var properties = new Properties();
        properties.putAll(System.getProperties());

        var localProperties = this.loadPropertiesFromResource("local.properties");

        var environment = System.getProperty("environment", localProperties.getProperty("region", defaultEnvironment));
        var region = System.getProperty("region", localProperties.getProperty("region", defaultRegion));

        Stream.of("local", "ENVIRONMENT-REGION", "REGION", "ENVIRONMENT", "common").map((key) -> {
            return key.replace("ENVIRONMENT", environment).replace("REGION", region);
        }).map((key) -> {
            return key + ".properties";
        }).map(this::loadPropertiesFromResource).forEach((p) -> {
            p.forEach(properties::putIfAbsent);
        });
        return properties;
    }
}