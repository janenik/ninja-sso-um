package controllers.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for result with secure headers.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface SecureHtmlHeaders {
}
