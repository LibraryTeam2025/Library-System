package librarymanagement;

import librarymanagement.domain.Book;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.LibraryUser;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class BookTest {

    @Test
    void testBookCreation() {
        Book book = new Book("book", "Yaman", "111");
        assertEquals("book", book.getTitle());
        assertEquals("Yaman", book.getAuthor());
        assertEquals("111", book.getIsbn());
        assertTrue(book.isAvailable());
    }

    @Test
    void testAvailabilityToggle() {
        Book book = new Book("database", "Yaman", "111");
        book.setAvailable(false);
        assertFalse(book.isAvailable());
        book.setAvailable(true);
        assertTrue(book.isAvailable());
    }

    @Test
    void testToString() {
        Book book = new Book("database", "Yaman", "111");
        String expected = "database by Yaman (ISBN: 111)";
        assertEquals(expected, book.toString());
    }

    @Test
    void testBorrowedBookAvailability() {
        Book book = new Book("siber", "Yaman", "111");
        LibraryUser user = new LibraryUser("Roa");
        BorrowedBook borrowedBook = new BorrowedBook(book);
        user.getBorrowedBooks().add(borrowedBook);

        assertFalse(book.isAvailable()); // بعد استعارة الكتاب، غير متاح
        borrowedBook.returnBook();
        assertTrue(book.isAvailable());  // بعد إرجاع الكتاب، متاح
    }

    @Test
    void testBorrowedBookDueDate() {
        Book book = new Book("Basic in Python", "Yaman", "111");
        BorrowedBook borrowedBook = new BorrowedBook(book);

        LocalDate expectedDueDate = LocalDate.now().plusDays(28);
        assertEquals(expectedDueDate, borrowedBook.getDueDate());
    }

    @Test
    void testLibraryServiceBorrowBook() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("Java", "Yaman", "111");
        service.addBook(book);

        service.borrowBook(user, book);

        assertEquals(1, user.getBorrowedBooks().size());
        assertFalse(book.isAvailable());
    }
}
