package controllers.annotations;

import com.google.inject.BindingAnnotation;
import ninja.Context;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Injected context annotation since Ninja framework (as for 5.8.x) doesn't allow to inject {@link Context}.
 * This can be useful when context is needed in web request scoped beans. For example, in case of this application
 * injected context is need in URL builder class.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface InjectedContext {
}
