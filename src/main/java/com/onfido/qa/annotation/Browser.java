package com.onfido.qa.annotation;

import org.openqa.selenium.Platform;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Browser {

    Platform platform() default Platform.ANY;

    boolean enableMicrophoneCameraAccess() default false;

    String fileForFakeAudioCapture() default "";
    String fileForFakeVideoCapture() default "";

    boolean acceptInsureCertificates() default false;
}
