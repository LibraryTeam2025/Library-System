package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    private static final String TEST_ADMINS = "test_admins.txt";
    private static final String TEST_USERS = "test_users.txt";
    private static final String TEST_BOOKS = "books.txt";
    private static final String TEST_CDS = "cds.txt";

    private AdminService adminService;
    private UserService userService;
    private EmailService emailService;
    private LibraryService libraryService;

    @BeforeEach
    void setup() throws IOException {
        clearFiles();

        // إعداد ملف الأدمن
        writeFile(TEST_ADMINS, "admin1,pass1\nadmin2,pass2\n");

        adminService = new AdminService(TEST_ADMINS);
        userService = new UserService(TEST_USERS);
        emailService = new EmailService();
        libraryService = new LibraryService(emailService, userService);

        // تحميل المستخدمين
        libraryService.getUsers().clear();
        userService.getUsers().forEach(libraryService::addUser);
    }

    @AfterEach
    void cleanup() {
        clearFiles();
    }

    private void clearFiles() {
        new File(TEST_ADMINS).delete();
        new File(TEST_USERS).delete();
        new File(TEST_BOOKS).delete();
        new File(TEST_CDS).delete();
    }

    private void writeFile(String path, String content) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.write(content);
        }
    }

    @Test
    void testLoginSuccess() {
        Admin admin = adminService.login("admin1", "pass1");
        assertNotNull(admin);
        assertTrue(admin.isLoggedIn());
        assertEquals("admin1", admin.getUsername());
    }

    @Test
    void testLoginFailure() {
        assertNull(adminService.login("admin1", "wrong"));
        assertNull(adminService.login("ghost", "pass1"));
    }

    @Test
    void testLogout() {
        Admin admin = adminService.login("admin1", "pass1");
        admin.logout();
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testBorrowMediaSuccess() {
        LibraryUser user = new LibraryUser("Roa", "123");
        userService.addUser("Roa", "123");
        libraryService.addUser(user);

        Book book = new Book("Java", "Oracle", "B001");
        assertTrue(libraryService.addMedia(book));
        assertTrue(libraryService.borrowMedia(user, book));

        assertEquals(1, user.getBorrowedMedia().size());
        assertFalse(book.isAvailable());
        assertEquals(LocalDate.now().plusDays(28), user.getBorrowedMedia().get(0).getDueDate());
    }

    @Test
    void testBorrowMediaFailsWithOverdue() {
        LibraryUser user = new LibraryUser("Roa", "123");
        userService.addUser("Roa", "123");
        libraryService.addUser(user);

        Book b1 = new Book("B1", "A", "1");
        CD c1 = new CD("C1", "A", "2");
        libraryService.addMedia(b1);
        libraryService.addMedia(c1);

        libraryService.borrowMedia(user, b1);
        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(1)); // متأخر

        assertFalse(libraryService.borrowMedia(user, c1));
    }

    @Test
    void testBorrowMediaFailsWithUnpaidFines() {
        LibraryUser user = new LibraryUser("Roa", "123");
        userService.addUser("Roa", "123");
        libraryService.addUser(user);

        Book book = new Book("Java", "A", "1");
        libraryService.addMedia(book);
        user.addFine(10);

        assertFalse(libraryService.borrowMedia(user, book));
    }

    @Test
    void testOverdueMediaAddsFineOnce() {
        LibraryUser user = new LibraryUser("Roa", "123");
        userService.addUser("Roa", "123");
        libraryService.addUser(user);

        Book book = new Book("Java", "A", "1");
        libraryService.addMedia(book);
        libraryService.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(1));

        libraryService.checkOverdueMedia(user);
        assertEquals(10.0, user.getFineBalance());
        assertTrue(bm.isFineAdded());

        // لا يضيف غرامة مرة أخرى
        libraryService.checkOverdueMedia(user);
        assertEquals(10.0, user.getFineBalance());
    }

    @Test
    void testPayFine() {
        LibraryUser user = new LibraryUser("Roa");
        user.addFine(20);
        libraryService.payFine(user, 15);
        assertEquals(5.0, user.getFineBalance());
        libraryService.payFine(user, 10);
        assertEquals(0.0, user.getFineBalance());
    }

    @Test
    void testUnregisterUserSuccess() {
        Admin admin = adminService.login("admin1", "pass1");
        LibraryUser user = new LibraryUser("Roa");
        libraryService.addUser(user);

        assertTrue(libraryService.unregisterUser(admin, user));
        assertFalse(libraryService.getUsers().contains(user));
    }

    @Test
    void testUnregisterUserFailsWithActiveLoans() {
        Admin admin = adminService.login("admin1", "pass1");
        LibraryUser user = new LibraryUser("Roa");
        libraryService.addUser(user);

        Book book = new Book("Java", "A", "1");
        libraryService.addMedia(book);
        libraryService.borrowMedia(user, book);

        assertFalse(libraryService.unregisterUser(admin, user));
    }

    @Test
    void testUnregisterUserFailsWithFines() {
        Admin admin = adminService.login("admin1", "pass1");
        LibraryUser user = new LibraryUser("Roa");
        user.addFine(10);
        libraryService.addUser(user);

        assertFalse(libraryService.unregisterUser(admin, user));
    }
}