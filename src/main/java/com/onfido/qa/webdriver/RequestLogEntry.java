package com.onfido.qa.webdriver;

import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;

public class RequestLogEntry {
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

    @Override
    public String toString() {
        return request.getMethod() + " " + request.getUrl() + ":" + response.getStatus();
    }
}
