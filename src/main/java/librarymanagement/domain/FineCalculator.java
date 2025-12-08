package librarymanagement.domain;

public class FineCalculator {
    private FineCalculator() {

    }
    public static int calculateFine(Media media, int overdueDays) {

        FineStrategy strategy;

        if (media instanceof Book) {
            strategy = new BookFineStrategy();
        } else if (media instanceof CD) {
            strategy = new CDFineStrategy();
        }
        else {
            throw new IllegalArgumentException("Unknown media type");
        }

        return strategy.calculateFine(overdueDays);
    }
}

