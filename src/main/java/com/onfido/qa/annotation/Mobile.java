package com.onfido.qa.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 * annotation, that the test needs a mobile device or mobile emulation
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Mobile {

    boolean allowEmulation() default true;

    int width() default 0;
    int height() default  0;
    int pixelRation() default 0;

    String device () default "";
    String osVersion() default "";
}
