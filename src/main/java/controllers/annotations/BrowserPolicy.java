package controllers.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Browser token append policy.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface BrowserPolicy {
}
