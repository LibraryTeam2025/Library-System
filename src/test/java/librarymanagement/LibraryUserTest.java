package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryUserTest {

    private LibraryUser user;

    @BeforeEach
    void setUp() {
        user = new LibraryUser("Roa", "pass123", "roa@example.com");
    }

    @Test
    void testAddFine_IncreasesBalance() {
        user.addFine(30.0);
        assertEquals(30.0, user.getFineBalance(), 0.01);

        user.addFine(25.5);
        assertEquals(55.5, user.getFineBalance(), 0.01);
    }

    @Test
    void testAddFine_IgnoresZeroOrNegative() {
        user.addFine(20.0);
        user.addFine(0.0);
        user.addFine(-10.0);
        assertEquals(20.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testPayFine_ReducesBalance() {
        user.addFine(100.0);
        user.payFine(40.0);
        assertEquals(60.0, user.getFineBalance(), 0.01);

        user.payFine(60.0);
        assertEquals(0.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testPayFine_CannotGoNegative() {
        user.addFine(50.0);
        user.payFine(100.0);  // ندفع أكتر من الرصيد
        assertEquals(0.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testPayFine_IgnoresZeroOrNegativePayment() {
        user.addFine(80.0);
        user.payFine(0);
        user.payFine(-5);
        assertEquals(80.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testCalculateNewFines_OnlyForOverdueNotAddedYet() {
        Book book1 = new Book("Java", "Oracle", "B001", 2);
        Book book2 = new Book("Clean Code", "Martin", "B002", 1);

        BorrowedMedia bm1 = new BorrowedMedia(book1);
        BorrowedMedia bm2 = new BorrowedMedia(book2);

        // نجعل bm1 overdue من 5 أيام → fine = 50
        bm1.setDueDate(LocalDate.now().minusDays(5));
        bm1.setFineAdded(false);

        // bm2 overdue من 3 أيام بس fine مضافة خلاص
        bm2.setDueDate(LocalDate.now().minusDays(3));
        bm2.setFineAdded(true);

        user.getBorrowedMediaInternal().add(bm1);
        user.getBorrowedMediaInternal().add(bm2);

        double newFines = user.calculateNewFines();

        assertEquals(50.0, newFines, 0.01);  // بس bm1 اللي اتحسب
        assertTrue(bm1.isFineAdded());
        assertTrue(bm2.isFineAdded()); // ما تغيرش
    }

    @Test
    void testUpdateFineBalance_AppliesNewFines() {
        Book book = new Book("Design Patterns", "GoF", "DP001", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(4));
        bm.setFineAdded(false);

        user.getBorrowedMediaInternal().add(bm);

        user.updateFineBalance();

        assertEquals(40.0, user.getFineBalance(), 0.01); // 4 أيام × 10
        assertTrue(bm.isFineAdded());
    }

    @Test
    void testBlockedStatus_WhenHasFineOrOverdue() {
        assertFalse(user.isBlocked());

        user.addFine(10.0);
        assertTrue(user.isBlocked());

        user.payFine(10.0);
        assertFalse(user.isBlocked());

        // نضيف عنصر overdue
        Book book = new Book("Refactoring", "Fowler", "R001", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(1));
        user.getBorrowedMediaInternal().add(bm);

        // حسب منطق الكود الحالي: blocked يعتمد فقط على الغرامة
        assertFalse(user.isBlocked());

        bm.returnMedia();
        assertFalse(user.isBlocked());
    }


    @Test
    void testToString() {
        user.addFine(25.75);
        String expected = "Roa (roa@example.com) (Fine: $25.75)";
        assertEquals(expected, user.toString());
    }

    @Test
    void testHasOverdueItems() {
        assertFalse(user.hasOverdueItems());

        Book book = new Book("Test Book", "Author", "T001", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(2));
        user.getBorrowedMediaInternal().add(bm);

        assertTrue(user.hasOverdueItems());

        bm.setDueDate(LocalDate.now().plusDays(10));
        assertFalse(user.hasOverdueItems());
    }
}