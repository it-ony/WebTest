package com.onfido.qa.webdriver.backend;

import com.onfido.qa.annotation.Mobile;

public class Config {

    boolean requiresMobileDevice;
    boolean fakeDeviceForMediaStream;
    boolean fakeUiForMediaStream;

    Mobile mobileDevice;

    public Config mobile(Mobile mobile) {

        requiresMobileDevice = true;
        this.mobileDevice = mobile;

        return this;
    }

    public Config withFakeDeviceForMediaStream(boolean fakeDeviceForMediaStream) {
        this.fakeDeviceForMediaStream = fakeDeviceForMediaStream;
        return this;
    }

    public Config withFakeUiForMediaStream(boolean fakeUiForMediaStream) {
        this.fakeUiForMediaStream = fakeUiForMediaStream;
        return this;
    }
}
