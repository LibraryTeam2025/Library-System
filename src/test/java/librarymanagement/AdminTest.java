package librarymanagement;

import librarymanagement.domain.*;
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
    void testBorrowMediaSuccess() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Media book = new Book("book", "Author", "123");

        service.addMedia(book);
        boolean borrowed = service.borrowMedia(user, book);

        assertTrue(borrowed);
        assertEquals(1, user.getBorrowedMedia().size());
        assertFalse(book.isAvailable());
    }

    @Test
    void testBorrowMediaFailsWithOverdue() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");

        Media media1 = new Book("Book1", "Author", "101");
        Media media2 = new CD("Top Hits", "Various Artists", "CD001");

        service.addMedia(media1);
        service.addMedia(media2);

        service.borrowMedia(user, media1);

        // نجعل الوسيط متأخر
        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(1));

        // محاولة استعارة أخرى تفشل بسبب overdue
        assertFalse(service.borrowMedia(user, media2));
    }

    @Test
    void testBorrowMediaFailsWithUnpaidFines() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");

        Media media = new Book("Book1", "Author", "101");
        service.addMedia(media);

        user.addFine(10); // غرامة غير مدفوعة
        assertFalse(service.borrowMedia(user, media));
    }

    @Test
    void testOverdueMediaAddsFine() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");

        Media media = new Book("Java", "Author", "001");
        service.addMedia(media);
        service.borrowMedia(user, media);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(1)); // متأخر

        service.checkOverdueMedia(user);
        assertEquals(media.getFineAmount(), user.getFineBalance());
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