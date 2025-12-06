package librarymanagement;
import librarymanagement.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


public class MediaTest {

        @Test
        void testSetTitle_NullOrEmpty_KeepsOld() {
            Book book = new Book("Old Title", "Author", "B1", 1);
            book.setTitle(null);
            book.setTitle("");
            book.setTitle("   ");
            assertEquals("Old Title", book.getTitle());
        }

        @Test
        void testSetTitle_Valid_Updates() {
            Book book = new Book("Old", "A", "B2", 1);
            book.setTitle("New Title");
            assertEquals("New Title", book.getTitle());
        }

        @Test
        void testSetAuthor_NullOrEmpty_KeepsOld() {
            Book book = new Book("Title", "Old Author", "B3", 1);
            book.setAuthor(null);
            book.setAuthor("");
            assertEquals("Old Author", book.getAuthor());
        }

        @Test
        void testSetTotalCopies_LessThanAvailable_ThrowsException() {
            Book book = new Book("Book", "A", "B4", 5);
            book.borrowCopy();
            book.borrowCopy();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> book.setTotalCopies(2));
            assertEquals("Total copies cannot be less than available copies!", ex.getMessage());
        }

        @Test
        void testSetTotalCopies_EqualToAvailable_Success() {
            Book book = new Book("Book", "A", "B5", 3);
            book.borrowCopy();
            book.borrowCopy();
            book.setTotalCopies(3);
            assertEquals(3, book.getTotalCopies());
        }

        @Test
        void testCalculateFine_ZeroDays() {
            Book book = new Book("B", "A", "B6", 1);
            assertEquals(0.0, book.calculateFine(0L));
        }

    @Test
    void testCalculateFine_NegativeDays() {
        Book book = new Book("B", "A", "B7", 1);
        assertEquals(-50.0, book.calculateFine(-5L));
    }

        @Test
        void testAddCopy_IncreasesBoth() {
            Book book = new Book("B", "A", "B8", 2);
            book.borrowCopy(); // available = 1
            book.addCopy();
            assertEquals(3, book.getTotalCopies());
            assertEquals(2, book.getAvailableCopies());
        }

        @Test
        void testGetType_ForBookAndCD() {
            Book book = new Book("Java", "X", "B9", 1);
            CD cd = new CD("Music", "Y", "C9", 1);
            assertEquals("[Book]", book.getType());
            assertEquals("[CD]", cd.getType());
        }
    @Test
    void testBorrowCopy_WhenNoCopiesAvailable_ReturnsFalse() {
        Book book = new Book("Test", "Author", "B99", 1);
        book.borrowCopy();

        assertFalse(book.borrowCopy());
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void testReturnCopy_WhenAllCopiesAlreadyAvailable_DoesNothing() {
        Book book = new Book("Test", "Author", "B100", 3);
        book.returnCopy();
        assertEquals(3, book.getAvailableCopies());
        assertEquals(3, book.getTotalCopies());
    }

    @Test
    void testCalculateFine_WithLocalDate_WhenNotOverdue_ReturnsZero() {
        Book book = new Book("Future", "A", "B101", 1);

        LocalDate dueDate = LocalDate.now().plusDays(5);
        double fine = book.calculateFine(dueDate);

        assertEquals(0.0, fine, 0.001);
    }

    @Test
    void testCalculateFine_WithLocalDate_WhenOverdue_CalculatesCorrectly() {
        Book book = new Book("Past", "A", "B102", 1);

        LocalDate dueDate = LocalDate.now().minusDays(10);
        double fine = book.calculateFine(dueDate);

        assertEquals(100.0, fine, 0.001);
    }

    @Test
    void testAddCopy_WhenSomeBorrowed_IncreasesBoth() {
        Book book = new Book("Mixed", "A", "B103", 2);
        book.borrowCopy();

        book.addCopy();

        assertEquals(3, book.getTotalCopies());
        assertEquals(2, book.getAvailableCopies());
    }
    @Test
    void testConstructor_WithZeroOrNegativeCopies_SetsMinimumOne() {
        Book book1 = new Book("Title", "Author", "BZero", 0);
        Book book2 = new Book("Title", "Author", "BNeg", -5);
        assertAll(
                () -> assertEquals(1, book1.getTotalCopies()),
                () -> assertEquals(1, book1.getAvailableCopies()),
                () -> assertEquals(1, book2.getTotalCopies()),
                () -> assertEquals(1, book2.getAvailableCopies())
        );
    }
    @Test
    void testGetType_WhenUnknownMedia_ReturnsUnknown() {
        Media unknownMedia = new Media("Unknown Title", "Unknown Author", "U999", 1, new BookFineStrategy()) {
            @Override
            public int getBorrowDays() {
                return 14;
            }
        };

        assertEquals("[Unknown]", unknownMedia.getType());
    }
    @Test
    void testReturnCopy_MultipleTimes_DoesNotExceedTotalCopies() {
        Book book = new Book("Test", "A", "B999", 1);
        book.borrowCopy();
        book.returnCopy();
        book.returnCopy();
        assertEquals(1, book.getAvailableCopies());
    }
    @Test
    void testSetAuthor_Null_DoesNotChange() {
        Book book = new Book("Title", "Old Author", "B1", 1);
        book.setAuthor(null);
        assertEquals("Old Author", book.getAuthor());
    }

    @Test
    void testSetAuthor_EmptyOrWhitespace_DoesNotChange() {
        Book book = new Book("Title", "Old Author", "B2", 1);

        book.setAuthor("");
        assertEquals("Old Author", book.getAuthor());

        book.setAuthor("   ");
        assertEquals("Old Author", book.getAuthor());

        book.setAuthor("\t\n");
        assertEquals("Old Author", book.getAuthor());
    }
    @Test
    void testSetAuthor_ValidValue_UpdatesAndTrims() {
        Book book = new Book("Title", "Old", "B3", 1);
        book.setAuthor("  New Author Name  ");
        assertEquals("New Author Name", book.getAuthor());
    }
    @Test
    void testSetAvailableCopies_NegativeValue_DoesNotChange() {
        Book book = new Book("Test", "Author", "B777", 5);
        book.setAvailableCopies(3);

        book.setAvailableCopies(-1);

        assertEquals(3, book.getAvailableCopies());
    }

    @Test
    void testSetAvailableCopies_GreaterThanTotal_DoesNotChange() {
        Book book = new Book("Test", "Author", "B888", 3);

        book.setAvailableCopies(4);
        assertEquals(3, book.getAvailableCopies());

        book.setAvailableCopies(10);
        assertEquals(3, book.getAvailableCopies());
    }
    @Test
    void testSetAvailableCopies_ValidValue_UpdatesCorrectly() {
        Book book = new Book("Test", "A", "B999", 10);
        book.setAvailableCopies(7);
        assertEquals(7, book.getAvailableCopies());

        book.setAvailableCopies(0);
        assertEquals(0, book.getAvailableCopies());

        book.setAvailableCopies(10);
        assertEquals(10, book.getAvailableCopies());
    }
    }



