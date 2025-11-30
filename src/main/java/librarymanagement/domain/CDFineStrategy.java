package librarymanagement.domain;

public class CDFineStrategy implements FineStrategy {
    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 20; // 20 شيكل باليوم
    }
}
