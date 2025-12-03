package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class FineCalculatorTest {

    private static class UnknownMedia extends Media {
        public UnknownMedia() {
            super("Unknown", "Anonymous", "UNKNOWN999", new BookFineStrategy());
        }
        @Override
        public int getBorrowDays() {
            return 0;
        }
    }

    private Book book;
    private CD cd;
    private Media unknownMedia;

    @BeforeEach
    void setUp() {
        book = new Book("Clean Code", "Robert Martin", "B001");
        cd = new CD("Greatest Hits", "Queen", "CD001");
        unknownMedia = new UnknownMedia();
    }

    @Test
    void testCalculateFineForBook_OneDayOverdue() {
        assertEquals(10, FineCalculator.calculateFine(book, 1));
    }

    @Test
    void testCalculateFineForBook_MultipleDays() {
        assertEquals(50, FineCalculator.calculateFine(book, 5));
        assertEquals(200, FineCalculator.calculateFine(book, 20));
    }

    @Test
    void testCalculateFineForCD_OneDayOverdue() {
        assertEquals(20, FineCalculator.calculateFine(cd, 1));
    }

    @Test
    void testCalculateFineForCD_MultipleDays() {
        assertEquals(100, FineCalculator.calculateFine(cd, 5));
        assertEquals(400, FineCalculator.calculateFine(cd, 20));
    }

    @Test
    void testCalculateFine_UnknownMediaType_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FineCalculator.calculateFine(unknownMedia, 5)
        );
        assertEquals("Unknown media type", exception.getMessage());
    }

    @Test
    void testFineForCD_IsDoubleThatOfBook() {
        int days = 7;
        int bookFine = FineCalculator.calculateFine(book, days);
        int cdFine = FineCalculator.calculateFine(cd, days);

        assertEquals(70, bookFine);
        assertEquals(140, cdFine);
        assertEquals(bookFine * 2, cdFine, "fine of cd double from the book");
    }

    // التصليح الحاسم هنا:
    @Test
    void testNullMedia_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FineCalculator.calculateFine(null, 5),
                "null media should be treated as unknown type"
        );

        assertEquals("Unknown media type", exception.getMessage());
    }

    @Test
    void testNegativeDays_BookAndCD() {
        assertEquals(-40, FineCalculator.calculateFine(book, -4));
        assertEquals(-60, FineCalculator.calculateFine(cd, -3));
    }
}