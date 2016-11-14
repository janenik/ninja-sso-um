package services.sso.annotations;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A set of exclusion substrings that are not allowed in user names, etc.
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface ExclusionSubstrings {
}
