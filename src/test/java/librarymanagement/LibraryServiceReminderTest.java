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

        // ملفات مؤقتة عشان ما يأثرش على ملفات المشروع الحقيقية
        userService = new UserService("test_users_reminder.txt", "test_borrowed_reminder.txt");
        service = new LibraryService(mockEmail, userService);
        userService.setLibraryService(service); // مهم جدًا!

        user = new LibraryUser("Roaa", "123456", "roaa@example.com");

        // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
        // الكتاب لازم 4 باراميترات (title, author, isbn, copies)
        book = new Book("Java Programming", "James Gosling", "J001", 5);
        // ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←←

        service.addMedia(book);
        // استخدم addUser من UserService وليس من LibraryService
        userService.addUser("Roaa", "123456", "roaa@example.com");
    }

    @AfterEach
    void tearDown() {
        new java.io.File("test_users_reminder.txt").delete();
        new java.io.File("test_borrowed_reminder.txt").delete();
    }

    @Test
    void testSendReminder_WhenOverdue_SendsEmail() throws Exception {
        // استعارة الكتاب
        assertTrue(service.borrowMedia(user, book));

        // جلب العنصر المستعار
        BorrowedMedia borrowed = user.getBorrowedMedia().get(0);

        // تعديل dueDate ليكون overdue (باستخدام Reflection)
        var dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(borrowed, LocalDate.now().minusDays(3)); // متأخر 3 أيام

        // التأكد إن fineAdded = false (لو موجود الحقل)
        try {
            var fineAddedField = BorrowedMedia.class.getDeclaredField("fineAdded");
            fineAddedField.setAccessible(true);
            fineAddedField.set(borrowed, false);
        } catch (NoSuchFieldException ignored) {}

        // تنفيذ التذكير
        service.sendReminder(user);

        // التحقق من إرسال الإيميل
        verify(mockEmail).sendEmail(
                eq("roaa@example.com"),
                anyString(),                    // العنوان ممكن يختلف
                contains("overdue")             // النص يحتوي على كلمة overdue
        );
    }

    @Test
    void testSendReminder_WhenNoOverdue_DoesNotSend() {
        // لا نستعير أي كتاب → مفيش overdue
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
        LibraryUser userNoEmail = new LibraryUser("Ahmed", "pass", ""); // إيميل فاضي
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

        // اجعل الكتابين overdue
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

        // يرسل إيميل واحد فقط (مهما كان عدد العناصر المتأخرة)
        verify(mockEmail, times(1)).sendEmail(anyString(), anyString(), anyString());
    }
}