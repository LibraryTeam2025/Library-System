package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class BorrowedMediaTest {

    @Test
    void testDueDateOnCreation() {
        Book book = new Book("Java", "X", "001");
        BorrowedMedia bm = new BorrowedMedia(book);
        assertEquals(LocalDate.now().plusDays(28), bm.getDueDate());
    }

    @Test
    void testReturnMedia() {
        Book book = new Book("Java", "X", "001");
        BorrowedMedia bm = new BorrowedMedia(book);
        assertFalse(book.isAvailable());
        bm.returnMedia();
        assertTrue(book.isAvailable());
        assertTrue(bm.isReturned());
    }

    @Test
    void testCalculateFine() {
        Book book = new Book("Java", "X", "001");
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(3));
        assertEquals(30.0, bm.calculateFine()); // 3 * 10
    }

    @Test
    void testCalculateFineZeroIfNotLate() {
        Book book = new Book("Java", "X", "001");
        BorrowedMedia bm = new BorrowedMedia(book);
        assertEquals(0.0, bm.calculateFine());
    }

    @Test
    void testCalculateFineZeroIfReturned() {
        Book book = new Book("Java", "X", "001");
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(1));
        bm.returnMedia();
        assertEquals(0.0, bm.calculateFine());
    }

    @Test
    void testFineAddedFlag() {
        Book book = new Book("Java", "X", "001");
        BorrowedMedia bm = new BorrowedMedia(book);
        assertFalse(bm.isFineAdded());
        bm.setFineAdded(true);
        assertTrue(bm.isFineAdded());
    }
}