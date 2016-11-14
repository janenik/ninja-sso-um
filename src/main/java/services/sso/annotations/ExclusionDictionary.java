package services.sso.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A set of exclusion keywords that are not allowed in usernames, etc.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface ExclusionDictionary {
}
