package services.sso.annotations.entitiestopreload;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Countries to preload.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface PreloadedCountries {
}

