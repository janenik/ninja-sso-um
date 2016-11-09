package controllers.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Application token append policy.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface ApplicationPolicy {
}
