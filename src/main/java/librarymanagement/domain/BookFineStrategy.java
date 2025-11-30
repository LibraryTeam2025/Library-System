package librarymanagement.domain;

public class BookFineStrategy implements FineStrategy {
    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 10; // 10 شيكل باليوم
    }
}

