package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LibraryServiceReminderTest {

    @Mock
    private EmailService mockEmail;

    private LibraryService service;
    private UserService userService;
    private LibraryUser user;
    private Book book;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        userService = new UserService("test_users_reminder.txt", "test_borrowed_reminder.txt");
        service = new LibraryService(mockEmail, userService);
        userService.setLibraryService(service);

        user = new LibraryUser("Roaa", "123456", "roaa@example.com");

        book = new Book("Java Programming", "James Gosling", "J001", 5);


        service.addMedia(book);
        userService.addUser("Roaa", "123456", "roaa@example.com");
    }

    @AfterEach
    void tearDown() {
        new java.io.File("test_users_reminder.txt").delete();
        new java.io.File("test_borrowed_reminder.txt").delete();
    }

    @Test
    void testSendReminder_WhenOverdue_SendsEmail() throws Exception {
        assertTrue(service.borrowMedia(user, book));

        BorrowedMedia borrowed = user.getBorrowedMedia().get(0);

        var dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(borrowed, LocalDate.now().minusDays(3));

        try {
            var fineAddedField = BorrowedMedia.class.getDeclaredField("fineAdded");
            fineAddedField.setAccessible(true);
            fineAddedField.set(borrowed, false);
        } catch (NoSuchFieldException ignored) {}

        service.sendReminder(user);

        verify(mockEmail).sendEmail(
                eq("roaa@example.com"),
                anyString(),
                contains("overdue")
        );
    }

    @Test
    void testSendReminder_WhenNoOverdue_DoesNotSend() {
        service.sendReminder(user);

        verify(mockEmail, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendReminder_WhenNoBorrowedItems_DoesNotSend() {
        service.sendReminder(user);
        verify(mockEmail, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendReminder_WhenEmailEmpty_DoesNotSend() {
        LibraryUser userNoEmail = new LibraryUser("Ahmed", "pass", "");
        userService.addUser("Ahmed", "pass", "");

        service.sendReminder(userNoEmail);

        verify(mockEmail, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testSendReminder_WithMultipleOverdueItems_StillSendsOneEmail() throws Exception {
        Book book2 = new Book("Clean Code", "Robert Martin", "C002", 3);
        service.addMedia(book2);

        service.borrowMedia(user, book);
        service.borrowMedia(user, book2);

        for (BorrowedMedia bm : user.getBorrowedMedia()) {
            var f = BorrowedMedia.class.getDeclaredField("dueDate");
            f.setAccessible(true);
            f.set(bm, LocalDate.now().minusDays(5));
            try {
                var fa = BorrowedMedia.class.getDeclaredField("fineAdded");
                fa.setAccessible(true);
                fa.set(bm, false);
            } catch (NoSuchFieldException ignored) {}
        }

        service.sendReminder(user);

        verify(mockEmail, times(1)).sendEmail(anyString(), anyString(), anyString());
    }
}