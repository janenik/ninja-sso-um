package controllers.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A list of allowed continuer URLs.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface AllowedContinueUrls {
}
