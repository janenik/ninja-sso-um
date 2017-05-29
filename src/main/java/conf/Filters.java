package conf;

import controllers.sso.filters.ClearEntityManagerFilter;
import ninja.Filter;
import ninja.application.ApplicationFilters;

import java.util.List;

/**
 * Global filters for the application.
 */
public class Filters implements ApplicationFilters {

    @Override
    public void addFilters(List<Class<? extends Filter>> filters) {
        filters.add(ClearEntityManagerFilter.class);
    }
}
