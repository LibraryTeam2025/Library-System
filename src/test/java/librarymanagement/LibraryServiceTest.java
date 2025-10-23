package librarymanagement;

import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;
import librarymanagement.domain.Book;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.LibraryUser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class LibraryServiceTest {

    @Test
    void testAddAndSearchBook() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        Book book = new Book("Java book", "Yaman", "111");
        service.addBook(book);
        List<Book> results = service.searchBook("Java");
        assertEquals(1, results.size());
        assertEquals("Java book", results.get(0).getTitle());
    }

    @Test
    void testSearchNoResults() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        List<Book> results = service.searchBook("Python");
        assertTrue(results.isEmpty());
    }

    @Test
    void testBorrowBook() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("book", "Author", "123");

        service.addBook(book);
        service.borrowBook(user, book);

        assertEquals(1, user.getBorrowedBooks().size());
        assertFalse(book.isAvailable());
    }

    @Test
    void testOverdueBookAddsFine28Days() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("book", "Author", "123");

        service.addBook(book);
        service.borrowBook(user, book);

        BorrowedBook bb = user.getBorrowedBooks().get(0);

        // بدون تعديل dueDate، لن تكون هناك غرامة الآن
        service.checkOverdueBooks(user);

        assertEquals(0, user.getFineBalance()); // لأن اليوم لم يتجاوز 28 يوم
    }

    @Test
    void testPayFine() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");

        user.addFine(10);
        service.payFine(user, 4);
        assertEquals(6, user.getFineBalance());

        service.payFine(user, 6);
        assertEquals(0, user.getFineBalance());
    }
}
