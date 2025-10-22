package librarymanagement;

import librarymanagement.domain.Admin;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.Book;
import librarymanagement.domain.LibraryUser;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
    void testBorrowBookSuccess() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("book", "Author", "123");

        service.addBook(book);
        boolean borrowed = service.borrowBook(user, book);

        assertTrue(borrowed);
        assertEquals(1, user.getBorrowedBooks().size());
        assertFalse(book.isAvailable());
    }

    @Test
    void testBorrowBookFailsWithOverdue() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");

        Book book1 = new Book("Book1", "Author", "101");
        Book book2 = new Book("Book2", "Author", "102");

        service.addBook(book1);
        service.addBook(book2);

        service.borrowBook(user, book1);

        // نجعل الكتاب متأخر
        BorrowedBook bb = user.getBorrowedBooks().get(0);
        bb.setDueDate(LocalDate.now().minusDays(1));

        // محاولة استعارة كتاب آخر تفشل بسبب overdue
        assertFalse(service.borrowBook(user, book2));
    }

    @Test
    void testBorrowBookFailsWithUnpaidFines() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");

        Book book = new Book("Book1", "Author", "101");
        service.addBook(book);

        user.addFine(10); // غرامة غير مدفوعة
        assertFalse(service.borrowBook(user, book));
    }

    @Test
    void testOverdueBookAddsFine() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");

        Book book = new Book("Java", "Author", "001");
        service.addBook(book);
        service.borrowBook(user, book);

        BorrowedBook bb = user.getBorrowedBooks().get(0);
        bb.setDueDate(LocalDate.now().minusDays(1)); // متأخر

        service.checkOverdueBooks(user);
        assertEquals(5, user.getFineBalance());
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
