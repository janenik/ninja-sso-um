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
        List<PaginationResult.Item> paginationItems = PaginationResult.getPaginationItems(10L, 100L);
        assertEquals(createPaginationItems(1L, 2L, null, 8L, 9L, 10L, 11L, 12L, null, 99L, 100L), paginationItems);
    }

    @Test
    public void testManyPages_start() {
        List<PaginationResult.Item> paginationItems = PaginationResult.getPaginationItems(1L, 100L);
        assertEquals("Starts with 1, 2, 3", createPaginationItems(1L, 2L, 3L, null, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(2L, 100L);
        assertEquals("Starts with 1, 2, 3, 4", createPaginationItems(1L, 2L, 3L, 4L, null, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(3L, 100L);
        assertEquals("Starts with 1, 2, 3, 4, 5",
                createPaginationItems(1L, 2L, 3L, 4L, 5L, null, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(4L, 100L);
        assertEquals("Starts with 1, 2, 3, 4, 5, 6",
                createPaginationItems(1L, 2L, 3L, 4L, 5L, 6L, null, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(5L, 100L);
        assertEquals("Starts with 1, 2, 3, 4, 5, 6, 7, ...",
                createPaginationItems(1L, 2L, 3L, 4L, 5L, 6L, 7L, null, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(6L, 100L);
        assertEquals("Starts with 1, 2, ..., 4, 5, 6, 7, 8, ...",
                createPaginationItems(1L, 2L, null, 4L, 5L, 6L, 7L, 8L, null, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(7L, 100L);
        assertEquals("Starts with 1, 2, ..., 5, 6, 7, 8, 9, ...",
                createPaginationItems(1L, 2L, null, 5L, 6L, 7L, 8L, 9L, null, 99L, 100L), paginationItems);
    }

    @Test
    public void testManyPages_end() {
        List<PaginationResult.Item> paginationItems = PaginationResult.getPaginationItems(100L, 100L);
        assertEquals("Ends with 98, 99, 100", createPaginationItems(1L, 2L, null, 98L, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(99L, 100L);
        assertEquals("Ends with 97, 98, 99, 100",
                createPaginationItems(1L, 2L, null, 97L, 98L, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(98L, 100L);
        assertEquals("Ends with 96, 97, 98, 99, 100",
                createPaginationItems(1L, 2L, null, 96L, 97L, 98L, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(97L, 100L);
        assertEquals("Ends with 95, 96, 97, 98, 99, 100",
                createPaginationItems(1L, 2L, null, 95L, 96L, 97L, 98L, 99L, 100L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(96L, 100L);
        assertEquals("Ends with 97, 98, 99, 100",
                createPaginationItems(1L, 2L, null, 94L, 95L, 96L, 97L, 98L, 99L, 100L), paginationItems);


        paginationItems = PaginationResult.getPaginationItems(95L, 100L);
        assertEquals("Ends with ..., 99, 100",
                createPaginationItems(1L, 2L, null, 93L, 94L, 95L, 96L, 97L, null, 99L, 100L), paginationItems);
    }

    @Test
    public void testFewPages() {
        List<PaginationResult.Item> paginationItems = PaginationResult.getPaginationItems(1L, 1L);
        assertEquals("Contains: 1", createPaginationItems(1L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(1L, 2L);
        assertEquals("Contains: 1, 2", createPaginationItems(1L, 2L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(2L, 3L);
        assertEquals("Contains: 1, 2, 3", createPaginationItems(1L, 2L, 3L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(3L, 4L);
        assertEquals("Contains: 1, 2, 3, 4", createPaginationItems(1L, 2L, 3L, 4L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(5L, 5L);
        assertEquals("Contains: 1, 2, 3, 4, 5", createPaginationItems(1L, 2L, 3L, 4L, 5L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(5L, 6L);
        assertEquals("Contains: 1, 2, 3, 4, 5, 6", createPaginationItems(1L, 2L, 3L, 4L, 5L, 6L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(5L, 7L);
        assertEquals("Contains: 1, 2, 3, 4, 5, 6, 7",
                createPaginationItems(1L, 2L, 3L, 4L, 5L, 6L, 7L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(5L, 8L);
        assertEquals("Contains: 1, 2, 3, 4, 5, 6, 7, 8",
                createPaginationItems(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(5L, 9L);
        assertEquals("Contains: 1, 2, 3, 4, (5), 6, 7, 8, 9",
                createPaginationItems(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(4L, 9L);
        assertEquals("Contains: 1, 2, 3, (4), 5, 6, ..., 9",
                createPaginationItems(1L, 2L, 3L, 4L, 5L, 6L, null, 8L,  9L), paginationItems);

        paginationItems = PaginationResult.getPaginationItems(6L, 9L);
        assertEquals("Contains: 1, 2, 3, 4, 5, (6), 7, 8, 9",
                createPaginationItems(1L, 2L, null, 4L, 5L, 6L, 7L, 8L, 9L), paginationItems);
    }

    /**
     * Builds pagination items list from given pages, treating null as a separator.
     *
     * @param pages Pages including null for separator.
     * @return List of pagination items.
     */
    private static List<PaginationResult.Item> createPaginationItems(Long... pages) {
        List<PaginationResult.Item> expectedItems = new ArrayList<>(pages.length);
        for (Long page : pages) {
            if (page == null) {
                expectedItems.add(PaginationResult.Item.SEPARATOR);
            } else {
                expectedItems.add(new PaginationResult.Item(page));
            }
        }
        return expectedItems;
    }
}
