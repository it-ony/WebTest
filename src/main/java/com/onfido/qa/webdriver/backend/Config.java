package com.onfido.qa.webdriver.backend;

import com.onfido.qa.annotation.Mobile;

public class Config {

    boolean requiresMobileDevice;
    boolean enableMicrophoneCameraAccess;
    boolean acceptInsecureCertificates;

    String fileForFakeAudioCapture;

    String fileForFakeVideoCapture;
    Mobile mobileDevice;

    public Config mobile(Mobile mobile) {

        requiresMobileDevice = true;
        this.mobileDevice = mobile;

        return this;
    }

    public boolean allowMobileEmulation() {
        return this.mobileDevice != null && this.mobileDevice.allowEmulation();
    }

    public Config withEnableMicrophoneCameraAccess(boolean enableMicrophoneCameraAccess) {
        this.enableMicrophoneCameraAccess = enableMicrophoneCameraAccess;
        return this;
    }

    public Config withFileForFakeAudioCapture(String fileForFakeAudioCapture) {
        this.fileForFakeAudioCapture = fileForFakeAudioCapture;
        return this;
    }

    public Config withFileForFakeVideoCapture(String fileForFakeVideoCapture) {
        this.fileForFakeVideoCapture = fileForFakeVideoCapture;
        return this;
    }

    public Config withAcceptInsecureCertificates(boolean acceptInsecureCertificates) {
        this.acceptInsecureCertificates = acceptInsecureCertificates;
        return this;
    }
}
