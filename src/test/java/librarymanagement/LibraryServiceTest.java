package librarymanagement;

import librarymanagement.application.LibraryService;
import librarymanagement.domain.Book;
import librarymanagement.domain.User;
import librarymanagement.domain.Loan;
import librarymanagement.domain.Fine;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    @Test
    void borrowBook_setsLoanAndDueDateCorrectly() {
        LibraryService service = new LibraryService();
        User user = new User("john");
        Book book = new Book("Java Basics", "Yaman", "111");

        Loan loan = service.borrowBook(user, book);
        assertFalse(book.isAvailable());
        assertEquals(user, loan.getUser());
        assertEquals(book, loan.getBook());
        assertEquals(LocalDate.now().plusDays(28), loan.getDueDate());
    }

    @Test
    void borrowBook_failsIfUserHasOverdue() throws Exception {
        LibraryService service = new LibraryService();
        User user = new User("john");
        Book book1 = new Book("Java Basics", "Yaman", "111");
        Book book2 = new Book("Advanced Java", "Yaman", "222");

        Loan loan = service.borrowBook(user, book1);

        Field dueDateField = Loan.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(loan, LocalDate.now().minusDays(1));

        assertThrows(RuntimeException.class, () -> service.borrowBook(user, book2));
    }

    @Test
    void borrowBook_failsIfUserHasUnpaidFines() {
        LibraryService service = new LibraryService();
        User user = new User("john");
        Book book = new Book("Java Basics", "Yaman", "111");

        Fine fine = new Fine(user, 50);
        assertThrows(RuntimeException.class, () -> service.borrowBook(user, book));
    }

    @Test
    void payFine_reducesAmountAndMarksPaid() {
        User user = new User("john");
        Fine fine = new Fine(user, 50);
        LibraryService service = new LibraryService();

        service.payFine(user, 50);
        assertTrue(fine.isPaid());
        assertEquals(0, fine.getAmount());
    }

    @Test
    void checkOverdueBooks_returnsCorrectLoans() throws Exception {
        LibraryService service = new LibraryService();
        User user = new User("john");
        Book book = new Book("Java Basics", "Yaman", "111");

        Loan loan = service.borrowBook(user, book);
        Field dueDateField = Loan.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(loan, LocalDate.now().minusDays(1));

        List<Loan> overdue = service.checkOverdueBooks();
        assertEquals(1, overdue.size());
        assertEquals(loan, overdue.get(0));
    }
}
