package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
public class CDFineStrategyTest {

    private CDFineStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CDFineStrategy();
    }

    @Test
    void testFineForZeroDaysOverdue() {
        int fine = strategy.calculateFine(0);
        assertEquals(0, fine, "No fine should be charged when no days are overdue");
    }

    @Test
    void testFineForOneDayOverdue() {
        int fine = strategy.calculateFine(1);
        assertEquals(20, fine, "CD fine should be 20 per day overdue");
    }

    @Test
    void testFineForMultipleDaysOverdue() {
        assertEquals(100, strategy.calculateFine(5), "5 days overdue → 5 × 20 = 100");
        assertEquals(200, strategy.calculateFine(10), "10 days overdue → 200");
    }

    @Test
    void testFineForLargeOverduePeriod() {
        assertEquals(1000, strategy.calculateFine(50), "50 days overdue → 1000 fine");
    }

    @Test
    void testNegativeOverdueDays_ReturnsZeroOrNegative() {
        assertEquals(-60, strategy.calculateFine(-3),
                "Current implementation allows negative fine for negative days");

    }

    @Test
    void testStrategyImplementsInterface() {
        assertTrue(strategy instanceof FineStrategy,
                "CDFineStrategy must implement FineStrategy interface");
    }

    @Test
    void testCalculationIsConsistent() {
        int first = strategy.calculateFine(7);
        int second = strategy.calculateFine(7);
        assertEquals(first, second, "Same input must always return same output");
        assertEquals(140, first, "7 days overdue → 7 × 20 = 140");
    }

    @Test
    void testFineIncreasesLinearlyBy20() {
        assertEquals(20, strategy.calculateFine(1));
        assertEquals(40, strategy.calculateFine(2));
        assertEquals(60, strategy.calculateFine(3));
        assertEquals(80, strategy.calculateFine(4));
        assertEquals(100, strategy.calculateFine(5));
    }

    @Test
    void testFineIsDoubleThatOfBookStrategy() {
        BookFineStrategy bookStrategy = new BookFineStrategy();
        int cdFine = strategy.calculateFine(4);
        int bookFine = bookStrategy.calculateFine(4);

        assertEquals(80, cdFine, "CD fine for 4 days");
        assertEquals(40, bookFine, "Book fine for 4 days");
        assertEquals(bookFine * 2, cdFine, "CD fine should be exactly double the book fine");
    }
}
