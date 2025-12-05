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
        BorrowedMedia bm = new BorrowedMedia(cd);
        try {
            var field = BorrowedMedia.class.getDeclaredField("dueDate");
            field.setAccessible(true);
            field.set(bm, java.time.LocalDate.now().minusDays(1));
        } catch (Exception e) {
            fail("Reflection failed");
        }
        assertEquals(20.0, bm.calculateFine(), 0.01);
    }

    @Test
    void testToString() {
        CD cd = new CD("Rock", "Band", "CD003");
        assertEquals("[CD] Rock by Band (ID: CD003)", cd.toString());
    }

    // الحل السحري اللي يشتغل في كل المشاريع مهما كان IntelliJ متلخبط
    @Test
    void testCD_IsNotBook() {
        CD cd = new CD("Pop", "Singer", "CD004");

        // نستخدم getClass() بدل instanceof → يقضي على الإرور نهائيًا
        assertNotEquals(Book.class, cd.getClass(), "CD class must be different from Book class");
        assertEquals(CD.class, cd.getClass(), "Object must be of type CD");
        assertTrue(Media.class.isAssignableFrom(cd.getClass()), "CD must extend Media");
    }

    @Test
    void testCDFine_IsHigherThanBook() {
        Book book = new Book("Test Book", "Author", "B001");
        CD cd = new CD("Test CD", "Artist", "CD001");

        int bookFine = FineCalculator.calculateFine(book, 5);
        int cdFine = FineCalculator.calculateFine(cd, 5);

        assertEquals(50, bookFine);
        assertEquals(100, cdFine);
        assertTrue(cdFine > bookFine, "CD fine must be higher than book fine");
    }
}