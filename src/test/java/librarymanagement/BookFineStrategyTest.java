package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class BookFineStrategyTest {

    private BookFineStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BookFineStrategy();
    }

    @Test
    void testFineForZeroDaysOverdue() {
        assertEquals(0, strategy.calculateFine(0));
    }

    @Test
    void testFineForOneDayOverdue() {
        assertEquals(10, strategy.calculateFine(1));
    }

    @Test
    void testFineForMultipleDaysOverdue() {
        assertEquals(50, strategy.calculateFine(5));
    }

    @Test
    void testFineForLargeNumberOfDays() {
        assertEquals(300, strategy.calculateFine(30));
    }

    @Test
    void testFineWithNegativeDays_ReturnsNegative() {
        assertEquals(-30, strategy.calculateFine(-3),
                "Current implementation returns negative fine for negative days");
        assertEquals(-100, strategy.calculateFine(-10));
    }

    @Test
    void testStrategyImplementsFineStrategyInterface() {
        assertTrue(strategy instanceof FineStrategy);
    }

    @Test
    void testConsistentCalculation() {
        assertEquals(strategy.calculateFine(7), strategy.calculateFine(7));
        assertEquals(70, strategy.calculateFine(7));
    }

    @Test
    void testFineIncreasesLinearly() {
        assertEquals(20, strategy.calculateFine(2));
        assertEquals(30, strategy.calculateFine(3));
        assertEquals(40, strategy.calculateFine(4));
    }
}