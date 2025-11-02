package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private LibraryService service;
    private UserService userService;
    private EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new EmailService();
        userService = new UserService("test_users.txt");
        service = new LibraryService(emailService, userService);
    }

    @Test
    void testBookCreation() {
        Book book = new Book("Clean Code", "Robert Martin", "ISBN123");
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals("ISBN123", book.getId());
        assertTrue(book.isAvailable());
    }

    @Test
    void testAvailabilityToggle() {
        Book book = new Book("DDD", "Eric Evans", "ISBN456");
        book.setAvailable(false);
        assertFalse(book.isAvailable());
        book.setAvailable(true);
        assertTrue(book.isAvailable());
    }

    @Test
    void testToString() {
        Book book = new Book("Refactoring", "Martin Fowler", "ISBN789");
        assertEquals("Refactoring by Martin Fowler (ID: ISBN789)", book.toString());
    }

    @Test
    void testBorrowedMediaAvailability() {
        Book book = new Book("Java", "Yaman", "B001");
        LibraryUser user = new LibraryUser("Roa");
        BorrowedMedia bm = new BorrowedMedia(book);
        user.getBorrowedMedia().add(bm);

        assertFalse(book.isAvailable());
        bm.returnMedia();
        assertTrue(book.isAvailable());
    }

    @Test
    void testBorrowedMediaDueDate() {
        Book book = new Book("Python", "Yaman", "B002");
        BorrowedMedia bm = new BorrowedMedia(book);
        assertEquals(LocalDate.now().plusDays(28), bm.getDueDate());
    }

    @Test
    void testLibraryServiceBorrowBook() {
        userService.addUser("Roa", "123");
        LibraryUser user = userService.getUserByName("Roa");
        service.addUser(user);

        Book book = new Book("Java", "Yaman", "B001");
        service.addMedia(book);

        assertTrue(service.borrowMedia(user, book));
        assertEquals(1, user.getBorrowedMedia().size());
        assertFalse(book.isAvailable());
    }

    @Test
    void testGetBorrowDays() {
        Book book = new Book("X", "Y", "Z");
        assertEquals(28, book.getBorrowDays());
    }

    @Test
    void testGetFineAmount() {
        Book book = new Book("X", "Y", "Z");
        assertEquals(10.0, book.getFineAmount());
    }
}