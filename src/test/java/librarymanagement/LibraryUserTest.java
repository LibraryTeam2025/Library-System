package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryUserTest {

    @Test
    void testAddFine() {
        LibraryUser user = new LibraryUser("Roa");
        user.addFine(10);
        assertEquals(10.0, user.getFineBalance());
        user.addFine(5);
        assertEquals(15.0, user.getFineBalance());
    }

    @Test
    void testPayFine() {
        LibraryUser user = new LibraryUser("Roa");
        user.addFine(20);
        user.payFine(15);
        assertEquals(5.0, user.getFineBalance());
        user.payFine(10);
        assertEquals(0.0, user.getFineBalance());
    }

    @Test
    void testPayFineNegative() {
        LibraryUser user = new LibraryUser("Roa");
        user.addFine(10);
        user.payFine(-5); // لا يجب أن يؤثر
        assertEquals(10.0, user.getFineBalance());
    }

    @Test
    void testCalculateTotalFine() {
        LibraryUser user = new LibraryUser("Roa");
        Book book = new Book("Java", "X", "1");
        BorrowedMedia bm = new BorrowedMedia(book);
        bm.setDueDate(LocalDate.now().minusDays(2));
        user.getBorrowedMedia().add(bm);
        assertEquals(20.0, user.calculateTotalFine());
    }
}