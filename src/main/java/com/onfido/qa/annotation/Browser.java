package com.onfido.qa.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Browser {

    boolean enableMicrophoneCameraAccess() default false;

    String fileForFakeAudioCapture() default "";
    String fileForFakeVideoCapture() default "";

}
