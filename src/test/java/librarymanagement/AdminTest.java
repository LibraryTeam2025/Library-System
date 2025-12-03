package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    private static final String TEST_ADMINS = "test_admins.txt";
    private static final String TEST_USERS = "test_users.txt";
    private static final String TEST_BORROWED = "test_borrowed.txt";
    private static final String TEST_BOOKS = "books.txt";
    private static final String TEST_CDS = "cds.txt";

    private AdminService adminService;
    private UserService userService;
    private EmailService emailService;
    private LibraryService libraryService;

    @BeforeEach
    void setup() throws IOException {
        clearFiles();

        writeFile(TEST_ADMINS,
                "admin1|admin1@mail.com|pass1|OWNER\n" +
                        "admin2|admin2@mail.com|pass2|SMALL_ADMIN\n");

        adminService = new AdminService(TEST_ADMINS);
        emailService = new EmailService();
        userService = new UserService(TEST_USERS, TEST_BORROWED);
        libraryService = new LibraryService(emailService, userService);

        userService.setLibraryService(libraryService);
    }

    @AfterEach
    void cleanup() {
        clearFiles();
    }

    private void clearFiles() {
        new File(TEST_ADMINS).delete();
        new File(TEST_USERS).delete();
        new File(TEST_BORROWED).delete();
        new File(TEST_BOOKS).delete();
        new File(TEST_CDS).delete();
        new File("users_fines.txt").delete();
    }

    private void writeFile(String path, String content) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.write(content);
        }
    }


    @Test
    void testLoginSuccess() {
        Admin admin = adminService.login("admin1@mail.com", "pass1");
        assertNotNull(admin);
        assertTrue(admin.isLoggedIn());
        assertEquals("admin1@mail.com", admin.getEmail());
        assertTrue(admin.isOwner());
    }

    @Test
    void testLoginWithUsername() {
        Admin admin = adminService.login("admin2", "pass2");
        assertNotNull(admin);
        assertTrue(admin.isLoggedIn());
        assertTrue(admin.isSmallAdmin());
    }

    @Test
    void testLoginFailure() {
        assertNull(adminService.login("admin1@mail.com", "wrongpass"));
        assertNull(adminService.login("nonexistent", "pass1"));
    }

    @Test
    void testLogout() {
        Admin admin = adminService.login("admin1@mail.com", "pass1");
        assertNotNull(admin);
        admin.logout();
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testBorrowMediaSuccess() {
        LibraryUser user = new LibraryUser("Roa", "123", "roa@mail.com");
        userService.addUser(user.getName(), user.getPassword(), user.getEmail());
        user = userService.getUserByName("Roa");

        Book book = new Book("Java Programming", "Oracle", "B001");
        assertTrue(libraryService.addMedia(book));

        assertTrue(libraryService.borrowMedia(user, book));
        assertEquals(1, user.getBorrowedMedia().size());
        assertFalse(book.isAvailable());
    }

    @Test
    void testBorrowMediaFailsWithOverdue() throws Exception {
        LibraryUser user = new LibraryUser("Roa", "123", "r@mail.com");
        userService.addUser(user.getName(), user.getPassword(), user.getEmail());
        user = userService.getUserByName("Roa");

        Book b1 = new Book("Book1", "Author", "1");
        CD c1 = new CD("CD1", "Artist", "2");
        libraryService.addMedia(b1);
        libraryService.addMedia(c1);

        libraryService.borrowMedia(user, b1);
        BorrowedMedia borrowed = user.getBorrowedMedia().get(0);

        Field dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(borrowed, LocalDate.now().minusDays(5));

        Field borrowDateField = BorrowedMedia.class.getDeclaredField("borrowDate");
        borrowDateField.setAccessible(true);
        borrowDateField.set(borrowed, LocalDate.now().minusDays(30));

        Field fineAddedField = BorrowedMedia.class.getDeclaredField("fineAdded");
        fineAddedField.setAccessible(true);
        fineAddedField.set(borrowed, false);

        libraryService.checkOverdueMedia(user);

        assertEquals(50.0, user.getFineBalance(), 0.01); // 5 أيام × 10 = 50
        assertFalse(libraryService.borrowMedia(user, c1));
    }

    @Test
    void testBorrowMediaFailsWithUnpaidFines() {
        LibraryUser user = new LibraryUser("Roa", "123", "r@mail.com");
        userService.addUser(user.getName(), user.getPassword(), user.getEmail());
        user = userService.getUserByName("Roa");

        Book book = new Book("Java", "Author", "1");
        libraryService.addMedia(book);

        user.addFine(15.0);

        assertFalse(libraryService.borrowMedia(user, book));
        assertEquals(15.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testOverdueMediaAddsFineOnce() throws Exception {
        LibraryUser user = new LibraryUser("Roa", "123", "r@mail.com");
        userService.addUser(user.getName(), user.getPassword(), user.getEmail());
        user = userService.getUserByName("Roa");

        Book book = new Book("Java", "Author", "1");
        libraryService.addMedia(book);
        libraryService.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);

        Field dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(bm, LocalDate.now().minusDays(4));

        Field borrowDateField = BorrowedMedia.class.getDeclaredField("borrowDate");
        borrowDateField.setAccessible(true);
        borrowDateField.set(bm, LocalDate.now().minusDays(25));

        Field fineAddedField = BorrowedMedia.class.getDeclaredField("fineAdded");
        fineAddedField.setAccessible(true);
        fineAddedField.set(bm, false);

        libraryService.checkOverdueMedia(user);
        assertEquals(40.0, user.getFineBalance(), 0.01); // 4 أيام × 10

        libraryService.checkOverdueMedia(user);
        assertEquals(40.0, user.getFineBalance(), 0.01);
    }


    @Test
    void testPayFine() {
        LibraryUser user = new LibraryUser("Roa", "123", "r@mail.com");
        userService.addUser(user.getName(), user.getPassword(), user.getEmail());
        user = userService.getUserByName("Roa");

        user.addFine(75.0);

        libraryService.payFine(user, 50.0);
        assertEquals(25.0, user.getFineBalance(), 0.01);

        libraryService.payFine(user, 30.0);
        assertEquals(0.0, user.getFineBalance(), 0.01);
    }
}