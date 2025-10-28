package librarymanagement;

import librarymanagement.domain.*;
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
        assertEquals("111", book.getId());
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
        String expected = "database by Yaman (ID: 111)";
        assertEquals(expected, book.toString());
    }

    @Test
    void testBorrowedMediaAvailability() {
        Book book = new Book("siber", "Yaman", "111");
        LibraryUser user = new LibraryUser("Roa");
        BorrowedMedia borrowedMedia = new BorrowedMedia(book);
        user.getBorrowedMedia().add(borrowedMedia);

        assertFalse(book.isAvailable()); // بعد استعارة الكتاب، غير متاح
        borrowedMedia.returnMedia();
        assertTrue(book.isAvailable());  // بعد إرجاع الكتاب، متاح
    }

    @Test
    void testBorrowedMediaDueDate() {
        Book book = new Book("Basic in Python", "Yaman", "111");
        BorrowedMedia borrowedMedia = new BorrowedMedia(book);

        LocalDate expectedDueDate = LocalDate.now().plusDays(book.getBorrowDays());
        assertEquals(expectedDueDate, borrowedMedia.getDueDate());
    }

    @Test
    void testLibraryServiceBorrowMedia() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("Java", "Yaman", "111");
        service.addMedia(book);

        service.borrowMedia(user, book);

        assertEquals(1, user.getBorrowedMedia().size());
        assertFalse(book.isAvailable());
    }
}