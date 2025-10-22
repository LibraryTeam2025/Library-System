package librarymanagement;

import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;
import librarymanagement.domain.Book;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.LibraryUser;
import librarymanagement.domain.Admin;
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
        assertTrue(service.addBook(book));  // يضيف الكتاب بنجاح
        assertFalse(service.addBook(book)); // لا يمكن إضافة نفس ISBN مرة ثانية

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

        // استعارة ناجحة
        assertTrue(service.borrowBook(user, book));
        assertEquals(1, user.getBorrowedBooks().size());
        assertFalse(book.isAvailable());

        // محاولة استعارة نفس الكتاب مرة ثانية تفشل
        assertFalse(service.borrowBook(user, book));
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
        Book book1 = new Book("Book1", "Author", "101");

        service.addBook(book1);

        user.addFine(10); // غرامة غير مدفوعة
        assertFalse(service.borrowBook(user, book1));
    }

    @Test
    void testCheckOverdueBooksAddsFine() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("book", "Author", "123");

        service.addBook(book);
        service.borrowBook(user, book);

        // نجعل الكتاب متأخر
        BorrowedBook bb = user.getBorrowedBooks().get(0);
        bb.setDueDate(LocalDate.now().minusDays(1));

        service.checkOverdueBooks(user);

        assertEquals(5, user.getFineBalance()); // يجب إضافة الغرامة
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
        service.addBook(book);
        service.borrowBook(user2, book);

        assertFalse(service.unregisterUser(admin, user2)); // فشل بسبب loan

        LibraryUser user3 = new LibraryUser("Omar");
        service.addUser(user3);
        user3.addFine(10);
        assertFalse(service.unregisterUser(admin, user3)); // فشل بسبب fine
    }
}
