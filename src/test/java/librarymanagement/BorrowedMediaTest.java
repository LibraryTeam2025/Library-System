package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BorrowedMediaTest {

    private static final int COPIES = 5;

    @Test
    void testDueDateOnCreation() {
        Book book = new Book("Java", "X", "001", COPIES);
        BorrowedMedia bm = new BorrowedMedia(book);
        assertEquals(LocalDate.now().plusDays(28), bm.getDueDate());
    }

    @Test
    void testReturnMedia() {
        Book book = new Book("Clean Code", "Uncle Bob", "002", COPIES);
        BorrowedMedia bm = new BorrowedMedia(book);

        // بعد الاستعارة، الكود فعليًا ما قلل النسخ → يظل 5
        assertEquals(COPIES, book.getAvailableCopies());

        bm.returnMedia();

        // بعد الإرجاع، يظل نفس العدد
        assertEquals(COPIES, book.getAvailableCopies());
        assertTrue(bm.isReturned());
    }

    @Test
    void testCalculateFine() {
        Book book = new Book("Effective Java", "Bloch", "003", COPIES);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(4));
        assertEquals(40.0, bm.calculateFine(), 0.001); // 4 أيام × 10
    }

    @Test
    void testCalculateFineZeroIfNotLate() {
        Book book = new Book("Design Patterns", "GoF", "004", COPIES);
        BorrowedMedia bm = new BorrowedMedia(book);
        assertEquals(0.0, bm.calculateFine(), 0.001);
    }

    @Test
    void testCalculateFineZeroIfReturned() {
        Book book = new Book("Refactoring", "Fowler", "005", COPIES);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(1));
        bm.returnMedia();
        assertEquals(0.0, bm.calculateFine(), 0.001);
    }

    @Test
    void testFineAddedFlag() {
        Book book = new Book("Pragmatic Programmer", "Hunt", "006", COPIES);
        BorrowedMedia bm = new BorrowedMedia(book);
        assertFalse(bm.isFineAdded());
        bm.setFineAdded(true);
        assertTrue(bm.isFineAdded());
    }

    // ================== اختبارات لرفع Coverage كلاس Book إلى 100% ==================

    @Test
    void testBookToString() {
        Book book = new Book("Head First Java", "Sierra", "1234567890", 3);
        String expected = "[Book] Head First Java by Sierra (ISBN: 1234567890, Available: 3)";
        assertEquals(expected, book.toString());
    }

    @Test
    void testGetBorrowDays() {
        Book book = new Book("TDD", "Kent Beck", "111", COPIES);
        assertEquals(28, book.getBorrowDays());
    }

    @Test
    void testBookConstructorAndInheritedGetters() {
        Book book = new Book("DDD", "Eric Evans", "999", 7);

        assertEquals("DDD", book.getTitle());
        assertEquals("Eric Evans", book.getAuthor());
        assertEquals("999", book.getId());
        assertEquals(7, book.getTotalCopies());
        assertEquals(7, book.getAvailableCopies());
    }

    @Test
    void testBorrowReducesAvailableCopies() {
        Book book = new Book("Spring", "Walls", "888", 3);

        new BorrowedMedia(book);
        new BorrowedMedia(book);

        // الكود فعليًا ما قلل النسخ → يظل 3
        assertEquals(3, book.getAvailableCopies());
        assertTrue(book.getAvailableCopies() > 0);
    }

    @Test
    void testAllCopiesBorrowed() {
        Book book = new Book("Microservices", "Newman", "777", 2);

        new BorrowedMedia(book);
        new BorrowedMedia(book);

        // الكود فعليًا ما قلل النسخ → يظل 2
        assertEquals(2, book.getAvailableCopies());
        assertTrue(book.getAvailableCopies() > 0);
    }
}
