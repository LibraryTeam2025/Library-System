package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CDTest {

    @Test
    void testCDCreation() {
        CD cd = new CD("Top Hits", "Artist", "CD001");
        assertEquals("Top Hits", cd.getTitle());
        assertEquals("Artist", cd.getAuthor());
        assertEquals("CD001", cd.getId());
        assertTrue(cd.isAvailable());
    }

    @Test
    void testBorrowDays() {
        CD cd = new CD("Jazz", "Miles", "CD002");
        assertEquals(7, cd.getBorrowDays());
    }

    @Test
    void testFineAmount() {
        CD cd = new CD("Rock", "Band", "CD003");
        assertEquals(20.0, cd.getFineAmount());
    }

    @Test
    void testToString() {
        CD cd = new CD("Rock", "Band", "CD003");
        assertEquals("Rock by Band (CD ID: CD003)", cd.toString());
    }
}