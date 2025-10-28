package librarymanagement;

import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;
import librarymanagement.domain.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LibraryServiceTest {

    @Test
    void testAddAndSearchBook() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        Book book = new Book("Java book", "Yaman", "111");
        assertTrue(service.addMedia(book));  // يضيف الكتاب بنجاح
        assertFalse(service.addMedia(book)); // لا يمكن إضافة نفس ISBN مرة ثانية

        List<Media> results = service.searchMedia("Java");
        assertEquals(1, results.size());
        assertEquals("Java book", results.get(0).getTitle());
    }

    @Test
    void testSearchNoResults() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        List<Media> results = service.searchMedia("Python");
        assertTrue(results.isEmpty());
    }

    @Test
    void testBorrowBook() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("book", "Author", "123");

        service.addMedia(book);

        // استعارة ناجحة
        assertTrue(service.borrowMedia(user, book));
        assertEquals(1, user.getBorrowedMedia().size());
        assertFalse(book.isAvailable());

        // محاولة استعارة نفس الكتاب مرة ثانية تفشل
        assertFalse(service.borrowMedia(user, book));
    }

    @Test
    void testBorrowBookFailsWithOverdue() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book1 = new Book("Book1", "Author", "101");
        Book book2 = new Book("Book2", "Author", "102");

        service.addMedia(book1);
        service.addMedia(book2);

        service.borrowMedia(user, book1);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(1)); // الكتاب متأخر

        // استعارة أخرى تفشل
        assertFalse(service.borrowMedia(user, book2));
    }

    @Test
    void testBorrowBookFailsWithUnpaidFines() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book1 = new Book("Book1", "Author", "101");

        service.addMedia(book1);
        user.addFine(10); // غرامة غير مدفوعة

        assertFalse(service.borrowMedia(user, book1));
    }

    @Test
    void testCheckOverdueMediaAddsFine() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("book", "Author", "123");

        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(1));

        service.checkOverdueMedia(user);

        assertEquals(10, user.getFineBalance());
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

    @Test
    void testUnregisterUser() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        Admin admin = new Admin("admin", "123");
        admin.login("admin", "123");

        LibraryUser user = new LibraryUser("Roa");
        service.addUser(user);

        // إزالة المستخدم ناجحة بدون كتب أو غرامات
        assertTrue(service.unregisterUser(admin, user));

        // محاولة إزالة مستخدم مع كتب مستعارة أو غرامات
        LibraryUser user2 = new LibraryUser("Ali");
        service.addUser(user2);

        Book book = new Book("Book1", "Author", "101");
        service.addMedia(book);
        service.borrowMedia(user2, book);
        assertFalse(service.unregisterUser(admin, user2));

        LibraryUser user3 = new LibraryUser("Omar");
        service.addUser(user3);
        user3.addFine(10);
        assertFalse(service.unregisterUser(admin, user3));
    }

    // ✅ Sprint 5: Borrow CD test
    @Test
    void testBorrowCD() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        CD cd = new CD("Top Hits", "Various Artists", "CD001");

        service.addMedia(cd);
        service.borrowMedia(user, cd);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);

        assertEquals(LocalDate.now().plusDays(7), bm.getDueDate()); // مدة الاستعارة 7 أيام
        assertFalse(cd.isAvailable()); // السي دي غير متاح بعد الاستعارة
    }
}