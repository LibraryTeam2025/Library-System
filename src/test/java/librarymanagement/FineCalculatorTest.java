package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class FineCalculatorTest {

    private Book book;
    private CD cd;
    private Media unknownMedia;

    // كائن من FineCalculator لأن الميثود مش static
    private final FineCalculator calculator = new FineCalculator();

    @BeforeEach
    void setUp() {
        book = new Book("Clean Code", "Robert Martin", "B001", 5);
        cd = new CD("Greatest Hits", "Queen", "CD001", 3);

        // وسيلة غير معروفة لاختبار الحالة الاستثنائية
        unknownMedia = new Media("Unknown", "Anon", "U999", 1, new BookFineStrategy()) {
            @Override
            public int getBorrowDays() {
                return 14;
            }
        };
    }

    @Test
    void testCalculateFineForBook_OneDayOverdue() {
        assertEquals(10.0, calculator.calculateFine(book, 1), 0.01);
    }

    @Test
    void testCalculateFineForBook_MultipleDays() {
        assertEquals(50.0, calculator.calculateFine(book, 5), 0.01);
        assertEquals(200.0, calculator.calculateFine(book, 20), 0.01);
    }

    @Test
    void testCalculateFineForCD_OneDayOverdue() {
        assertEquals(20.0, calculator.calculateFine(cd, 1), 0.01);
    }

    @Test
    void testCalculateFineForCD_MultipleDays() {
        assertEquals(100.0, calculator.calculateFine(cd, 5), 0.01);
        assertEquals(400.0, calculator.calculateFine(cd, 20), 0.01);
    }

    @Test
    void testFineForCD_IsDoubleThatOfBook() {
        int days = 7;
        double bookFine = calculator.calculateFine(book, days);
        double cdFine = calculator.calculateFine(cd, days);

        assertEquals(70.0, bookFine, 0.01);
        assertEquals(140.0, cdFine, 0.01);
        assertEquals(bookFine * 2, cdFine, 0.0001);
    }

    @Test
    void testNullMedia_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateFine(null, 5)
        );
        // الكود فعليًا يرجع "Unknown media type"
        assertEquals("Unknown media type", ex.getMessage());
    }

    @Test
    void testUnknownMediaType_ThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateFine(unknownMedia, 5)
        );
        assertEquals("Unknown media type", ex.getMessage());
    }

    @Test
    void testNegativeDays_ReturnsNegativeFine() {
        assertEquals(-40.0, calculator.calculateFine(book, -4), 0.01);
        assertEquals(-60.0, calculator.calculateFine(cd, -3), 0.01);
    }

    @Test
    void testZeroDaysOverdue_ReturnsZero() {
        assertEquals(0.0, calculator.calculateFine(book, 0), 0.01);
        assertEquals(0.0, calculator.calculateFine(cd, 0), 0.01);
    }

    @Test
    void testLargeNumberOfDays_DoesNotOverflow() {
        double fine = calculator.calculateFine(book, 1000);
        assertEquals(10000.0, fine, 0.01); // 1000 يوم × 10
    }
}
