package models.sso;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link PaginationResult}.
 */
public class PaginationResultTest {

    @Test
    public void testManyPages() {
        List<PaginationResult.Item> paginationItems = PaginationResult.buildPaginationItems(10, 100);
        assertEquals(createPaginationItems(1, 2, null, 8, 9, 10, 11, 12, null, 99, 100), paginationItems);
    }

    @Test
    public void testManyPages_start() {
        List<PaginationResult.Item> paginationItems = PaginationResult.buildPaginationItems(1, 100);
        assertEquals("Starts with 1, 2, 3", createPaginationItems(1, 2, 3, null, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(2, 100);
        assertEquals("Starts with 1, 2, 3, 4", createPaginationItems(1, 2, 3, 4, null, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(3, 100);
        assertEquals("Starts with 1, 2, 3, 4, 5",
                createPaginationItems(1, 2, 3, 4, 5, null, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(4, 100);
        assertEquals("Starts with 1, 2, 3, 4, 5, 6",
                createPaginationItems(1, 2, 3, 4, 5, 6, null, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(5, 100);
        assertEquals("Starts with 1, 2, 3, 4, 5, 6, 7, ...",
                createPaginationItems(1, 2, 3, 4, 5, 6, 7, null, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(6, 100);
        assertEquals("Starts with 1, 2, ..., 4, 5, 6, 7, 8, ...",
                createPaginationItems(1, 2, null, 4, 5, 6, 7, 8, null, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(7, 100);
        assertEquals("Starts with 1, 2, ..., 5, 6, 7, 8, 9, ...",
                createPaginationItems(1, 2, null, 5, 6, 7, 8, 9, null, 99, 100), paginationItems);
    }

    @Test
    public void testManyPages_end() {
        List<PaginationResult.Item> paginationItems = PaginationResult.buildPaginationItems(100, 100);
        assertEquals("Ends with 98, 99, 100", createPaginationItems(1, 2, null, 98, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(99, 100);
        assertEquals("Ends with 97, 98, 99, 100",
                createPaginationItems(1, 2, null, 97, 98, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(98, 100);
        assertEquals("Ends with 96, 97, 98, 99, 100",
                createPaginationItems(1, 2, null, 96, 97, 98, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(97, 100);
        assertEquals("Ends with 95, 96, 97, 98, 99, 100",
                createPaginationItems(1, 2, null, 95, 96, 97, 98, 99, 100), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(96, 100);
        assertEquals("Ends with 97, 98, 99, 100",
                createPaginationItems(1, 2, null, 94, 95, 96, 97, 98, 99, 100), paginationItems);


        paginationItems = PaginationResult.buildPaginationItems(95, 100);
        assertEquals("Ends with ..., 99, 100",
                createPaginationItems(1, 2, null, 93, 94, 95, 96, 97, null, 99, 100), paginationItems);
    }

    @Test
    public void testFewPages() {
        List<PaginationResult.Item> paginationItems = PaginationResult.buildPaginationItems(1, 1);
        assertEquals("Contains: 1", createPaginationItems(1), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(1, 2);
        assertEquals("Contains: 1, 2", createPaginationItems(1, 2), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(2, 3);
        assertEquals("Contains: 1, 2, 3", createPaginationItems(1, 2, 3), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(3, 4);
        assertEquals("Contains: 1, 2, 3, 4", createPaginationItems(1, 2, 3, 4), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(5, 5);
        assertEquals("Contains: 1, 2, 3, 4, 5", createPaginationItems(1, 2, 3, 4, 5), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(5, 6);
        assertEquals("Contains: 1, 2, 3, 4, 5, 6", createPaginationItems(1, 2, 3, 4, 5, 6), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(5, 7);
        assertEquals("Contains: 1, 2, 3, 4, 5, 6, 7",
                createPaginationItems(1, 2, 3, 4, 5, 6, 7), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(5, 8);
        assertEquals("Contains: 1, 2, 3, 4, 5, 6, 7, 8",
                createPaginationItems(1, 2, 3, 4, 5, 6, 7, 8), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(5, 9);
        assertEquals("Contains: 1, 2, 3, 4, (5), 6, 7, 8, 9",
                createPaginationItems(1, 2, 3, 4, 5, 6, 7, 8, 9), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(4, 9);
        assertEquals("Contains: 1, 2, 3, (4), 5, 6, ..., 9",
                createPaginationItems(1, 2, 3, 4, 5, 6, null, 8, 9), paginationItems);

        paginationItems = PaginationResult.buildPaginationItems(6, 9);
        assertEquals("Contains: 1, 2, 3, 4, 5, (6), 7, 8, 9",
                createPaginationItems(1, 2, null, 4, 5, 6, 7, 8, 9), paginationItems);
    }

    /**
     * Builds pagination items list from given pages, treating null as a separator.
     *
     * @param pages Pages including null for separator.
     * @return List of pagination items.
     */
    private static List<PaginationResult.Item> createPaginationItems(Integer... pages) {
        List<PaginationResult.Item> expectedItems = new ArrayList<>(pages.length);
        for (Integer page : pages) {
            if (page == null) {
                expectedItems.add(PaginationResult.Item.SEPARATOR);
            } else {
                expectedItems.add(new PaginationResult.Item(page.longValue()));
            }
        }
        return expectedItems;
    }
}
