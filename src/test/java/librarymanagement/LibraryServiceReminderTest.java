package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

public class LibraryServiceReminderTest {

    @Mock
    private EmailService mockEmail;

    private LibraryService service;
    private LibraryUser user;
    private Book book;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        UserService userService = new UserService("test_users.txt", "test_borrowed.txt");
        service = new LibraryService(mockEmail, userService);

        user = new LibraryUser("Roaa", "password", "roaa@example.com");
        book = new Book("Java", "Author", "001");

        service.addMedia(book);
        service.addUser(user);
    }

    @Test
    void testSendReminderWithOverdue() {
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(1));
        user.getBorrowedMedia().add(bm);

        service.sendReminder(user);

        verify(mockEmail).sendEmail(
                eq("roaa@example.com"),
                eq("Overdue Reminder"),
                contains("1 overdue")
        );
    }

    @Test
    void testSendReminderNoOverdue() {
        service.sendReminder(user);
        verify(mockEmail, never()).sendEmail(any(), any(), any());
    }
}
