package librarymanagement;

import librarymanagement.domain.Admin;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.Book;
import librarymanagement.domain.LibraryUser;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    @Test
    void testLoginSuccess() {
        Admin admin = new Admin("soft", "123");
        assertTrue(admin.login("soft", "123"));
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testLoginFailure() {
        Admin admin = new Admin("soft", "123");
        assertFalse(admin.login("wrong", "123"));
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testLogout() {
        Admin admin = new Admin("soft", "123");
        admin.login("soft", "123");
        admin.logout();
        assertFalse(admin.isLoggedIn());
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
        Book book = new Book("Java", "Author", "001");

        service.addBook(book);
        service.borrowBook(user, book);

        BorrowedBook bb = user.getBorrowedBooks().get(0);

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
