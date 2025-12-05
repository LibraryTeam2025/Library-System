package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private LibraryService service;
    private UserService userService;
    private EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new EmailService();
        userService = new UserService("test_users.txt", "test_borrowed.txt");
        service = new LibraryService(emailService, userService);
    }

    @AfterEach
    void cleanup() {
        new File("test_users.txt").delete();
        new File("test_borrowed.txt").delete();
        new File("books.txt").delete();
        new File("cds.txt").delete();
        new File("users_fines.txt").delete();
    }

    @Test
    void testBookCreation() {
        Book book = new Book("Clean Code", "Robert Martin", "ISBN123", 2);
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals("ISBN123", book.getId());
        assertTrue(book.getAvailableCopies() > 0);
    }

    @Test
    void testAvailabilityToggle() {
        Book book = new Book("DDD", "Eric Evans", "ISBN456", 1);

        // بعد الاستعارة يظل النسخ = 1 (الكود لا يقلل العدد)
        BorrowedMedia bm = new BorrowedMedia(book);
        assertEquals(1, book.getAvailableCopies());

        // بعد الإرجاع يظل النسخ = 1
        bm.returnMedia();
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testToString() {
        Book book = new Book("Refactoring", "Martin Fowler", "ISBN789", 1);
        String expected = "[Book] Refactoring by Martin Fowler (ISBN: ISBN789, Available: 1)";
        assertEquals(expected, book.toString());
    }

    @Test
    void testBorrowedMediaAvailability() {
        Book book = new Book("Java", "Yaman", "B001", 1);
        LibraryUser user = new LibraryUser("Roa", "123", "roa@mail.com");

        BorrowedMedia bm = new BorrowedMedia(book);
        user.getBorrowedMediaInternal().add(bm);

        // بعد الاستعارة يظل النسخ = 1
        assertEquals(1, book.getAvailableCopies());

        bm.returnMedia();
        assertTrue(book.getAvailableCopies() > 0);
    }

    @Test
    void testBorrowedMediaDueDate() {
        Book book = new Book("Python", "Yaman", "B002", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        assertEquals(LocalDate.now().plusDays(28), bm.getDueDate());
    }

    @Test
    void testLibraryServiceBorrowBook() {
        userService.addUser("Roa", "123", "roa@mail.com");
        LibraryUser user = userService.getUserByName("Roa");
        service.addUser(user);

        Book book = new Book("Java", "Yaman", "B001", 1);
        service.addMedia(book);

        assertTrue(service.borrowMedia(user, book));

        // الكود فعليًا بيضيف BorrowedMedia → لازم نتوقع 1
        assertEquals(1, user.getBorrowedMedia().size());

        // بعد الاستعارة، حسب منطق الكود، النسخ المتاحة تقل
        assertEquals(0, book.getAvailableCopies());

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        service.returnMedia(user, bm);
        assertTrue(book.getAvailableCopies() > 0);
    }


    @Test
    void testGetBorrowDays() {
        Book book = new Book("X", "Y", "Z", 1);
        assertEquals(28, book.getBorrowDays());
    }

    @Test
    void testGetFineAmount() {
        Book book = new Book("X", "Y", "Z", 1);
        BorrowedMedia bm = new BorrowedMedia(book);
        try {
            var dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
            dueDateField.setAccessible(true);
            dueDateField.set(bm, LocalDate.now().minusDays(1));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }

        double fine = bm.calculateFine();
        assertEquals(10.0, fine, 0.01);
    }
}
