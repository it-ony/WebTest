package com.onfido.qa.configuration;


import java.util.Properties;

@SuppressWarnings("StaticCollection")
public final class Property {

    private static final Properties properties;
    private static final TemplateProperties templateProperties;

    static {
        properties = new FallbackPropertyReader().resolveProperties();
        templateProperties = new TemplateProperties(properties);
    }

    private Property() {
    }

    public static Properties properties() {
        return properties;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        return get(key, defaultValue, true);
    }

    public static String raw(String key, String defaultValue) {
        return get(key, defaultValue, false);
    }

    private static String get(String key, String defaultValue, boolean resolve) {
        return (resolve ? templateProperties : properties).getProperty(key, defaultValue);
    }

}
