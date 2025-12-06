package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceExtendedTest {

    private static EmailService emailService;
    private static UserService userService;
    private LibraryService service;

    @BeforeAll
    static void setupOnce() {
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
    void setup() {
        service = new LibraryService(emailService, userService);
        clearPrivateList(service, "mediaList");
        clearPrivateList(service, "users");
    }

    private void clearPrivateList(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            ((List<?>) field.get(obj)).clear();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }

    private int getPrivateListSize(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return ((List<?>) field.get(obj)).size();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Test
    void testBorrowMedia_Safe() {
        LibraryUser user = new LibraryUser("U1", "p", "u1@u.com");
        service.addUser(user);
        Book book = new Book("Title", "Author", "B1", 1);
        service.addMedia(book);

        assertDoesNotThrow(() -> service.borrowMedia(user, book));
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void testBorrowMedia_UserBlocked_Fine() {
        LibraryUser user = new LibraryUser("U2", "p", "u2@u.com");
        user.addFine(5.0);
        service.addUser(user);
        Book book = new Book("Book1", "Author", "B1", 1);
        service.addMedia(book);
        assertFalse(service.borrowMedia(user, book));
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testBorrowMedia_UserBlocked_Manual() {
        LibraryUser user = new LibraryUser("U3", "p", "u3@u.com");
        service.addUser(user);
        try {
            Field blockedField = LibraryUser.class.getDeclaredField("blocked");
            blockedField.setAccessible(true);
            blockedField.setBoolean(user, true);
        } catch (Exception ignored) {}

        Book book = new Book("Book2", "Author", "B2", 1);
        service.addMedia(book);

        assertFalse(service.borrowMedia(user, book));
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testBorrowMedia_NoCopiesLeft() {
        Book book = new Book("Book3", "Author", "B3", 1);
        service.addMedia(book);
        LibraryUser u1 = new LibraryUser("U4", "p", "u4@u.com");
        LibraryUser u2 = new LibraryUser("U5", "p", "u5@u.com");
        service.addUser(u1);
        service.addUser(u2);

        assertTrue(service.borrowMedia(u1, book));
        assertFalse(service.borrowMedia(u2, book));
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void testBorrowMedia_Success() {
        LibraryUser user = new LibraryUser("U6", "p", "u6@u.com");
        service.addUser(user);
        Book book = new Book("Book4", "Author", "B4", 2);
        service.addMedia(book);

        assertTrue(service.borrowMedia(user, book));
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testBorrowMedia_NullUserOrMedia() {
        LibraryUser user = new LibraryUser("U18", "p", "u18@u.com");
        Book book = new Book("Book13", "A", "B13", 1);

        service.addUser(user);
        service.addMedia(book);

        assertFalse(service.borrowMedia(null, book));
        assertFalse(service.borrowMedia(user, null));
        assertFalse(service.borrowMedia(null, null));
    }

    @Test
    void testReturnMedia_UnblocksUserAfterFinePaid() {
        LibraryUser user = new LibraryUser("U7", "p", "u7@u.com");
        service.addUser(user);
        Book book = new Book("Book5", "A", "B5", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        service.checkOverdueMedia(user);
        service.payFine(user, 1000.0);
        service.returnMedia(user, bm);

        assertFalse(user.isBlocked());
        assertEquals(0.0, user.getFineBalance(), 0.001);
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testReturnMedia_NotBorrowed() {
        LibraryUser user = new LibraryUser("U8", "p", "u8@u.com");
        service.addUser(user);

        Book book = new Book("Book6", "A", "B6", 1);
        service.addMedia(book);

        BorrowedMedia fake = new BorrowedMedia(book, LocalDate.now(), LocalDate.now().plusDays(7));
        service.returnMedia(user, fake);

        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testReturnMedia_NullBorrowed() {
        LibraryUser u = new LibraryUser("U19", "p", "u19@u.com");
        service.addUser(u);
        assertThrows(NullPointerException.class, () -> service.returnMedia(u, null));
    }

    @Test
    void testDeleteMedia_NullOrEmpty() {
        assertFalse(service.deleteMedia(null));
        assertFalse(service.deleteMedia(""));
        assertFalse(service.deleteMedia("   "));
    }

    @Test
    void testDeleteMedia_Borrowed() {
        LibraryUser user = new LibraryUser("U9", "p", "u9@u.com");
        service.addUser(user);
        Book book = new Book("Book7", "A", "B7", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        assertFalse(service.deleteMedia("B7"));
    }

    @Test
    void testDeleteMedia_Success() {
        Book book = new Book("Book8", "A", "B8", 1);
        service.addMedia(book);
        assertTrue(service.deleteMedia("B8"));
        assertNull(service.getMediaById("B8"));
    }

    @Test
    void testDeleteMedia_NotExist() {
        assertFalse(service.deleteMedia("NonExist"));
    }

    @Test
    void testPayFine_Partial() {
        LibraryUser user = new LibraryUser("U10", "p", "u10@u.com");
        service.addUser(user);
        user.addFine(50.0);

        service.payFine(user, 20.0);
        assertEquals(30.0, user.getFineBalance(), 0.001);
        assertTrue(user.isBlocked());
    }

    @Test
    void testPayFine_Excess() {
        LibraryUser user = new LibraryUser("U11", "p", "u11@u.com");
        service.addUser(user);
        user.addFine(30.0);

        service.payFine(user, 50.0);
        assertEquals(0.0, user.getFineBalance(), 0.001);
        assertFalse(user.isBlocked());
    }

    @Test
    void testPayFine_NegativeOrZero() {
        LibraryUser user = new LibraryUser("U12", "p", "u12@u.com");
        service.addUser(user);
        user.addFine(10.0);

        service.payFine(user, 0.0);
        assertEquals(10.0, user.getFineBalance(), 0.001);

        service.payFine(user, -5.0);
        assertEquals(10.0, user.getFineBalance(), 0.001);
    }

    @Test
    void testCheckOverdue_AlreadyChecked() {
        LibraryUser user = new LibraryUser("U13", "p", "u13@u.com");
        service.addUser(user);
        Book book = new Book("Book9", "A", "B9", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        service.checkOverdueMedia(user);
        double firstFine = user.getFineBalance();

        service.checkOverdueMedia(user);
        assertEquals(firstFine, user.getFineBalance(), 0.001);
    }

    @Test
    void testCheckOverdue_NotOverdue() {
        LibraryUser user = new LibraryUser("U14", "p", "u14@u.com");
        service.addUser(user);
        Book book = new Book("Book10", "A", "B10", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        service.checkOverdueMedia(user);
        assertEquals(0.0, user.getFineBalance(), 0.001);
    }

    @Test
    void testCheckOverdueMedia_WithOverdue() {
        LibraryUser user = new LibraryUser("U19", "p", "u19@u.com");
        service.addUser(user);
        Book book = new Book("OverdueBook", "A", "B19", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(10));
        } catch (Exception ignored) {}

        service.checkOverdueMedia(user);
        assertTrue(user.getFineBalance() > 0);
        assertTrue(user.isBlocked());
    }

    @Test
    void testSendReminder_NoOverdue() {
        LibraryUser user = new LibraryUser("U15", "p", "u15@u.com");
        service.addUser(user);
        ((List<String>) emailService.getSentMessages()).clear();
        service.sendReminder(user);
        assertTrue(emailService.getSentMessages().isEmpty());
    }

    @Test
    void testSendReminder_EmptyEmail() {
        LibraryUser user = new LibraryUser("NoEmail", "p", "");
        service.addUser(user);
        assertDoesNotThrow(() -> service.sendReminder(user));
    }

    @Test
    void testSendReminder_Success() {
        LibraryUser user = new LibraryUser("U17", "p", "test@t.com");
        service.addUser(user);
        Book book = new Book("Book12", "A", "B12", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        ((List<String>) emailService.getSentMessages()).clear();
        service.sendReminder(user);
        assertFalse(emailService.getSentMessages().isEmpty());
    }

    @Test
    void testSendReminder_WithOverdueAndEmail() {
        LibraryUser user = new LibraryUser("U20", "p", "overdue@u.com");
        service.addUser(user);
        Book book = new Book("RemindBook", "A", "B20", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(3));
        } catch (Exception ignored) {}

        ((List<String>) emailService.getSentMessages()).clear();
        service.sendReminder(user);
        assertFalse(emailService.getSentMessages().isEmpty());
    }

    @Test
    void testSendReminder_NoEmailAndNoOverdue() {
        LibraryUser user = new LibraryUser("NoEmail", "p", "");
        service.addUser(user);
        if (emailService instanceof EmailService es) {
            try {
                Field f = es.getClass().getDeclaredField("sentMessages");
                f.setAccessible(true);
                ((List<?>) f.get(es)).clear();
            } catch (Exception ignored) {}
        }

        service.sendReminder(user);
        assertTrue(emailService.getSentMessages().isEmpty(),
                "No email sent if no email address or no overdue");
    }

    @Test
    void testAddUser_NullChecks() {
        assertDoesNotThrow(() -> {
            service.addUser(null);
            service.addUser(new LibraryUser(null, "pass", "e@e.com"));
            service.addUser(new LibraryUser("Name", null, "e@e.com"));
            service.addUser(new LibraryUser("Name", "pass", null));
        });
    }

    @Test
    void testAddUser_Duplicate() {
        LibraryUser user = new LibraryUser("DupUser", "p", "dup@u.com");
        service.addUser(user);
        int initialSize = service.getUsers().size();

        service.addUser(user);
        assertEquals(initialSize, service.getUsers().size());
    }

    @Test
    void testAddMedia_NullChecks() {
        assertDoesNotThrow(() -> service.addMedia(null));
        assertEquals(0, getPrivateListSize(service, "mediaList"));

        assertDoesNotThrow(() -> service.addMedia(new Book(null, "A", "ID1", 1)));
        assertEquals(0, getPrivateListSize(service, "mediaList"));

        assertDoesNotThrow(() -> service.addMedia(new Book("Title", null, "ID2", 1)));
        assertEquals(0, getPrivateListSize(service, "mediaList"));

        assertDoesNotThrow(() -> service.addMedia(new Book("Title", "A", null, 1)));
        assertEquals(0, getPrivateListSize(service, "mediaList"));
    }

    @Test
    void testAddMedia_Duplicate() {
        Book b = new Book("Dup", "A", "D1", 1);
        assertTrue(service.addMedia(b));
        int initialSize = getPrivateListSize(service, "mediaList");

        assertFalse(service.addMedia(b));
        assertEquals(initialSize, getPrivateListSize(service, "mediaList"));
    }

    @Test
    void testSearchMedia_AllCases() {
        Book book = new Book("Java Basics", "John Doe", "J1", 1);
        Book book2 = new Book("Advanced Java", "Jane Doe", "J2", 1);
        service.addMedia(book);
        service.addMedia(book2);

        assertTrue(service.searchMedia(null).isEmpty());
        assertTrue(service.searchMedia("").isEmpty());
        assertTrue(service.searchMedia("   ").isEmpty());

        assertEquals(2, service.searchMedia("Java").size());
        assertEquals(1, service.searchMedia("Advanced").size());
        assertEquals(1, service.searchMedia("Jane").size());
        assertEquals(0, service.searchMedia("Python").size());
    }

    @Test
    void testReturnMedia_NotBorrowed_MultipleBooks() {
        LibraryUser user = new LibraryUser("U25", "p", "u25@u.com");
        service.addUser(user);

        Book book1 = new Book("BookA", "A", "B25", 1);
        Book book2 = new Book("BookB", "B", "B26", 1);
        service.addMedia(book1);
        service.addMedia(book2);

        service.borrowMedia(user, book1);

        BorrowedMedia fake = new BorrowedMedia(book2, LocalDate.now(), LocalDate.now().plusDays(7));
        service.returnMedia(user, fake);

        assertEquals(0, book1.getAvailableCopies(), "Borrowed book1 copies should decrease");
        assertEquals(1, book2.getAvailableCopies(), "Not borrowed book2 copies should remain 1");
    }

    @Test
    void testSearchMedia_PartialMatch() {
        Book book1 = new Book("Java Programming", "Alice", "J1", 1);
        Book book2 = new Book("Advanced Java Programming", "Bob", "J2", 1);
        service.addMedia(book1);
        service.addMedia(book2);

        List<Media> result = service.searchMedia("Programming");
        assertEquals(2, result.size(), "Should find both books containing 'Programming'");

        result = service.searchMedia("Alice");
        assertEquals(1, result.size(), "Should find book by author Alice");

        result = service.searchMedia("Python");
        assertEquals(0, result.size(), "Should find no books for 'Python'");
    }

    @Test
    void testPayFine_ExactAmount() {
        LibraryUser user = new LibraryUser("U31", "p", "u31@u.com");
        service.addUser(user);
        user.addFine(40.0);

        service.payFine(user, 40.0);
        assertEquals(0.0, user.getFineBalance(), 0.001);
        assertFalse(user.isBlocked());
    }

    @Test
    void testDeleteMedia_NotExistEdge() {
        assertFalse(service.deleteMedia("UNKNOWN_ID"));
    }
    @Test
    void testSendReminder_MultipleOverdue() {
        LibraryUser user = new LibraryUser("U32", "p", "multi@u.com");
        service.addUser(user);

        Book book1 = new Book("B1", "A", "B101", 1);
        Book book2 = new Book("B2", "A", "B102", 1);
        service.addMedia(book1);
        service.addMedia(book2);

        service.borrowMedia(user, book1);
        service.borrowMedia(user, book2);

        try {
            Field due1 = BorrowedMedia.class.getDeclaredField("dueDate");
            Field due2 = BorrowedMedia.class.getDeclaredField("dueDate");
            due1.setAccessible(true);
            due2.setAccessible(true);
            due1.set(user.getBorrowedMediaInternal().get(0), LocalDate.now().minusDays(3));
            due2.set(user.getBorrowedMediaInternal().get(1), LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        ((List<String>) emailService.getSentMessages()).clear();
        service.sendReminder(user);

        assertFalse(emailService.getSentMessages().isEmpty());
    }
    @Test
    void testPayFine_LargeAmount() {
        LibraryUser user = new LibraryUser("UserLarge", "pass", "l@l.com");
        service.addUser(user);
        user.addFine(50.0);

        service.payFine(user, Double.MAX_VALUE);
        assertEquals(0.0, user.getFineBalance(), 0.001, "Fine should be fully cleared");
        assertFalse(user.isBlocked());
    }
    @Test
    void testDeleteMedia_EmptyList() {
        assertFalse(service.deleteMedia("NonExist"), "Deleting from empty list should return false");
    }
    @Test
    void testDeleteMedia_NullAndEmptyList() {
        LibraryService emptyService = new LibraryService(emailService, userService);
        assertFalse(emptyService.deleteMedia("B999")); // empty list
        assertFalse(emptyService.deleteMedia(null));   // null check
    }
    @Test
    void testSendReminder_OverdueNoEmail() {
        LibraryUser user = new LibraryUser("NoEmailOverdue", "p", "");
        service.addUser(user);
        Book book = new Book("BookOverdue", "A", "B200", 1);
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        ((List<String>) emailService.getSentMessages()).clear();
        service.sendReminder(user);
        assertTrue(emailService.getSentMessages().isEmpty());
    }
    @Test
    void testLoadBorrowedMedia_EmptyLineCoverage() throws IOException {

        String usersPath = System.getProperty("java.io.tmpdir") + "/cov_users.txt";
        String borrowedPath = System.getProperty("java.io.tmpdir") + "/cov_borrowed.txt";

        try (PrintWriter pw = new PrintWriter(usersPath)) {
            pw.println("UserX:pass:x@mail.com");
        }

        UserService us = new UserService(usersPath, borrowedPath);
        LibraryService ls = new LibraryService(null, us);
        us.setLibraryService(ls);

        Book b = new Book("Book", "A", "ID1", 1);
        ls.addMedia(b);

        try (PrintWriter pw = new PrintWriter(borrowedPath)) {
            pw.println("");
            pw.println("UserX|ID1|2024-01-01|2024-01-05|false|0.0");
        }

        us.loadBorrowedMedia();

        LibraryUser u = us.getUserByName("UserX");
        assertNotNull(u);
        assertEquals(1, u.getBorrowedMediaInternal().size());
    }
    @Test
    void testLoadBorrowedMedia_PartsLessThan4_SkipsLine() throws IOException {

        String usersPath = System.getProperty("java.io.tmpdir") + "/cov_users.txt";
        String borrowedPath = System.getProperty("java.io.tmpdir") + "/cov_borrowed.txt";

        // create user
        try (PrintWriter pw = new PrintWriter(usersPath)) {
            pw.println("UserX:pass:mail@mail.com");
        }

        UserService us = new UserService(usersPath, borrowedPath);
        LibraryService ls = new LibraryService(null, us);
        us.setLibraryService(ls);

        Book b = new Book("Book", "A", "ID1", 1);
        ls.addMedia(b);

        try (PrintWriter pw = new PrintWriter(borrowedPath)) {
            pw.println("UserX|ID1|2024-01-01");
            pw.println("UserX|ID1|2024-01-01|2024-01-05|false|0.0");
        }

        us.loadBorrowedMedia();

        LibraryUser u = us.getUserByName("UserX");
        assertNotNull(u);

        assertEquals(1, u.getBorrowedMediaInternal().size());
    }
    @Test
    void testLoadMedia_PartsLessThan5_UsesTotalCopies() throws IOException {
        String bookFile = "books.txt";

        try (PrintWriter pw = new PrintWriter(bookFile)) {
            pw.println("B001|Title|Author|7");
        }

        UserService us = new UserService("users.txt", "borrowed.txt");
        LibraryService ls = new LibraryService(null, us);

        Media m = ls.getMediaById("B001");
        assertNotNull(m);

        assertEquals(7, m.getAvailableCopies());
    }
    @Test
    void testLoadMedia_PartsGreaterThanOrEqual5_ReadsAvailableCopies() throws IOException {
        String bookFile = "books.txt";

        try (PrintWriter pw = new PrintWriter(bookFile)) {
            pw.println("B001|Title|Author|10|3");
        }

        UserService us = new UserService("users.txt", "borrowed.txt");
        LibraryService ls = new LibraryService(null, us);

        Media m = ls.getMediaById("B001");
        assertNotNull(m);

        assertEquals(3, m.getAvailableCopies());
    }
    @Test
    void testSaveMediaToFile_BookOnly() throws IOException {
        UserService us = new UserService("users.txt", "borrowed.txt");
        LibraryService ls = new LibraryService(null, us);

        Book b1 = new Book("Title1", "Author1", "B1", 1);
        CD cd1 = new CD("Album1", "Artist1", "C1", 1);

        ls.addMedia(b1);
        ls.addMedia(cd1);
        ls.saveAllMedia();

        File booksFile = new File("books.txt");
        List<String> lines = java.nio.file.Files.readAllLines(booksFile.toPath());

        assertTrue(lines.stream().anyMatch(line -> line.contains("B1")));
        assertFalse(lines.stream().anyMatch(line -> line.contains("C1")));
    }

    @Test
    void testSaveMediaToFile_CDOnly() throws IOException {
        UserService us = new UserService("users.txt", "borrowed.txt");
        LibraryService ls = new LibraryService(null, us);

        CD cd1 = new CD("Album1", "Artist1", "C1", 1);
        Book b1 = new Book("Title1", "Author1", "B1", 1);

        ls.addMedia(cd1);
        ls.addMedia(b1);
        ls.saveAllMedia();

        File cdsFile = new File("cds.txt");
        List<String> lines = java.nio.file.Files.readAllLines(cdsFile.toPath());

        assertTrue(lines.stream().anyMatch(line -> line.contains("C1")));
        assertFalse(lines.stream().anyMatch(line -> line.contains("B1")));
    }

    @Test
    void testPayFine_NullUserOrNonPositiveAmount() {
        LibraryUser user = new LibraryUser("U1", "pass", "u1@u.com");
        service.addUser(user);
        user.addFine(50.0);
        service.payFine(user, 0.0);
        assertEquals(50.0, user.getFineBalance(), 0.001);

        service.payFine(user, -10.0);
        assertEquals(50.0, user.getFineBalance(), 0.001);
        assertDoesNotThrow(() -> service.payFine(null, 10.0));
    }
    @Test
    void testPayFine_SetsFineAddedForOverdue() {
        LibraryUser user = new LibraryUser("U2", "pass", "u2@u.com");
        service.addUser(user);

        Book book = new Book("Test Book", "Author", "B001", 1);
        service.addMedia(book);

        service.borrowMedia(user, book);
        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);

        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        assertFalse(bm.isFineAdded());

        service.payFine(user, 10.0);
        assertTrue(bm.isFineAdded(), "Fine should be marked as added for overdue item");
    }
    @Test
    void testReturnMedia_UnblocksUserAfterReturnAndFinePaid() {
        LibraryUser user = new LibraryUser("U3", "pass", "u3@u.com");
        service.addUser(user);

        Book book = new Book("BookX", "Author", "BX1", 1);
        service.addMedia(book);

        service.borrowMedia(user, book);
        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);

        try {
            Field dueField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueField.setAccessible(true);
            dueField.set(bm, LocalDate.now().minusDays(5));
        } catch (Exception ignored) {}

        service.checkOverdueMedia(user);

        service.payFine(user, user.getFineBalance());

        service.returnMedia(user, bm);

        assertFalse(user.isBlocked(), "User should be unblocked after returning all items and paying fines");
    }
    @Test
    void testBorrowMedia_BlockedOrWithFine() {
        LibraryUser user = new LibraryUser("U4", "pass", "u4@u.com");
        user.addFine(10.0);
        service.addUser(user);

        Book book = new Book("BookY", "Author", "BY1", 1);
        service.addMedia(book);
        assertFalse(service.borrowMedia(user, book));

        user.setBlocked(true);
        assertFalse(service.borrowMedia(user, book));
    }
    @Test
    void testGetAvailableMedia() {
        Book book1 = new Book("Book1", "Author1", "B1", 1);
        Book book2 = new Book("Book2", "Author2", "B2", 1);
        service.addMedia(book1);
        service.addMedia(book2);

        LibraryUser user = new LibraryUser("U1", "pass", "u1@u.com");
        service.addUser(user);

        service.borrowMedia(user, book1);

        List<Media> available = service.getAvailableMedia();
        assertTrue(available.contains(book2), "Book2 should be available");
        assertFalse(available.contains(book1), "Book1 should not be available");
    }
    @Test
    void testGetUserByName_NullInput() {
        LibraryUser result = service.getUserByName(null);
        assertNull(result, "Should return null if name is null");
    }
    @Test
    void testGetAllMedia_EmptyAndNonEmpty() {

        List<Media> result = service.getAllMedia();
        assertNotNull(result, "Should not return null even if media list is empty");
        assertTrue(result.isEmpty(), "Media list should be empty initially");

        Book book = new Book("Test Book", "Author", "B1", 1);
        service.addMedia(book);
        result = service.getAllMedia();
        assertEquals(1, result.size(), "Media list should contain 1 item");
        assertEquals("B1", result.get(0).getId());
    }

    @Test
    void testBorrowMedia_UserBlockedOrHasFines() {
        Book book = new Book("Test Book", "Author", "B100", 1);
        service.addMedia(book);

        LibraryUser user1 = new LibraryUser("User1", "pass", "u1@test.com");
        service.addUser(user1);
        assertTrue(service.borrowMedia(user1, book));

        LibraryUser user2 = new LibraryUser("User2", "pass", "u2@test.com");
        user2.addFine(10.0);
        service.addUser(user2);
        assertFalse(service.borrowMedia(user2, book));

        LibraryUser user3 = new LibraryUser("User3", "pass", "u3@test.com");
        service.addUser(user3);
        try {
            Field blockedField = LibraryUser.class.getDeclaredField("blocked");
            blockedField.setAccessible(true);
            blockedField.setBoolean(user3, true);
        } catch (Exception ignored) {}
        assertFalse(service.borrowMedia(user3, book));
    }

}

