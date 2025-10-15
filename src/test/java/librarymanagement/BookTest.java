package librarymanagement;
import librarymanagement.domain.Book;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookTest {


    @Test
    void testBookCreation() {
        Book book = new Book("Java Basics", "Yaman", "111");
        assertEquals("Java Basics", book.getTitle());
        assertEquals("Yaman", book.getAuthor());
        assertEquals("111", book.getIsbn());
        assertTrue(book.isAvailable());
    }

    @Test
    void testAvailabilityToggle() {
        Book book = new Book("Java Basics", "Yaman", "111");
        book.setAvailable(false);
        assertFalse(book.isAvailable());
        book.setAvailable(true);
        assertTrue(book.isAvailable());
    }

    @Test
    void testToString() {
        Book book = new Book("Java Basics", "Yaman", "111");
        String expected = "Java Basics by Yaman (ISBN: 111)";
        assertEquals(expected, book.toString());
    }



}
