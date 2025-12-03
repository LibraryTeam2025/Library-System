package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private static EmailService emailService;
    private static UserService userService;
    private LibraryService service;

    private static final String BOOKS_FILE = "books.txt";
    private static final String CDS_FILE = "cds.txt";
    private static final String FINES_FILE = "users_fines.txt";

    @BeforeAll
    static void setUpOnce() {
        emailService = new EmailService() {
            private final List<String> sentMessages = new ArrayList<>();

            @Override
            public void sendEmail(String toEmail, String subject, String message) {
                sentMessages.add(message);
            }

            public List<String> getSentMessages() {
                return new ArrayList<>(sentMessages);
            }
        };

        userService = new UserService("test_users.txt", null) {
            @Override public void loadBorrowedMedia() {}
            @Override public void saveBorrowedMedia() {}
            @Override public void saveUsers() {}
          public void loadUsers() {}
        };
    }

    @BeforeEach
    void setUp() {
        deleteIfExists(BOOKS_FILE);
        deleteIfExists(CDS_FILE);
        deleteIfExists(FINES_FILE);
        deleteIfExists("test_users.txt");

        service = new LibraryService(emailService, userService);

        clearPrivateList(service, "mediaList");
        clearPrivateList(service, "users");
    }

    @AfterEach
    void tearDown() {
        deleteIfExists(BOOKS_FILE);
        deleteIfExists(CDS_FILE);
        deleteIfExists(FINES_FILE);
        deleteIfExists("test_users.txt");
    }

    private void deleteIfExists(String fileName) {
        File file = new File(fileName);
        if (file.exists()) file.delete();
    }

    private void clearPrivateList(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            ((List<?>) field.get(obj)).clear();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear " + fieldName, e);
        }
    }

    @Test
    void testAddAndSearchBook() {
        Book book = new Book("Java", "Yaman", "111");
        assertTrue(service.addMedia(book));
        assertFalse(service.addMedia(book));

        Media found = service.getMediaById("111");
        assertNotNull(found);
        assertEquals("Java", found.getTitle());
    }

    @Test
    void testSearchNoResults() {
        assertNull(service.getMediaById("xyz"));
    }

    @Test
    void testBorrowCD() {
        LibraryUser user = new LibraryUser("Roa", "password", "roa@example.com");
        service.addUser(user);

        CD cd = new CD("Hits", "Art", "CD1");
        service.addMedia(cd);

        assertTrue(service.borrowMedia(user, cd));

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        assertEquals(LocalDate.now().plusDays(7), bm.getDueDate());
        assertFalse(cd.isAvailable());
    }

    @Test
    void testReturnMedia() {
        LibraryUser user = new LibraryUser("Roa", "pass", "r@example.com");
        service.addUser(user);

        Book book = new Book("X", "Y", "1");
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        service.returnMedia(user, bm);

        assertTrue(book.isAvailable());
    }

    @Test
    void testCannotBorrowWithFine() {
        LibraryUser user = new LibraryUser("Roa", "pass", "r@example.com");
        service.addUser(user);
        user.addFine(10.0);

        Book book = new Book("Java", "Y", "1");
        service.addMedia(book);

        assertFalse(service.borrowMedia(user, book));
    }

    @Test
    void testPayFine() {
        LibraryUser user = new LibraryUser("Roa", "pass", "r@example.com");
        service.addUser(user);
        user.addFine(25.0);

        service.payFine(user, 15.0);
        assertEquals(10.0, user.getFineBalance(), 0.01);

        service.payFine(user, 10.0);
        assertEquals(0.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testSendReminder() throws Exception {
        LibraryUser user = new LibraryUser("Roa", "pass", "r@example.com");
        service.addUser(user);

        Book book = new Book("Old", "X", "1");
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);

        Field dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(bm, LocalDate.now().minusDays(5));

        Field fineAddedField = BorrowedMedia.class.getDeclaredField("fineAdded");
        fineAddedField.setAccessible(true);
        fineAddedField.set(bm, true);

        emailService.getSentMessages().clear();

        service.sendReminder(user);

        List<String> sent = emailService.getSentMessages();
        assertFalse(sent.isEmpty(), "should remember if there is overdue book!");
        assertTrue(sent.get(0).toLowerCase().contains("overdue"));
    }

    @Test
    void testAddMediaValidation() {
        Book invalid = new Book(null, "Author", "2");
        assertFalse(service.addMedia(invalid));

        Book valid = new Book("Title", "Author", "3");
        assertTrue(service.addMedia(valid));
        Book duplicate = new Book("Another", "Author", "3");
        assertFalse(service.addMedia(duplicate));
    }

    @Test
    void testSearchMediaKeyword() {
        Book b1 = new Book("Java Programming", "Yaman", "10");
        Book b2 = new Book("Python Programming", "Zara", "11");
        service.addMedia(b1);
        service.addMedia(b2);

        List<Media> results = service.searchMedia("java");
        assertEquals(1, results.size());
        assertEquals("10", results.get(0).getId());
    }

    @Test
    void testGetAvailableMedia() {
        Book b1 = new Book("A", "X", "101");
        Book b2 = new Book("B", "Y", "102");
        service.addMedia(b1);
        service.addMedia(b2);

        LibraryUser user = new LibraryUser("U", "p", "u@example.com");
        service.addUser(user);

        service.borrowMedia(user, b1);

        List<Media> available = service.getAvailableMedia();
        assertEquals(1, available.size());
        assertEquals("102", available.get(0).getId());
    }
}