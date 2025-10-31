package librarymanagement;

import librarymanagement.domain.*;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;
import org.junit.jupiter.api.*;
import java.io.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    private static final String TEST_FILE = "test_admins.txt";

    @BeforeEach
    void setup() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE))) {
            writer.write("soft,123\n");
            writer.write("roa,456\n");
        }
    }

    @AfterEach
    void cleanup() {
        File file = new File(TEST_FILE);
        if (file.exists()) file.delete();
    }

    @Test
    void testLoginSuccess() {
        AdminService adminService = new AdminService(TEST_FILE);
        Admin admin = adminService.login("soft", "123");
        assertNotNull(admin);
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testLoginFailure() {
        AdminService adminService = new AdminService(TEST_FILE);
        Admin admin = adminService.login("wrong", "999");
        assertNull(admin);
    }

    @Test
    void testLogout() {
        AdminService adminService = new AdminService(TEST_FILE);
        Admin admin = adminService.login("soft", "123");
        assertNotNull(admin);
        admin.logout();
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testBorrowMediaSuccess() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");
        Media book = new Book("Book", "Author", "123");
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
        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.setDueDate(LocalDate.now().minusDays(1));
        assertFalse(service.borrowMedia(user, media2));
    }

    @Test
    void testBorrowMediaFailsWithUnpaidFines() {
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);
        LibraryUser user = new LibraryUser("Roa");
        Media media = new Book("Book1", "Author", "101");
        service.addMedia(media);
        user.addFine(10);
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
        bm.setDueDate(LocalDate.now().minusDays(1));
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