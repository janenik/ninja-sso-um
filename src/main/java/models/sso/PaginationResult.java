package models.sso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pagination result for object retrieved from database.
 *
 * @param <T> Type of the object.
 */
public class PaginationResult<T> {

    /**
     * List of object on the current page.
     */
    private final List<T> objects;

    /**
     * Total number of the results.
     */
    private final long totalObjects;

    /**
     * Current page number. 1 based.
     */
    private final long currentPage;

    /**
     * Objects per page requested.
     */
    private final long objectsPerPage;

    /**
     * Total number of pages.
     */
    private final long totalPages;

    /**
     * Contains pagination items to display.
     */
    private final List<Item> items;

    /**
     * Constructs pagination result.
     *
     * @param objects Objects for current page.
     * @param totalObjects Total number of objects in database.
     * @param currentPage Current page number. 1 based.
     * @param objectsPerPage Objects per page requested.
     */
    public PaginationResult(List<T> objects, long totalObjects, int currentPage, int objectsPerPage) {
        this.objects = Collections.unmodifiableList(objects);
        this.totalObjects = totalObjects;
        this.totalPages = totalObjects % objectsPerPage > 0 ?
                totalObjects / objectsPerPage + 1 :
                totalObjects / objectsPerPage;
        this.currentPage = currentPage;
        this.objectsPerPage = objectsPerPage;
        this.items = buildPaginationItems(currentPage, this.totalPages);
    }

    /**
     * Constructs empty pagination result.
     *
     * @param objectsPerPage Objects per page requested.
     */
    public PaginationResult(int objectsPerPage) {
        this(Collections.emptyList(), 0, 1, objectsPerPage);
    }

    /**
     * Returns objects for current page.
     *
     * @return Objects for current page.
     */
    public List<T> getObjects() {
        return objects;
    }

    /**
     * Checks if the current pagination result has objects.
     *
     * @return Whether the current pagination result has objects.
     */
    public boolean hasObjects() {
        return objects != null && !objects.isEmpty();
    }

    /**
     * Returns total number of items in result set.
     *
     * @return Total number of items in result set.
     */
    public long getTotalObjects() {
        return totalObjects;
    }

    /**
     * Returns current page. 1 based.
     *
     * @return CurrentPage.
     */
    public long getCurrentPage() {
        return currentPage;
    }

    /**
     * Returns objects per page.
     *
     * @return Objects per page.
     */
    public long getObjectsPerPage() {
        return objectsPerPage;
    }

    /**
     * Returns number of items before this page.
     *
     * @return Number of items before this page.
     */
    public long getOffset() {
        return (currentPage - 1L) * objectsPerPage;
    }

    /**
     * Returns total pages.
     *
     * @return Total pages.
     */
    public long getTotalPages() {
        return totalPages;
    }

    /**
     * Returns an unmodifiable list with pagination items (pages).
     *
     * @return Pagination items.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Returns a list of pages for result set like (1,2,...,5,6,7,8,9,...,17,18)
     * where 7 is the current page.
     *
     * @param pages Number of all pages.
     * @return List of pages with separator inside.
     */
    static List<Item> buildPaginationItems(long currentPage, long pages) {
        if (pages < 0) {
            throw new IllegalArgumentException("Total number of pages is expected to be 0 or positive.");
        }
        if (currentPage <= 0L) {
            currentPage = 1L;
        }
        if (currentPage > pages) {
            currentPage = pages;
        }
        List<Item> items = new ArrayList<>(11);
        if (currentPage > 5) {
            items.add(new Item(1L));
            items.add(new Item(2L));
            items.add(Item.SEPARATOR);
            items.add(new Item(currentPage - 2L));
            items.add(new Item(currentPage - 1L));
        } else {
            for (long page = 1; page < currentPage; page++) {
                items.add(new Item(page));
            }
        }
        items.add(new Item(currentPage));
        if (currentPage <= pages - 5) {
            items.add(new Item(currentPage + 1));
            items.add(new Item(currentPage + 2));
            items.add(Item.SEPARATOR);
            items.add(new Item(pages - 1));
            items.add(new Item(pages));
        } else {
            for (long page = currentPage + 1L; page <= pages; page++) {
                items.add(new Item(page));
            }
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Pagination item that represents a page or a page range separator.
     */
    public static class Item {

        /**
         * Separator.
         */
        static final Item SEPARATOR = new Item();

        /**
         * Current page. Null means separator.
         */
        private final Long page;

        /**
         * Constructs separator pagination item.
         */
        public Item() {
            this.page = null;
        }

        /**
         * Constructs pagination item with page.
         *
         * @param page Page.
         */
        public Item(Long page) {
            this.page = page;
        }

        /**
         * Whether the current item is a separator.
         *
         * @return Whether the current item is a separator.
         */
        public boolean isSeparator() {
            return page == null;
        }

        /**
         * Whether the current item is a page.
         *
         * @return Whether the current item is a page.
         */
        public boolean isPage() {
            return page != null;
        }

        /**
         * Returns current page or throws {@link IllegalStateException} if the current item is a separator.
         *
         * @return Current page.
         */
        public long getPage() {
            if (page == null) {
                throw new IllegalStateException("Pagination item is a separator.  " +
                        "Check it with #isPage() or #isSeparator().");
            }
            return page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Item that = (Item) o;

            return page != null ? page.equals(that.page) : that.page == null;

        }

        @Override
        public int hashCode() {
            return page != null ? page.hashCode() : 0;
        }

        @Override
        public String toString() {
            return page != null ? page.toString() : "...";
        }
    }
}
