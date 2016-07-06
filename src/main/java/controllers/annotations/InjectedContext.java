package controllers.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Injected context annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface InjectedContext {
}
