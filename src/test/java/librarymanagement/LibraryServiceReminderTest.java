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

        EmailService mockEmail = mock(EmailService.class);

        LibraryService service = new LibraryService(mockEmail);

        LibraryUser user = new LibraryUser("Roaa");
        Book book = new Book("Java", "Author", "001");
        BorrowedBook bb = new BorrowedBook(book);
        bb.setDueDate(LocalDate.now().minusDays(1));
        user.getBorrowedBooks().add(bb);

        service.sendReminder(user);

        verify(mockEmail).sendEmail(user.getName(), "You have 1 overdue book(s).");
    }
}
