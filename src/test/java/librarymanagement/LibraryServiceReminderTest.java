package librarymanagement;

import librarymanagement.application.EmailService;
import librarymanagement.application.LibraryService;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.Book;
import librarymanagement.domain.LibraryUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

public class LibraryServiceReminderTest {
    @Test
    void testSendReminderWithMock() {
        // 1. أنشئ mock للـ EmailService
        EmailService mockEmail = mock(EmailService.class);

        // 2. أنشئ LibraryService مع الـ mock
        LibraryService service = new LibraryService(mockEmail);

        // 3. أنشئ مستخدم وكتاب متأخر
        LibraryUser user = new LibraryUser("Roaa");
        Book book = new Book("Java", "Author", "001");
        BorrowedBook bb = new BorrowedBook(book);
        bb.setDueDate(LocalDate.now().minusDays(1)); // الكتاب متأخر
        user.getBorrowedBooks().add(bb);

        // 4. نادى على sendReminder
        service.sendReminder(user);

        // 5. تحقق إن mock استدعي sendEmail باليوزر والرسالة الصحيحة
        verify(mockEmail).sendEmail("Roaa", "You have 1 overdue book(s).");
    }

}
