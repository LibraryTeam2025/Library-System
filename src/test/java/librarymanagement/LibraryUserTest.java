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
        user.payFine(100.0);
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
        bm1.setDueDate(LocalDate.now().minusDays(5));
        bm1.setFineAdded(false);

        bm2.setDueDate(LocalDate.now().minusDays(3));
        bm2.setFineAdded(true);

        user.getBorrowedMediaInternal().add(bm1);
        user.getBorrowedMediaInternal().add(bm2);

        double newFines = user.calculateNewFines();

        assertEquals(50.0, newFines, 0.01);
        assertTrue(bm1.isFineAdded());
        assertTrue(bm2.isFineAdded());
    }

    @Test
    void testUpdateFineBalance_AppliesNewFines() {
        Book book = new Book("Design Patterns", "GoF", "DP001", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(4));
        bm.setFineAdded(false);

        user.getBorrowedMediaInternal().add(bm);

        user.updateFineBalance();

        assertEquals(40.0, user.getFineBalance(), 0.01);
    }
    @Test
    void testUpdateFineBalance_FullBranchCoverage() {
        Book book1 = new Book("Book On Time", "Author", "B001", 1);
        Book book2 = new Book("Book Overdue", "Author", "B002", 1);
        Book book3 = new Book("Book Returned", "Author", "B003", 1);

        BorrowedMedia onTime = new BorrowedMedia(book1);
        onTime.setDueDate(LocalDate.now().plusDays(2));

        BorrowedMedia overdue = new BorrowedMedia(book2);
        overdue.setDueDate(LocalDate.now().minusDays(3));

        BorrowedMedia returned = new BorrowedMedia(book3);
        returned.returnMedia();

        user.getBorrowedMediaInternal().add(onTime);
        user.getBorrowedMediaInternal().add(overdue);
        user.getBorrowedMediaInternal().add(returned);

        user.updateFineBalance();
        assertEquals(overdue.calculateFine(), user.getFineBalance(), 0.01);
    }
    @Test
    void testCalculateNewFines_FullBranchCoverage() {
        Book book1 = new Book("On Time", "A", "B001", 1);
        Book book2 = new Book("Overdue Not Added", "B", "B002", 1);
        Book book3 = new Book("Overdue Already Added", "C", "B003", 1);
        Book book4 = new Book("Returned", "D", "B004", 1);

        BorrowedMedia onTime = new BorrowedMedia(book1);
        onTime.setDueDate(LocalDate.now().plusDays(2));

        BorrowedMedia overdueNotAdded = new BorrowedMedia(book2);
        overdueNotAdded.setDueDate(LocalDate.now().minusDays(3));
        overdueNotAdded.setFineAdded(false);

        BorrowedMedia overdueAlreadyAdded = new BorrowedMedia(book3);
        overdueAlreadyAdded.setDueDate(LocalDate.now().minusDays(2));
        overdueAlreadyAdded.setFineAdded(true);

        BorrowedMedia returned = new BorrowedMedia(book4);
        returned.returnMedia();
        returned.setDueDate(LocalDate.now().minusDays(5));

        user.getBorrowedMediaInternal().add(onTime);
        user.getBorrowedMediaInternal().add(overdueNotAdded);
        user.getBorrowedMediaInternal().add(overdueAlreadyAdded);
        user.getBorrowedMediaInternal().add(returned);

        double totalFines = user.calculateNewFines();

        assertEquals(overdueNotAdded.calculateFine(), totalFines, 0.01);
        assertTrue(overdueNotAdded.isFineAdded());
        assertTrue(overdueAlreadyAdded.isFineAdded());
        assertFalse(onTime.isFineAdded());
        assertFalse(returned.isFineAdded());
    }
    @Test
    void testCalculateNewFines_TotalZero_NoAddFineCalled() {
        Book book = new Book("On Time Book", "Author", "B005", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().plusDays(2));
        bm.setFineAdded(false);

        user.getBorrowedMediaInternal().add(bm);
        double total = user.calculateNewFines();
        assertEquals(0.0, total, 0.01);
        assertEquals(0.0, user.getFineBalance(), 0.01);
        assertFalse(bm.isFineAdded());
    }

    @Test
    void testConstructorWithNullEmail() {
        LibraryUser user = new LibraryUser("Alice", null);
        assertEquals("Alice", user.getName());
        assertEquals("", user.getEmail());
        assertEquals("", user.getPassword());
    }
    @Test
    void testSetEmail() {
        LibraryUser user = new LibraryUser("Alice", "a@example.com");
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail());

        user.setEmail(null);
        assertNull(user.getEmail());
    }
    @Test
    void testBlockedStatusCombinations() {
        LibraryUser testUser = new LibraryUser("Roa", "roa@example.com");

        assertFalse(testUser.isBlocked());

        testUser.addFine(10.0);
        assertTrue(testUser.isBlocked());

        testUser.payFine(10.0);
        assertFalse(testUser.isBlocked());
        Book book = new Book("Overdue Book", "Author", "B001", 1);
        BorrowedMedia overdueItem = new BorrowedMedia(book);
        overdueItem.setDueDate(LocalDate.now().minusDays(2));
        testUser.getBorrowedMediaInternal().add(overdueItem);

        assertTrue(testUser.hasOverdueItems());
        testUser.setBlocked(testUser.getFineBalance() > 0 || testUser.hasOverdueItems());
        assertTrue(testUser.isBlocked());

        overdueItem.returnMedia();
        testUser.setBlocked(testUser.getFineBalance() > 0 || testUser.hasOverdueItems());
        assertFalse(testUser.isBlocked());
    }
    @Test
    void testUpdateFineBalance_BranchCoverage() {
        LibraryUser testUser = new LibraryUser("Roa", "roa@example.com");

        Book book1 = new Book("Book 1", "Author", "B001", 1);
        BorrowedMedia bm1 = new BorrowedMedia(book1);
        bm1.setDueDate(LocalDate.now().minusDays(5));
        testUser.getBorrowedMediaInternal().add(bm1);

        testUser.updateFineBalance();
        assertTrue(testUser.getFineBalance() > 0);
        assertTrue(testUser.isBlocked());

        bm1.returnMedia();
        testUser.setFineBalance(0);
        assertFalse(testUser.isBlocked());

        Book book2 = new Book("Book 2", "Author", "B002", 1);
        BorrowedMedia bm2 = new BorrowedMedia(book2);
        bm2.setDueDate(LocalDate.now().minusDays(3));
        testUser.getBorrowedMediaInternal().add(bm2);

        testUser.updateFineBalance();
        assertTrue(testUser.hasOverdueItems());
        assertTrue(testUser.isBlocked());
    }


    @Test
    void testBlockedStatus_WhenHasFineOrOverdue() {
        assertFalse(user.isBlocked());

        user.addFine(10.0);
        assertTrue(user.isBlocked());

        user.payFine(10.0);
        assertFalse(user.isBlocked());

        Book book = new Book("Refactoring", "Fowler", "R001", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(1));
        user.getBorrowedMediaInternal().add(bm);
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
    @Test
    void testBlockedCoverage() {
        LibraryUser u = new LibraryUser("Test", "test@example.com");
        Book book = new Book("Book", "Author", "B001", 1);

        u.setFineBalance(0);
        assertFalse(u.isBlocked());

        u.setFineBalance(50);
        assertTrue(u.isBlocked());

        u.setFineBalance(0);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(1));
        u.getBorrowedMediaInternal().add(bm);
        u.updateFineBalance();
        assertTrue(u.isBlocked());
        u.addFine(20);
        assertTrue(u.isBlocked());
    }

}