package librarymanagement.domain;

import java.time.LocalDate;

public abstract class Media {

    protected String title;
    protected String author;
    protected String id;
    protected boolean available = true;

    private final FineStrategy fineStrategy;

    public Media(String title, String author, String id, FineStrategy fineStrategy) {
        this.title = title;
        this.author = author;
        this.id = id;
        this.fineStrategy = fineStrategy;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getId() { return id; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public abstract int getBorrowDays();

    public double calculateFine(long overdueDays) {
        return fineStrategy.calculateFine((int) overdueDays);
    }

    public double calculateFine(LocalDate dueDate) {
        long overdueDays = LocalDate.now().toEpochDay() - dueDate.toEpochDay();
        return calculateFine(Math.max(0, overdueDays));
    }

    public String getType() {
        if (this instanceof Book) return "[Book]";
        if (this instanceof CD) return "[CD]";
        return "[Unknown]";
    }

    @Override
    public String toString() {
        return getType() + " " + title + " - " + author + " (ID: " + id + ")";
    }
}
