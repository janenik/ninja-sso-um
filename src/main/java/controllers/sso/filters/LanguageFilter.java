package controllers.sso.filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.i18n.Lang;

import javax.inject.Inject;

/**
 * Language filter. Extracts language from request and places it into attributes of request context.
 */
public class LanguageFilter implements Filter {

    /**
     * Language parameter name.
     */
    public static final String LANG = "lang";

    /**
     * Default language.
     */
    public static final String DEFAULT_LANGUAGE = "en";

    /**
     * Language.
     */
    @Inject
    Lang lang;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        String langStr = context.getParameter(LANG, DEFAULT_LANGUAGE);
        if (!lang.isLanguageDirectlySupportedByThisApplication(langStr)) {
            langStr = DEFAULT_LANGUAGE;
        }
        context.setAttribute(LANG, langStr);
        Result result = filterChain.next(context);
        lang.setLanguage(langStr, result);
        return result;
    }
}
