package controllers.sso.filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.i18n.Lang;

import javax.inject.Inject;

/**
 * Language filter. Extracts language from request and places it into attributes.
 */
public class LanguageFilter implements Filter {

    /**
     * Language parameter name.
     */
    public static final String LANG = "lang";

    @Inject
    Lang lang;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        String langStr = context.getParameter(LANG, "en");
        if (!lang.isLanguageDirectlySupportedByThisApplication(langStr)) {
            langStr = "en";
        }
        context.setAttribute(LANG, langStr);
        Result result = filterChain.next(context);
        lang.setLanguage(langStr, result);
        return result;
    }
}
