package controllers.sso.filters;

import com.google.common.base.Strings;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;
import ninja.Results;
import ninja.utils.NinjaProperties;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Application exception filter to show error page in case of exception.
 */
public class ApplicationErrorHtmlFilter implements Filter {

    /**
     * Properties.
     */
    @Inject
    NinjaProperties properties;

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        try {
            return filterChain.next(context);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return Results.internalServerError().html().template("/views/error.ftl.html").
                    render("config", properties).
                    render("error", e).
                    render("errorTitle", 
                            Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getName() : e.getMessage()).
                    render("errorStackTrace", sw.toString()).
                    render("isRuntime", e instanceof RuntimeException);
        }
    }
}
