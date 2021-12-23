package com.onfido.qa.configuration;

import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;

public class TemplateProperties extends Properties {
    public static final Pattern TOKEN_MATCHER = Pattern.compile("[$][{]([^}]+)[}]");

    public TemplateProperties() {
    }

    public TemplateProperties(Properties defaults) {
        super(defaults);
    }

    public String getProperty(String key) {
        return this.evaluate(key, new ArrayList<>(), key);
    }

    protected String evaluate(String key, ArrayList<String> lookup, String property) {
        if (lookup.contains(key)) {
            throw new RuntimeException(String.format("Circular key lookup. Couldn't retrieve value for key '%s' to evaluate '%s'.", key, property));
        } else {
            var value = super.getProperty(key);
            if (value == null) {
                return null;
            } else {
                var matcher = TOKEN_MATCHER.matcher(value);
                var sb = new StringBuffer();
                lookup.add(key);

                String evaluate = "";
                while (matcher.find()) {
                    matcher.appendReplacement(sb, evaluate);
                    var token = matcher.group(1);
                    evaluate = this.evaluate(token, lookup, property);
                    if (evaluate == null) {
                        evaluate = "";
                    }
                }

                matcher.appendTail(sb);
                lookup.remove(key);
                return sb.toString();
            }
        }
    }
}
