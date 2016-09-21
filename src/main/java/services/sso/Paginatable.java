package services.sso;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import models.sso.PaginationResult;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pagination interface for all services that wish to perform paginated search queries.
 */
public interface Paginatable<E> {

    /**
     * Returns name of the named query to count all entities. This is used when text query is empty.
     *
     * @return Named query to count all entities.
     */
    String getEntityCountAllQueryName();

    /**
     * Returns name of the named query to fetch all entities. This is used when text query is empty.
     * Only one page is fetched (this is a query where offset and limit are applied).
     *
     * @return Named query to fetch all entities.
     */
    String getEntityAllQueryName();

    /**
     * Returns name of the named query to count entities filtered by text query.
     *
     * @return Named query to count entities filtered by text query.
     */
    String getEntityCountSearchQueryName();

    /**
     * Returns name of the named query to fetch entities filtered by text query.
     * Only one page is fetched (this is a query where offset and limit are applied).
     *
     * @return Named query to fetch entities filtered by text query.
     */
    String getEntitySearchQueryName();

    /**
     * Entity manager provider.
     *
     * @return Entity manager provider.
     */
    Provider<EntityManager> getEntityManagerProvider();

    /**
     * Returns text query parameter name.
     *
     * @return Text query parameter name.
     */
    default String getTextQueryParameterName() {
        return "query";
    }

    /**
     * Searches an entity table for a given text query. Performs count query first and then fetches the current
     * page of entities.
     *
     * @param query Query to search for.
     * @param currentPage Current page number (starts with 1).
     * @param entitiesPerPage Number of entities per page.
     * @return Pagination result for current parameters.
     */
    default PaginationResult<E> search(String query, int currentPage, int entitiesPerPage) {
        return search(query, Collections.emptyMap(), currentPage, entitiesPerPage);
    }

    /**
     * Searches an entity table for a given text query. Performs count query first and then fetches the current
     * page of entities.
     *
     * @param query Query to search for.
     * @param additionalParameters Additional named query parameters.
     * @param currentPage Current page number (starts with 1).
     * @param entitiesPerPage Number of entities per page.
     * @return Pagination result for current parameters.
     */
    @SuppressWarnings("unchecked")
    default PaginationResult<E> search(
            String query,
            Map<String, Object> additionalParameters,
            int currentPage,
            int entitiesPerPage) {
        Preconditions.checkArgument(currentPage >= 1 || entitiesPerPage >= 1,
                "Current page and objects per page must be positive.");
        Preconditions.checkArgument(entitiesPerPage > 0, "Objects per page must be positive.");

        // Clean up the query.
        query = Strings.nullToEmpty(query).replace('%', ' ').trim();
        boolean all = query.isEmpty();

        // By default query becomes 'something%' string which benefits from indexed fields (starts with string).
        query += '%';

        // If the query starts with '*' character then treat every '*' character as a SQL wildcard '%'.
        // This allows '%something%something2%' queries but these queries are significantly slower (contains string).
        if ('*' == query.charAt(0)) {
            query = query.replace('*', '%');
        }

        EntityManager em = getEntityManagerProvider().get();
        Query namedQuery;

        // Construct count query.
        if (all) {
            namedQuery = em.createNamedQuery(getEntityCountAllQueryName());
        } else {
            namedQuery = em.createNamedQuery(getEntityCountSearchQueryName())
                    .setParameter(getTextQueryParameterName(), query);
        }
        for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
            namedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        // Request count.
        long totalObjects = (Long) (namedQuery.getSingleResult());
        // No need to fetch if nothing found.
        if (totalObjects == 0L) {
            return new PaginationResult<>(entitiesPerPage);
        }

        // Construct fetch current page of entries query.
        if (all) {
            namedQuery = em.createNamedQuery(getEntityAllQueryName());
        } else {
            namedQuery = em.createNamedQuery(getEntitySearchQueryName())
                    .setParameter(getTextQueryParameterName(), query);
        }
        for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
            namedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        // Set offset/limit.
        namedQuery
                .setFirstResult((currentPage - 1) * entitiesPerPage)
                .setMaxResults(entitiesPerPage);

        // Fetch the results and populate pagination result.
        List<E> users = (List<E>) namedQuery.getResultList();
        return new PaginationResult<>(users, totalObjects, currentPage, entitiesPerPage);
    }
}


