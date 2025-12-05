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
    void testSendReminderWithOverdue() throws Exception {
        // نستعير الكتاب أولاً عشان يدخل في borrowedMedia
        service.borrowMedia(user, book);

        // نعدل dueDate باستخدام Reflection (لأن مفيش setter)
        BorrowedMedia bm = user.getBorrowedMedia().get(0);

        java.lang.reflect.Field dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(bm, LocalDate.now().minusDays(3)); // overdue من 3 أيام

        // نضمن إن fineAdded = false عشان ما يتعطلش الحساب
        try {
            java.lang.reflect.Field fineAddedField = BorrowedMedia.class.getDeclaredField("fineAdded");
            fineAddedField.setAccessible(true);
            fineAddedField.set(bm, false);
        } catch (NoSuchFieldException e) {
            // لو مفيش الحقل، خلاص
        }

        service.sendReminder(user);

        verify(mockEmail).sendEmail(
                eq("roaa@example.com"),
                contains("overdue"), // أو eq("Important Reminder: You have overdue library items") حسب الكود
                contains("overdue")
        );
    }

    @Test
    void testSendReminderNoOverdue() {
        service.sendReminder(user);
        verify(mockEmail, never()).sendEmail(any(), any(), any());
    }

    @Test
    void testSendReminderWithEmptyEmail_DoesNotSend() {
        user = new LibraryUser("NoEmail", "pass", ""); // إيميل فاضي
        service.addUser(user);
        service.borrowMedia(user, book);

        service.sendReminder(user);

        verify(mockEmail, never()).sendEmail(any(), any(), any());
    }
}