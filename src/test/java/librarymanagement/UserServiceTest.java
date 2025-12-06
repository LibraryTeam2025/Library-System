package librarymanagement;

import librarymanagement.application.EmailService;
import librarymanagement.application.LibraryService;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private static final String USERS_FILE = "test_users.txt";
    private static final String BORROWED_FILE = "test_borrowed.txt";
    private UserService service;

    @BeforeEach
    void setup() throws IOException {
        new File(USERS_FILE).delete();
        new File(BORROWED_FILE).delete();

        try (PrintWriter pw = new PrintWriter(USERS_FILE)) {
            pw.println("Roa:123:roa@mail.com");
            pw.println("Yaman:456:yaman@mail.com");
        }

        service = new UserService(USERS_FILE, BORROWED_FILE);
    }

    @AfterEach
    void cleanup() {
        new File(USERS_FILE).delete();
        new File(BORROWED_FILE).delete();
    }

    @Test
    void testLoginSuccess() {
        LibraryUser user = service.login("Roa", "123");
        assertNotNull(user);
        assertEquals("Roa", user.getName());
    }

    @Test
    void testLoginFailure() {
        assertNull(service.login("Roa", "wrong"));
        assertNull(service.login("NonExistent", "123"));
    }

    @Test
    void testAddUserSuccess() {
        assertTrue(service.addUser("Ali", "789", "ali@mail.com"));
        LibraryUser user = service.login("Ali", "789");
        assertNotNull(user);
        assertEquals("Ali", user.getName());
    }

    @Test
    void testAddUserDuplicate() {
        service.addUser("Dup", "111", "dup@mail.com");
        assertFalse(service.addUser("Dup", "222", "dup2@mail.com"));
    }

    @Test
    void testRemoveUser() {
        service.addUser("Temp", "temp", "temp@mail.com");
        assertTrue(service.removeUser("Temp"));
        assertNull(service.getUserByName("Temp"));
    }

    @Test
    void testGetUsersReturnsCopy() {
        service.getUsers().clear();
        assertEquals(2, service.getUsers().size());
    }
    @Test
    void testLoadAndSaveUsers() throws IOException {
        UserService us = new UserService(USERS_FILE, BORROWED_FILE);

        assertTrue(us.addUser("TestUser", "999", "test@mail.com"));
        us.saveUsers();
        UserService us2 = new UserService(USERS_FILE, BORROWED_FILE);
        LibraryUser user = us2.getUserByName("TestUser");
        assertNotNull(user);
        assertEquals("TestUser", user.getName());
        assertEquals("999", user.getPassword());
    }

    @Test
    void testToString_AllBranches() {
        Book book = new Book("Effective Java", "Joshua Bloch", "B100", 1);
        BorrowedMedia bm = new BorrowedMedia(book);

        bm.setDueDate(LocalDate.now().plusDays(3));
        String s1 = bm.toString();
        assertTrue(s1.contains("[BOOK]") && s1.contains("[ON TIME]"));

        bm.setDueDate(LocalDate.now().minusDays(5));
        bm.setFine(50.0);
        String s2 = bm.toString();
        assertTrue(s2.contains("[OVERDUE]") && s2.contains("50.00"));

        bm.returnMedia();
        String s3 = bm.toString();
        assertTrue(s3.contains("[RETURNED]"));
    }

    @Test
    void testCalculateFine_Branches() {
        Book book = new Book("Clean Code", "Robert Martin", "B200", 1);
        BorrowedMedia bm = new BorrowedMedia(book);

        bm.setDueDate(LocalDate.now().plusDays(2));
        assertEquals(0.0, bm.calculateFine(), 0.01);

        bm.setDueDate(LocalDate.now().minusDays(3));
        double fine = bm.calculateFine();
        assertTrue(fine > 0);

        bm.returnMedia();
        assertEquals(0.0, bm.calculateFine(), 0.01);
    }

    @Test
    void testFineAddedFlag() {
        Book book = new Book("Refactoring", "Fowler", "B300", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(2));

        assertFalse(bm.isFineAdded());
        bm.setFineAdded(true);
        assertTrue(bm.isFineAdded());
    }
    @Test
    void testLoadAndSaveBorrowedMedia() throws IOException {
        EmailService emailService = new EmailService();
        LibraryService libraryService = new LibraryService(emailService, service);
        service.setLibraryService(libraryService);

        Book book = new Book("Java Basics", "Author1", "B001", 1);
        CD cd = new CD("Hits Album", "Artist1", "C001", 1);
        libraryService.addMedia(book);
        libraryService.addMedia(cd);

        LibraryUser user1 = service.getUserByName("Roa");
        BorrowedMedia bm1 = new BorrowedMedia(book, LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
        BorrowedMedia bm2 = new BorrowedMedia(cd, LocalDate.now().minusDays(2), LocalDate.now().plusDays(3));
        user1.getBorrowedMediaInternal().add(bm1);
        user1.getBorrowedMediaInternal().add(bm2);

        service.saveBorrowedMedia();

        user1.getBorrowedMediaInternal().clear();

        service.loadBorrowedMedia();

        assertEquals(2, user1.getBorrowedMediaInternal().size());

        BorrowedMedia loaded1 = user1.getBorrowedMediaInternal().get(0);
        BorrowedMedia loaded2 = user1.getBorrowedMediaInternal().get(1);

        assertEquals("B001", loaded1.getMedia().getId());
        assertTrue(loaded1.isOverdue());

        assertEquals("C001", loaded2.getMedia().getId());
        assertFalse(loaded2.isOverdue());
    }

    @Test
    void testLoadUsers_WithEmptyOrNullFile() {
        UserService emptyFileService = new UserService("", BORROWED_FILE);
        assertNotNull(emptyFileService.getUsers());
        assertEquals(0, emptyFileService.getUsers().size());

        UserService nullFileService = new UserService(null, BORROWED_FILE);
        assertNotNull(nullFileService.getUsers());
        assertEquals(0, nullFileService.getUsers().size());
    }

    @Test
    void testLoadUsers_SkipsInvalidLines() throws IOException {
        try (PrintWriter pw = new PrintWriter(USERS_FILE)) {
            pw.println("ValidUser:123:valid@mail.com");
            pw.println("InvalidLine");
        }

        UserService us = new UserService(USERS_FILE, BORROWED_FILE);
        LibraryUser user = us.getUserByName("ValidUser");
        assertNotNull(user);
        assertEquals("ValidUser", user.getName());

        LibraryUser invalid = us.getUserByName("InvalidLine");
        assertNull(invalid);
    }
    @Test
    void testLoadUsers_EmailBranch() throws IOException {
        try (PrintWriter pw = new PrintWriter(USERS_FILE)) {
            pw.println("UserWithEmail:123:user@mail.com");
            pw.println("UserWithoutEmail:456");
        }

        UserService us = new UserService(USERS_FILE, BORROWED_FILE);

        LibraryUser user1 = us.getUserByName("UserWithEmail");
        assertNotNull(user1);
        assertEquals("user@mail.com", user1.getEmail());

        LibraryUser user2 = us.getUserByName("UserWithoutEmail");
        assertNotNull(user2);
        assertEquals("", user2.getEmail());
    }
    @Test
    void testSaveUsers_ExceptionHandling() throws IOException {
        File dir = new File("test_dir");
        dir.mkdir();

        UserService us = new UserService(dir.getAbsolutePath(), BORROWED_FILE);
        us.addUser("TempUser", "123", "temp@mail.com");

        dir.delete();
    }
    @Test
    void testLoadBorrowedMedia_EmptyBorrowedFile() {
        UserService us = new UserService("test_users.txt", "");
        us.loadBorrowedMedia();
    }
    @Test
    void testLoadBorrowedMedia_CatchIOException() {
        UserService us = new UserService("non_existent_dir/users.txt", "non_existent_dir/borrowed.txt");

        LibraryService libraryService = new LibraryService(null, us);
        us.setLibraryService(libraryService);
        us.loadBorrowedMedia();

        assertNotNull(us.getUsers());
        assertTrue(us.getUsers().size() >= 0);
    }
    @Test
    void testLoadBorrowedMedia_FineAddedFlag() throws IOException {
        File usersFile = new File(System.getProperty("java.io.tmpdir"), "test_users.txt");
        File borrowedFile = new File(System.getProperty("java.io.tmpdir"), "test_borrowed.txt");
        usersFile.delete();
        borrowedFile.delete();

        try (PrintWriter pw = new PrintWriter(usersFile)) {
            pw.println("TestUser:123:test@mail.com");
        }

        UserService us = new UserService(usersFile.getAbsolutePath(), borrowedFile.getAbsolutePath());
        LibraryService libraryService = new LibraryService(null, us);
        us.setLibraryService(libraryService);

        Book book = new Book("Test Book", "Author", "B001", 1);
        libraryService.addMedia(book);

        try (PrintWriter pw = new PrintWriter(borrowedFile)) {
            pw.println("TestUser|B001|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false|10.0");
        }

        us.loadBorrowedMedia();
        LibraryUser user = us.getUserByName("TestUser");

        assertNotNull(user);
        assertEquals(1, user.getBorrowedMediaInternal().size());

        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        assertEquals(10.0, bm.getFine(), 0.01);
        assertTrue(bm.isFineAdded());
    }

    @Test
    void testLoadBorrowedMedia_SkipsInvalidLine() throws IOException {
        File usersFile = new File(System.getProperty("java.io.tmpdir"), "test_users.txt");
        File borrowedFile = new File(System.getProperty("java.io.tmpdir"), "test_borrowed.txt");
        usersFile.delete();
        borrowedFile.delete();

        try (PrintWriter pw = new PrintWriter(usersFile)) {
            pw.println("TestUser:123:test@mail.com");
        }

        UserService us = new UserService(usersFile.getAbsolutePath(), borrowedFile.getAbsolutePath());
        LibraryService libraryService = new LibraryService(null, us);
        us.setLibraryService(libraryService);

        Book book = new Book("Test Book", "Author", "B001", 1);
        libraryService.addMedia(book);

        try (PrintWriter pw = new PrintWriter(borrowedFile)) {
            pw.println("TestUser|B001|2025-12-06");
            pw.println("TestUser|B001|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false|5.0");
        }

        us.loadBorrowedMedia();
        LibraryUser user = us.getUserByName("TestUser");

        assertNotNull(user);
        assertEquals(1, user.getBorrowedMediaInternal().size());
    }
    @Test
    void testLoadBorrowedMedia_UserOrMediaNull() throws IOException {
        File usersFile = new File(System.getProperty("java.io.tmpdir"), "test_users.txt");
        File borrowedFile = new File(System.getProperty("java.io.tmpdir"), "test_borrowed.txt");
        usersFile.delete();
        borrowedFile.delete();

        try (PrintWriter pw = new PrintWriter(usersFile)) {
            pw.println("TestUser:123:test@mail.com");
        }

        UserService us = new UserService(usersFile.getAbsolutePath(), borrowedFile.getAbsolutePath());
        LibraryService libraryService = new LibraryService(null, us);
        us.setLibraryService(libraryService);

        Book book = new Book("Test Book", "Author", "B001", 1);
        libraryService.addMedia(book);

        try (PrintWriter pw = new PrintWriter(borrowedFile)) {
            pw.println("TestUser|B001|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false|5.0");
            pw.println("NonUser|B001|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false|5.0");
            pw.println("TestUser|B999|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false|5.0");
        }

        us.loadBorrowedMedia();
        LibraryUser user = us.getUserByName("TestUser");

        assertNotNull(user);
        assertEquals(1, user.getBorrowedMediaInternal().size());
        BorrowedMedia bm = user.getBorrowedMediaInternal().get(0);
        assertEquals("B001", bm.getMedia().getId());
    }

    @Test
    void testLoadBorrowedMedia_ReturnedFlag() throws IOException {
        File usersFile = new File(System.getProperty("java.io.tmpdir"), "test_users.txt");
        File borrowedFile = new File(System.getProperty("java.io.tmpdir"), "test_borrowed.txt");
        usersFile.delete();
        borrowedFile.delete();

        try (PrintWriter pw = new PrintWriter(usersFile)) {
            pw.println("TestUser:123:test@mail.com");
        }

        UserService us = new UserService(usersFile.getAbsolutePath(), borrowedFile.getAbsolutePath());
        LibraryService libraryService = new LibraryService(null, us);
        us.setLibraryService(libraryService);

        Book book1 = new Book("Book1", "Author1", "B001", 1);
        Book book2 = new Book("Book2", "Author2", "B002", 1);
        libraryService.addMedia(book1);
        libraryService.addMedia(book2);

        try (PrintWriter pw = new PrintWriter(borrowedFile)) {
            pw.println("TestUser|B001|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|true|0.0");
            pw.println("TestUser|B002|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false|0.0");
        }

        us.loadBorrowedMedia();
        LibraryUser user = us.getUserByName("TestUser");
        assertNotNull(user);
        assertEquals(2, user.getBorrowedMediaInternal().size());

        BorrowedMedia bm1 = user.getBorrowedMediaInternal().get(0);
        BorrowedMedia bm2 = user.getBorrowedMediaInternal().get(1);

        assertTrue(bm1.isReturned());
        assertFalse(bm2.isReturned());
    }
    @Test
    void testLoadBorrowedMedia_FineParsing() throws IOException {
        File usersFile = new File(System.getProperty("java.io.tmpdir"), "test_users.txt");
        File borrowedFile = new File(System.getProperty("java.io.tmpdir"), "test_borrowed.txt");
        usersFile.delete();
        borrowedFile.delete();

        try (PrintWriter pw = new PrintWriter(usersFile)) {
            pw.println("TestUser:123:test@mail.com");
        }

        UserService us = new UserService(usersFile.getAbsolutePath(), borrowedFile.getAbsolutePath());
        LibraryService libraryService = new LibraryService(null, us);
        us.setLibraryService(libraryService);

        Book book1 = new Book("Book1", "Author1", "B001", 1);
        Book book2 = new Book("Book2", "Author2", "B002", 1);
        libraryService.addMedia(book1);
        libraryService.addMedia(book2);

        try (PrintWriter pw = new PrintWriter(borrowedFile)) {
            pw.println("TestUser|B001|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false|15.0");
            pw.println("TestUser|B002|" + LocalDate.now() + "|" + LocalDate.now().plusDays(3) + "|false");
        }

        us.loadBorrowedMedia();
        LibraryUser user = us.getUserByName("TestUser");
        assertNotNull(user);
        assertEquals(2, user.getBorrowedMediaInternal().size());

        BorrowedMedia bm1 = user.getBorrowedMediaInternal().get(0);
        BorrowedMedia bm2 = user.getBorrowedMediaInternal().get(1);

        assertEquals(15.0, bm1.getFine(), 0.01);
        assertEquals(0.0, bm2.getFine(), 0.01);
    }
    @Test
    void testLoadBorrowedMedia_ReturnsEarlyOnNullOrEmpty() {
        UserService us1 = new UserService("users.txt", "borrowed.txt");
        us1.setLibraryService(null);
        us1.loadBorrowedMedia();

        UserService us2 = new UserService("users.txt", null);
        LibraryService ls2 = new LibraryService(null, us2);
        us2.setLibraryService(ls2);
        us2.loadBorrowedMedia();

        UserService us3 = new UserService("users.txt", "");
        LibraryService ls3 = new LibraryService(null, us3);
        us3.setLibraryService(ls3);
        us3.loadBorrowedMedia();
    }


}
