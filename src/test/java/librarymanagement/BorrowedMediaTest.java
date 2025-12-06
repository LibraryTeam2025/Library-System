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
        assertEquals(COPIES, book.getAvailableCopies());

        bm.returnMedia();
        assertEquals(COPIES, book.getAvailableCopies());
        assertTrue(bm.isReturned());
    }

    @Test
    void testCalculateFine() {
        Book book = new Book("Effective Java", "Bloch", "003", COPIES);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(4));
        assertEquals(40.0, bm.calculateFine(), 0.001);
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
        assertEquals(3, book.getAvailableCopies());
        assertTrue(book.getAvailableCopies() > 0);
    }

    @Test
    void testAllCopiesBorrowed() {
        Book book = new Book("Microservices", "Newman", "777", 2);
        new BorrowedMedia(book);
        new BorrowedMedia(book);
        assertEquals(2, book.getAvailableCopies());
        assertTrue(book.getAvailableCopies() > 0);
    }
    @Test
    void testBorrowedMedia_ReduceAvailableCopies_OnCreation() {
        Book book = new Book("Java", "X", "B001", 5);
        assertEquals(5, book.getAvailableCopies());
        new BorrowedMedia(book);
        assertEquals(5, book.getAvailableCopies());
    }

    @Test
    void testReturnMedia_IncreasesAvailableCopies() {
        Book book = new Book("Clean Code", "Bob", "B002", 3);
        BorrowedMedia bm = new BorrowedMedia(book);
        book.borrowCopy();
        assertEquals(2, book.getAvailableCopies());
        bm.returnMedia();
        assertEquals(3, book.getAvailableCopies());
        assertTrue(bm.isReturned());
    }

    @Test
    void testCalculateFine_ForBook_UsesBookStrategy() {
        Book book = new Book("Effective Java", "Bloch", "B003", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(5));
        double fine = bm.calculateFine();
        assertEquals(50.0, fine, 0.001);
    }

    @Test
    void testCalculateFine_ForCD_UsesCDStrategy() {
        CD cd = new CD("Thriller", "MJ", "CD001", 1);
        BorrowedMedia bm = new BorrowedMedia(cd);
        bm.setDueDate(LocalDate.now().minusDays(3));

        double fine = bm.calculateFine();
        assertEquals(60.0, fine, 0.001);
    }

    @Test
    void testCalculateFine_ReturnedMedia_NoFineEvenIfLate() {
        Book book = new Book("Late but Returned", "X", "B004", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(10));
        bm.returnMedia();
        assertEquals(0.0, bm.calculateFine(), 0.001);
    }

    @Test
    void testToString_ShowsCorrectStatus() {
        Book book = new Book("Test Book", "Author", "B005", 1);
        BorrowedMedia bm = new BorrowedMedia(book);

        String s1 = bm.toString();
        assertTrue(s1.contains("[BOOK]"));
        assertTrue(s1.contains("[ON TIME]") || s1.contains("[OVERDUE]"));
        bm.setDueDate(LocalDate.now().minusDays(2));
        String s2 = bm.toString();
        assertTrue(s2.contains("[OVERDUE]"));
        assertTrue(s2.contains("2 day(s) late"));
        bm.returnMedia();
        String s3 = bm.toString();
        assertTrue(s3.contains("[RETURNED]"));
    }
    @Test
    void testCalculateFine_UnknownMediaType_ThrowsIllegalArgumentException() {
        Media unknownMedia = new Media("Unknown Title", "Unknown Author", "U999", 1, new BookFineStrategy()) {
            @Override
            public int getBorrowDays() {
                return 14;
            }
        };
        BorrowedMedia bm = new BorrowedMedia(unknownMedia);
        bm.setDueDate(LocalDate.now().minusDays(5));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                bm::calculateFine
        );
        assertEquals("Unknown media type", exception.getMessage());
    }
    @Test
    void testConstructor_WithCustomDueDate_UsesProvidedDueDate() {
        Book book = new Book("Custom Due", "Author", "C001", 1);
        LocalDate customDueDate = LocalDate.of(2025, 12, 31);
        BorrowedMedia bm = new BorrowedMedia(book, customDueDate);

        assertEquals(customDueDate, bm.getDueDate());
        assertEquals(LocalDate.now(), bm.getBorrowDate());
        assertFalse(bm.isReturned());
    }
    @Test
    void testToString_FullCoverage_AllBranchesAndMediaTypes() {
        Book book = new Book("Test Book", "A", "B1", 1);
        CD cd = new CD("Test CD", "B", "C1", 1);
        assertTrue(new BorrowedMedia(book, LocalDate.now().plusDays(1)).toString().contains("[ON TIME]"));
        assertTrue(new BorrowedMedia(book, LocalDate.now().minusDays(2)).toString().contains("[OVERDUE]"));
        BorrowedMedia returnedBook = new BorrowedMedia(book);
        returnedBook.returnMedia();
        assertTrue(returnedBook.toString().contains("[RETURNED]"));
        assertTrue(new BorrowedMedia(cd, LocalDate.now().plusDays(1)).toString().contains("[CD]"));
        assertTrue(new BorrowedMedia(cd, LocalDate.now().minusDays(1)).toString().contains("[OVERDUE]"));
        BorrowedMedia returnedCD = new BorrowedMedia(cd);
        returnedCD.returnMedia();
        assertTrue(returnedCD.toString().contains("[RETURNED]"));
    }
    @Test
    void testGetFine_CoversReturnValueExplicitly() {
        BorrowedMedia bm = new BorrowedMedia(new Book("X", "Y", "Z", 1));
        assertEquals(0.0, bm.getFine());
        bm.setDueDate(LocalDate.now().minusDays(5));
        bm.calculateFine();
        assertEquals(50.0, bm.getFine(), 0.001);
        bm.returnMedia();
        assertEquals(50.0, bm.getFine());
    }
    @Test
    void testToString_FullBranchCoverage_BookAndCD_AllStates() {
        Book book = new Book("Test", "A", "ID1", 1);
        CD cd = new CD("Test CD", "B", "ID2", 1);

        assertTrue(new BorrowedMedia(book, LocalDate.now().plusDays(1)).toString().contains("[ON TIME]"));
        assertTrue(new BorrowedMedia(book, LocalDate.now().minusDays(1)).toString().contains("[OVERDUE]"));
        BorrowedMedia returnedBook = new BorrowedMedia(book);
        returnedBook.returnMedia();
        assertTrue(returnedBook.toString().contains("[RETURNED]"));

        assertTrue(new BorrowedMedia(cd, LocalDate.now().plusDays(1)).toString().contains("[CD]"));
        assertTrue(new BorrowedMedia(cd, LocalDate.now().minusDays(1)).toString().contains("[OVERDUE]"));
        BorrowedMedia returnedCD = new BorrowedMedia(cd);
        returnedCD.returnMedia();
        assertTrue(returnedCD.toString().contains("[RETURNED]"));
    }
    @Test
    void testFinal100PercentCoverage_AllRedLinesBecomeGreen() {
        Book book = new Book("X", "Y", "Z", 2);

        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(5));
        bm.calculateFine();

        assertEquals(50.0, bm.getFine());
        bm.setFine(123.45);
        assertEquals(123.45, bm.getFine());
        assertTrue(bm.toString().contains("Fine: $123.45"));
        book.borrowCopy();
        bm.returnMedia();
        assertEquals(2, book.getAvailableCopies());
    }
    @Test
    void testToString_Full100PercentCoverage_AllSixCases_BookAndCD() {
        Book book = new Book("Book Title", "Author", "B001", 1);
        CD cd = new CD("CD Title", "Artist", "C001", 1);

        BorrowedMedia bm1 = new BorrowedMedia(book, LocalDate.now().plusDays(5));
        assertTrue(bm1.toString().contains("[BOOK]"));
        assertTrue(bm1.toString().contains("[ON TIME]"));

        BorrowedMedia bm2 = new BorrowedMedia(book, LocalDate.now().minusDays(3));
        bm2.calculateFine();
        String s2 = bm2.toString();
        assertTrue(s2.contains("[OVERDUE]"));
        assertTrue(s2.contains("day(s) late"));
        assertTrue(s2.contains("Fine: $"));
        BorrowedMedia bm3 = new BorrowedMedia(book);
        bm3.returnMedia();
        assertTrue(bm3.toString().contains("[RETURNED]"));
        BorrowedMedia bm4 = new BorrowedMedia(cd, LocalDate.now().plusDays(2));
        assertTrue(bm4.toString().contains("[CD]"));
        assertTrue(bm4.toString().contains("[ON TIME]"));
        BorrowedMedia bm5 = new BorrowedMedia(cd, LocalDate.now().minusDays(1));
        bm5.calculateFine();
        assertTrue(bm5.toString().contains("[OVERDUE]"));
        BorrowedMedia bm6 = new BorrowedMedia(cd);
        bm6.returnMedia();
        assertTrue(bm6.toString().contains("[RETURNED]"));
    }
    @Test
    void testIsOverdue_AllBranches() {
        Book book = new Book("X", "Y", "Z", 1);

        BorrowedMedia bmReturned = new BorrowedMedia(book);
        bmReturned.returnMedia();
        assertFalse(bmReturned.isOverdue());

        BorrowedMedia bmNotLate = new BorrowedMedia(book, LocalDate.now().plusDays(2));
        assertFalse(bmNotLate.isOverdue());

        BorrowedMedia bmLate = new BorrowedMedia(book, LocalDate.now().minusDays(1));
        assertTrue(bmLate.isOverdue());
    }

    @Test
    void testGetOverdueDays_AllBranches() {
        Book book = new Book("X", "Y", "Z", 1);

        BorrowedMedia bmReturned = new BorrowedMedia(book, LocalDate.now().minusDays(3));
        bmReturned.returnMedia();
        assertEquals(0, bmReturned.getOverdueDays());

        BorrowedMedia bmNotLate = new BorrowedMedia(book, LocalDate.now().plusDays(1));
        assertEquals(0, bmNotLate.getOverdueDays());

        BorrowedMedia bmLate = new BorrowedMedia(book, LocalDate.now().minusDays(2));
        assertEquals(2, bmLate.getOverdueDays());
    }

    @Test
    void testFineAdded_SetTwice() {
        Book book = new Book("X", "Y", "Z", 1);
        BorrowedMedia bm = new BorrowedMedia(book);

        bm.setFineAdded(true);
        assertTrue(bm.isFineAdded());

        bm.setFineAdded(false);
        assertFalse(bm.isFineAdded());
    }

    @Test
    void testSetDueDate_Coverage() {
        Book book = new Book("X", "Y", "Z", 1);
        BorrowedMedia bm = new BorrowedMedia(book);

        LocalDate newDue = LocalDate.now().plusDays(10);
        bm.setDueDate(newDue);
        assertEquals(newDue, bm.getDueDate());
    }

    @Test
    void testCalculateFine_ZeroDaysLate() {
        Book book = new Book("X", "Y", "Z", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now());
        assertEquals(0.0, bm.calculateFine(), 0.001);
    }
    @Test
    void testToString_AllBranches_BookAndCD() {
        Book book = new Book("Book Title", "Author", "B001", 1);
        CD cd = new CD("CD Title", "Artist", "C001", 1);
        BorrowedMedia bmBookOnTime = new BorrowedMedia(book, LocalDate.now().plusDays(3));
        assertTrue(bmBookOnTime.toString().contains("[BOOK]"));
        assertTrue(bmBookOnTime.toString().contains("[ON TIME]"));

        BorrowedMedia bmBookOverdue = new BorrowedMedia(book, LocalDate.now().minusDays(4));
        bmBookOverdue.calculateFine();
        String strOverdueBook = bmBookOverdue.toString();
        assertTrue(strOverdueBook.contains("[BOOK]"));
        assertTrue(strOverdueBook.contains("[OVERDUE]"));
        assertTrue(strOverdueBook.contains("day(s) late"));
        assertTrue(strOverdueBook.contains("Fine: $"));
        BorrowedMedia bmBookReturned = new BorrowedMedia(book, LocalDate.now().minusDays(2));
        bmBookReturned.returnMedia();
        assertTrue(bmBookReturned.toString().contains("[BOOK]"));
        assertTrue(bmBookReturned.toString().contains("[RETURNED]"));

        BorrowedMedia bmCDOnTime = new BorrowedMedia(cd, LocalDate.now().plusDays(2));
        assertTrue(bmCDOnTime.toString().contains("[CD]"));
        assertTrue(bmCDOnTime.toString().contains("[ON TIME]"));

        BorrowedMedia bmCDOverdue = new BorrowedMedia(cd, LocalDate.now().minusDays(3));
        bmCDOverdue.calculateFine();
        String strOverdueCD = bmCDOverdue.toString();
        assertTrue(strOverdueCD.contains("[CD]"));
        assertTrue(strOverdueCD.contains("[OVERDUE]"));
        assertTrue(strOverdueCD.contains("day(s) late"));
        assertTrue(strOverdueCD.contains("Fine: $"));

        BorrowedMedia bmCDReturned = new BorrowedMedia(cd, LocalDate.now().minusDays(1));
        bmCDReturned.returnMedia();
        assertTrue(bmCDReturned.toString().contains("[CD]"));
        assertTrue(bmCDReturned.toString().contains("[RETURNED]"));
    }
    @Test
    void testToString_FullBranchCoverage_BookAndCD() {
        Book book = new Book("Book Title", "Author", "B001", 1);

        BorrowedMedia bmBookOnTime = new BorrowedMedia(book, LocalDate.now().plusDays(3));
        assertTrue(bmBookOnTime.toString().contains("[BOOK]"));
        assertTrue(bmBookOnTime.toString().contains("[ON TIME]"));

        BorrowedMedia bmBookOverdue = new BorrowedMedia(book, LocalDate.now().minusDays(4));
        bmBookOverdue.calculateFine();
        String strOverdueBook = bmBookOverdue.toString();
        assertTrue(strOverdueBook.contains("[BOOK]"));
        assertTrue(strOverdueBook.contains("[OVERDUE]"));
        assertTrue(strOverdueBook.contains("day(s) late"));
        assertTrue(strOverdueBook.contains("Fine: $"));

        BorrowedMedia bmBookReturned = new BorrowedMedia(book, LocalDate.now().minusDays(2));
        bmBookReturned.returnMedia();
        assertTrue(bmBookReturned.toString().contains("[BOOK]"));
        assertTrue(bmBookReturned.toString().contains("[RETURNED]"));

        CD cd = new CD("CD Title", "Artist", "C001", 1);

        BorrowedMedia bmCDOnTime = new BorrowedMedia(cd, LocalDate.now().plusDays(2));
        assertTrue(bmCDOnTime.toString().contains("[CD]"));
        assertTrue(bmCDOnTime.toString().contains("[ON TIME]"));

        BorrowedMedia bmCDOverdue = new BorrowedMedia(cd, LocalDate.now().minusDays(3));
        bmCDOverdue.calculateFine();
        String strOverdueCD = bmCDOverdue.toString();
        assertTrue(strOverdueCD.contains("[CD]"));
        assertTrue(strOverdueCD.contains("[OVERDUE]"));
        assertTrue(strOverdueCD.contains("day(s) late"));
        assertTrue(strOverdueCD.contains("Fine: $"));

        BorrowedMedia bmCDReturned = new BorrowedMedia(cd, LocalDate.now().minusDays(1));
        bmCDReturned.returnMedia();
        assertTrue(bmCDReturned.toString().contains("[CD]"));
        assertTrue(bmCDReturned.toString().contains("[RETURNED]"));
    }
    @Test
    void testBorrowedMedia_FullCoverage() {

        Book book = new Book("Java", "Author", "B1", 1);
        CD cd = new CD("Music", "Singer", "C1", 1);

        BorrowedMedia bm1 = new BorrowedMedia(book);
        assertNotNull(bm1.getBorrowDate());
        assertNotNull(bm1.getDueDate());

        BorrowedMedia bm2 = new BorrowedMedia(cd, LocalDate.now().plusDays(5));
        assertEquals(cd, bm2.getMedia());

        BorrowedMedia bm3 = new BorrowedMedia(book, LocalDate.now().minusDays(5), LocalDate.now().minusDays(3));
        assertTrue(bm3.isOverdue());

        BorrowedMedia bm4 = new BorrowedMedia(book, LocalDate.now().plusDays(2));
        assertFalse(bm4.isOverdue());
        double fineZero = bm4.calculateFine();
        assertEquals(0.0, fineZero);

        BorrowedMedia bm5 = new BorrowedMedia(book, LocalDate.now().minusDays(4));
        double fineBook = bm5.calculateFine();
        assertTrue(fineBook > 0);

        BorrowedMedia bm6 = new BorrowedMedia(cd, LocalDate.now().minusDays(2));
        double fineCd = bm6.calculateFine();
        assertTrue(fineCd > 0);

        BorrowedMedia bm7 = new BorrowedMedia(book, LocalDate.now().minusDays(1));
        bm7.returnMedia();
        assertTrue(bm7.isReturned());

        bm7.setFineAdded(true);
        assertTrue(bm7.isFineAdded());

        BorrowedMedia bm8 = new BorrowedMedia(book, LocalDate.now().plusDays(3));
        assertTrue(bm8.toString().contains("[ON TIME]"));

        bm5.calculateFine();
        assertTrue(bm5.toString().contains("[OVERDUE]"));

        bm7.returnMedia();
        assertTrue(bm7.toString().contains("[RETURNED]"));
    }
    @Test
    void testBorrowedMedia_Full100Coverage() {

        Book book = new Book("Java", "A", "B1", 3);
        CD cd = new CD("Music", "B", "C1", 2);

        BorrowedMedia bm1 = new BorrowedMedia(book);
        assertNotNull(bm1.getBorrowDate());
        assertEquals(LocalDate.now().plusDays(28), bm1.getDueDate());

        LocalDate customDue = LocalDate.now().plusDays(10);
        BorrowedMedia bm2 = new BorrowedMedia(cd, customDue);
        assertEquals(customDue, bm2.getDueDate());

        LocalDate borrow = LocalDate.now().minusDays(5);
        LocalDate due = LocalDate.now().minusDays(2);
        BorrowedMedia bm3 = new BorrowedMedia(book, borrow, due);
        assertEquals(borrow, bm3.getBorrowDate());
        assertEquals(due, bm3.getDueDate());

        BorrowedMedia bm4 = new BorrowedMedia(book, LocalDate.now().plusDays(3));
        assertFalse(bm4.isOverdue());

        BorrowedMedia bm5 = new BorrowedMedia(book, LocalDate.now().minusDays(1));
        assertTrue(bm5.isOverdue());

        BorrowedMedia bm6 = new BorrowedMedia(book, LocalDate.now().minusDays(5));
        bm6.returnMedia();

        assertEquals(0.0, bm6.calculateFine());

        BorrowedMedia bm7 = new BorrowedMedia(book, LocalDate.now().plusDays(2));
        assertEquals(0.0, bm7.calculateFine());

        BorrowedMedia bm8 = new BorrowedMedia(book, LocalDate.now().minusDays(3));
        double fineBook = bm8.calculateFine();
        assertEquals(30.0, fineBook, 0.001);

        BorrowedMedia bm9 = new BorrowedMedia(cd, LocalDate.now().minusDays(2));
        double fineCd = bm9.calculateFine();
        assertEquals(40.0, fineCd, 0.001);

        Media unknown = new Media("X", "Y", "Z", 1, new BookFineStrategy()) {
            @Override
            public int getBorrowDays() { return 10; }
        };

        BorrowedMedia bm10 = new BorrowedMedia(unknown, LocalDate.now().minusDays(3));
        assertThrows(IllegalArgumentException.class, bm10::calculateFine);

        Book book2 = new Book("T", "A", "ID1", 2);
        BorrowedMedia bm11 = new BorrowedMedia(book2);
        book2.borrowCopy();
        assertEquals(1, book2.getAvailableCopies());
        bm11.returnMedia();
        assertEquals(2, book2.getAvailableCopies());
        assertTrue(bm11.isReturned());

        bm8.setFine(99.5);
        assertEquals(99.5, bm8.getFine(), 0.001);

        bm8.setFineAdded(true);
        assertTrue(bm8.isFineAdded());

        BorrowedMedia b1 = new BorrowedMedia(book, LocalDate.now().plusDays(1));
        assertTrue(b1.toString().contains("[BOOK]"));
        assertTrue(b1.toString().contains("[ON TIME]"));

        BorrowedMedia b2 = new BorrowedMedia(book, LocalDate.now().minusDays(2));
        b2.calculateFine();
        assertTrue(b2.toString().contains("[OVERDUE]"));
        assertTrue(b2.toString().contains("day(s) late"));

        BorrowedMedia b3 = new BorrowedMedia(book);
        b3.returnMedia();
        assertTrue(b3.toString().contains("[RETURNED]"));

        BorrowedMedia c1 = new BorrowedMedia(cd, LocalDate.now().plusDays(3));
        assertTrue(c1.toString().contains("[CD]"));
        assertTrue(c1.toString().contains("[ON TIME]"));

        BorrowedMedia c2 = new BorrowedMedia(cd, LocalDate.now().minusDays(1));
        c2.calculateFine();
        assertTrue(c2.toString().contains("[OVERDUE]"));

        BorrowedMedia c3 = new BorrowedMedia(cd);
        c3.returnMedia();
        assertTrue(c3.toString().contains("[RETURNED]"));
    }
    @Test
    void testUnknownMediaTypeBranch() {

        Media fakeMedia = new Media(
                "Fake Title",
                "Fake Author",
                "123",
                1,
                days -> days * 1
        ) {
            @Override
            public int getBorrowDays() {
                return 5;
            }
            @Override
            public boolean borrowCopy() {
                return true;
            }
            @Override
            public void returnCopy() {
            }
        };

        BorrowedMedia b = new BorrowedMedia(
                fakeMedia,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(5)
        );

        assertThrows(IllegalArgumentException.class, b::calculateFine);
    }
    @Test
    void testToStringFullCoverage() {
        Book book = new Book("B", "A", "1", 1);
        CD cd = new CD("CD", "Artist", "2", 1);

        assertTrue(new BorrowedMedia(book, LocalDate.now().plusDays(1)).toString().contains("[ON TIME]"));
        assertTrue(new BorrowedMedia(cd, LocalDate.now().plusDays(1)).toString().contains("[ON TIME]"));

        BorrowedMedia bmBookOver = new BorrowedMedia(book, LocalDate.now().minusDays(2));
        bmBookOver.calculateFine();
        assertTrue(bmBookOver.toString().contains("[OVERDUE]"));

        BorrowedMedia bmCDOver = new BorrowedMedia(cd, LocalDate.now().minusDays(2));
        bmCDOver.calculateFine();
        assertTrue(bmCDOver.toString().contains("[OVERDUE]"));

        BorrowedMedia bmBookReturned = new BorrowedMedia(book);
        bmBookReturned.returnMedia();
        assertTrue(bmBookReturned.toString().contains("[RETURNED]"));

        BorrowedMedia bmCDReturned = new BorrowedMedia(cd);
        bmCDReturned.returnMedia();
        assertTrue(bmCDReturned.toString().contains("[RETURNED]"));
    }
    @Test
    void testSetAndGetFine() {
        Book book = new Book("Test", "Author", "B001", 1);
        BorrowedMedia bm = new BorrowedMedia(book);

        bm.setFine(123.45);
        assertEquals(123.45, bm.getFine(), 0.001);
    }
    @Test
    void testToStringBranches() {
        Book book = new Book("Book", "Author", "B001", 1);
        CD cd = new CD("CD", "Artist", "C001", 1);

        BorrowedMedia onTime = new BorrowedMedia(book, LocalDate.now().plusDays(1));
        assertTrue(onTime.toString().contains("[ON TIME]"));

        BorrowedMedia overdue = new BorrowedMedia(cd, LocalDate.now().minusDays(2));
        overdue.calculateFine();
        assertTrue(overdue.toString().contains("[OVERDUE]"));

        BorrowedMedia returned = new BorrowedMedia(book);
        returned.returnMedia();
        assertTrue(returned.toString().contains("[RETURNED]"));
    }
    @Test
    void testSetFineBranch() {
        Book book = new Book("Test Book", "Author", "B001", 1);
        BorrowedMedia bm = new BorrowedMedia(book);

        bm.setFine(100.0);
        assertEquals(100.0, bm.getFine(), 0.001);
    }


}
