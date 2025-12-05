package librarymanagement.domain;

import java.time.LocalDate;

public abstract class Media {

    protected String title;
    protected String author;
    protected String id;
    protected int totalCopies;
    protected int availableCopies;
    private final FineStrategy fineStrategy;

    public Media(String title, String author, String id, int copies, FineStrategy fineStrategy) {
        this.title = title;
        this.author = author;
        this.id = id;
        this.totalCopies = Math.max(copies, 1);
        this.availableCopies = this.totalCopies;
        this.fineStrategy = fineStrategy;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getId() { return id; }

    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void addCopy() {
        totalCopies++;
        availableCopies++;
    }

    public boolean borrowCopy() {
        if (availableCopies > 0) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) availableCopies++;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies >= 0 && availableCopies <= totalCopies) {
            this.availableCopies = availableCopies;
        }
    }

    public abstract int getBorrowDays();

    public double calculateFine(long overdueDays) {
        return fineStrategy.calculateFine((int) overdueDays);
    }

    public double calculateFine(LocalDate dueDate) {
        long overdueDays = LocalDate.now().toEpochDay() - dueDate.toEpochDay();
        return calculateFine(Math.max(0, overdueDays));
    }


    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
    }

    public void setAuthor(String author) {
        if (author != null && !author.trim().isEmpty()) {
            this.author = author.trim();
        }
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies >= this.availableCopies) {
            this.totalCopies = totalCopies;
        } else {
            throw new IllegalArgumentException("Total copies cannot be less than available copies!");
        }
    }
    public String getType() {
        if (this instanceof Book) return "[Book]";
        if (this instanceof CD) return "[CD]";
        return "[Unknown]";
    }

    @Override
    public String toString() {
        return getType() + " " + title + " - " + author + " (ID: " + id + ", Available: " + availableCopies + ")";
    }
}
