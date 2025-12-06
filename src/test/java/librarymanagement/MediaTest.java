package librarymanagement;
import librarymanagement.domain.*;
import org.junit.jupiter.api.Test;
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
            book.borrowCopy(); // available = 3

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> book.setTotalCopies(2));
            assertEquals("Total copies cannot be less than available copies!", ex.getMessage());
        }

        @Test
        void testSetTotalCopies_EqualToAvailable_Success() {
            Book book = new Book("Book", "A", "B5", 3);
            book.borrowCopy();
            book.borrowCopy(); // available = 1
            book.setTotalCopies(3); // total was 3, now still 3
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
            assertEquals(0.0, book.calculateFine(-5L)); // لأن Math.max(0, ...)
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
    }



