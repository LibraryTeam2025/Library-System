package librarymanagement;

import librarymanagement.domain.*;
import librarymanagement.application.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    private static final String TEST_ADMINS = "test_admins.txt";
    private static final String TEST_USERS = "test_users.txt";
    private static final String TEST_BORROWED = "test_borrowed.txt";

    private AdminService adminService;
    private UserService userService;
    private EmailService emailService;
    private LibraryService libraryService;

    @BeforeEach
    void setUp() throws IOException {
        clearFiles();

        writeFile(TEST_ADMINS,
                "OwnerAdmin|owner@lib.com|owner123|OWNER\n" +
                        "smalladmin|small@lib.com|small456|SMALL_ADMIN\n" +
                        "Ahmad|ahmad@lib.com|pass789|SMALL_ADMIN\n");

        adminService = new AdminService(TEST_ADMINS);
        emailService = new EmailService();
        userService = new UserService(TEST_USERS, TEST_BORROWED);
        libraryService = new LibraryService(emailService, userService);
        userService.setLibraryService(libraryService);
        userService.loadBorrowedMedia();
    }

    @AfterEach
    void tearDown() {
        clearFiles();
    }

    private void clearFiles() {
        new File(TEST_ADMINS).delete();
        new File(TEST_USERS).delete();
        new File(TEST_BORROWED).delete();
        new File("books.txt").delete();
        new File("cds.txt").delete();
        new File("users_fines.txt").delete();
    }

    private void writeFile(String path, String content) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.write(content);
        }
    }

    @Test
    void testAdminLoginSuccess() {
        Admin admin = adminService.login("owner@lib.com", "owner123");
        assertNotNull(admin);
        assertTrue(admin.isLoggedIn());
        assertTrue(admin.isOwner());
    }

    @Test
    void testAdminLoginWithName() {
        Admin admin = adminService.login("AhMaD", "pass789");
        assertNotNull(admin);
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testAdminLogout() {
        Admin admin = adminService.login("smalladmin", "small456");
        admin.logout();
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testAdminToString() {
        Admin admin = adminService.login("OwnerAdmin", "owner123");
        assertEquals("OwnerAdmin (OWNER)", admin.toString());
    }
    @Test
    void testAddUserAndLogin() {
        boolean added = userService.addUser("Khalid", "123456", "khalid@mail.com");
        assertTrue(added);

        LibraryUser user = userService.login("Khalid", "123456");
        assertNotNull(user);
        assertEquals("khalid@mail.com", user.getEmail());
    }

    @Test
    void testCannotAddDuplicateUser() {
        userService.addUser("Sara", "pass", "sara@mail.com");
        boolean addedAgain = userService.addUser("SARA", "pass", "other@mail.com");
        assertFalse(addedAgain);
    }

    @Test
    void testBorrowBookSuccess() {
        userService.addUser("Ali", "123", "ali@mail.com");
        LibraryUser user = userService.getUserByName("Ali");

        Book book = new Book("Clean Code", "Robert Martin", "B001", 3);
        libraryService.addMedia(book);

        assertTrue(libraryService.borrowMedia(user, book));
        assertEquals(1, user.getBorrowedMedia().size());
        assertEquals(2, book.getAvailableCopies());
    }

    @Test
    void testCannotBorrowWhenFineExists() {
        userService.addUser("Nora", "abc", "nora@mail.com");
        LibraryUser user = userService.getUserByName("Nora");

        Book book = new Book("Java", "Gosling", "J001", 1);
        libraryService.addMedia(book);

        user.addFine(100.0);

        assertFalse(libraryService.borrowMedia(user, book));
        assertEquals(0, user.getBorrowedMedia().size());
    }

    @Test
    void testOverdueFineAddedOnlyOnce() {
        userService.addUser("Omar", "xyz", "omar@mail.com");
        LibraryUser user = userService.getUserByName("Omar");

        Book book = new Book("Effective Java", "Bloch", "EJ001", 1);
        libraryService.addMedia(book);
        libraryService.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(4));
        bm.setFineAdded(false);

        libraryService.checkOverdueMedia(user);
        assertEquals(40.0, user.getFineBalance(), 0.01);

        libraryService.checkOverdueMedia(user);
        assertEquals(40.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testPayFineReducesBalance() {
        userService.addUser("Lama", "pass", "lama@mail.com");
        LibraryUser user = userService.getUserByName("Lama");

        user.addFine(150.0);

        libraryService.payFine(user, 100.0);
        assertEquals(50.0, user.getFineBalance(), 0.01);

        libraryService.payFine(user, 50.0);
        assertEquals(0.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testAddCD() {
        CD cd = new CD("Thriller", "Michael Jackson", "CD001", 5);
        assertTrue(libraryService.addMedia(cd));
    }
    @Test
    void testAdminLogin_WrongPassword_Fails_ButKeepsCurrentSession() {
        Admin admin = adminService.login("owner@lib.com", "owner123");
        assertTrue(admin.isLoggedIn());

        boolean result = admin.login("owner@lib.com", "totally_wrong");
        assertFalse(result);
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testAdminLogin_WrongPassword_WithName_Fails_ButKeepsLoggedIn() {
        Admin admin = adminService.login("smalladmin", "small456");
        assertTrue(admin.isLoggedIn());

        boolean result = admin.login("SMALLadmin", "wrong_pass");
        assertFalse(result);
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testAdminLogin_NonExistentIdentifier_Fails_OnExistingAdmin() {
        Admin admin = adminService.login("Ahmad", "pass789");
        assertTrue(admin.isLoggedIn());

        boolean result = admin.login("GhostUser", "pass789");
        assertFalse(result);
        assertTrue(admin.isLoggedIn());
    }
    @Test
    void testAdminRoles_BothOwnerAndSmallAdmin() {
        Admin owner = adminService.login("owner@lib.com", "owner123");
        Admin small = adminService.login("smalladmin", "small456");

        assertAll("Owner checks",
                () -> assertTrue(owner.isOwner()),
                () -> assertFalse(owner.isSmallAdmin())
        );

        assertAll("Small Admin checks",
                () -> assertFalse(small.isOwner()),
                () -> assertTrue(small.isSmallAdmin())
        );
    }
    @Test
    void testGetRole_ReturnsCorrectRoleForAllAdmins() {
        Admin owner = adminService.login("OwnerAdmin", "owner123");
        Admin small1 = adminService.login("smalladmin", "small456");
        Admin small2 = adminService.login("Ahmad", "pass789");

        assertAll("Admin roles are correctly returned",
                () -> assertEquals(Admin.Role.OWNER, owner.getRole()),
                () -> assertEquals(Admin.Role.SMALL_ADMIN, small1.getRole()),
                () -> assertEquals(Admin.Role.SMALL_ADMIN, small2.getRole())
        );
    }
    @Test
    void testAdminRoleMethods_FullCoverage() {
        Admin owner = adminService.login("OwnerAdmin", "owner123");
        Admin small = adminService.login("Ahmad", "pass789");

        assertAll("Owner Admin",
                () -> assertEquals(Admin.Role.OWNER, owner.getRole()),
                () -> assertTrue(owner.isOwner()),
                () -> assertFalse(owner.isSmallAdmin())
        );

        assertAll("Small Admin",
                () -> assertEquals(Admin.Role.SMALL_ADMIN, small.getRole()),
                () -> assertFalse(small.isOwner()),
                () -> assertTrue(small.isSmallAdmin())
        );
    }
    @Test
    void coverGetRoleMethodExplicitly() {
        assertEquals(Admin.Role.OWNER, adminService.login("owner@lib.com", "owner123").getRole());
        assertEquals(Admin.Role.SMALL_ADMIN, adminService.login("smalladmin", "small456").getRole());
    }
}