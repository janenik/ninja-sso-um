package controllers.sso.filters;

import controllers.sso.web.Controllers;
import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;

import javax.inject.Singleton;

/**
 * JSON/JSONP filter wraps API calls and returns error if exception happens inside the controller.
 */
@Singleton
public class ApplicationErrorJsonOrJsonpFilter implements Filter {

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        try {
            return filterChain.next(context);
        } catch (Exception e) {
            return Controllers.jsonOrJsonPBadRequest("error", e, context);
        }
    }
}
