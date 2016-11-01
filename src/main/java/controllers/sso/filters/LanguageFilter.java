package controllers.sso.filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.i18n.Lang;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Language filter. Extracts language from request (URL parameter 'lang') and places it into attributes of request
 * context.
 */
@Singleton
public class LanguageFilter implements Filter {

    /**
     * Language parameter name.
     */
    public static final String LANG = "lang";

    /**
     * All languages parameter name.
     */
    public static final String LANGUAGES = "languages";

    /**
     * Default language.
     */
    public static final String DEFAULT_LANGUAGE = "en";

    /**
     * Language.
     */
    final Lang lang;

    /**
     * Application properties.
     */
    final NinjaProperties properties;

    /**
     * Mapped languages.
     */
    final Map<String, String> mappedLanguages;

    /**
     * Constructs language filter.
     *
     * @param lang Language holder for current request.
     * @param properties Application properties.
     */
    @Inject
    public LanguageFilter(Lang lang, NinjaProperties properties) {
        String[] languages = properties.getStringArray("application.languages");
        String[] languageTitles = properties.getStringArray("application.languageTitles");
        if (languages.length < 1 || languages.length > languageTitles.length) {
            throw new IllegalStateException("Number of supported languages must be positive.  "
                    + "Number of language titles must be equal or greater than number of languages.");
        }
        Map<String, String> mapping = new LinkedHashMap<>();
        for (int index = 0; index < languages.length; index++) {
            mapping.put(languages[index], languageTitles[index]);
        }
        this.lang = lang;
        this.properties = properties;
        this.mappedLanguages = Collections.unmodifiableMap(mapping);
    }

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        String langStr = context.getParameter(LANG, DEFAULT_LANGUAGE);
        if (!lang.isLanguageDirectlySupportedByThisApplication(langStr)) {
            langStr = DEFAULT_LANGUAGE;
        }
        context.setAttribute(LANG, langStr);
        context.setAttribute(LANGUAGES, mappedLanguages);
        Result result = filterChain.next(context);
        lang.setLanguage(langStr, result);
        return result;
    }
}
