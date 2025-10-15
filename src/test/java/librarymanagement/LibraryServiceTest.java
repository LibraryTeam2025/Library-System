package librarymanagement;

import librarymanagement.application.LibraryService;
import librarymanagement.domain.Book;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;



public class LibraryServiceTest {


    @Test
    void testAddAndSearchBook() {
        LibraryService service = new LibraryService();
        Book book = new Book("Java Basics", "Yaman", "111");
        service.addBook(book);

        List<Book> results = service.searchBook("Java");
        assertEquals(1, results.size());
        assertEquals("Java Basics", results.get(0).getTitle());
    }

    @Test
    void testSearchNoResults() {
        LibraryService service = new LibraryService();
        List<Book> results = service.searchBook("Python");
        assertTrue(results.isEmpty());
    }

}
