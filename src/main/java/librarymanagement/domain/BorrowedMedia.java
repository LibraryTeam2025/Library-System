package librarymanagement.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BorrowedMedia {

    private final Media media;
    private final LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned = false;
    private double fine = 0.0;
    private boolean fineAdded = false;

    public BorrowedMedia(Media media) {
        this.media = media;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(media.getBorrowDays());
        media.setAvailable(false);
    }

    public BorrowedMedia(Media media, LocalDate dueDate) {
        this.media = media;
        this.borrowDate = LocalDate.now();
        this.dueDate = dueDate;
        media.setAvailable(false);
    }

    public BorrowedMedia(Media media, LocalDate borrowDate, LocalDate dueDate) {
        this.media = media;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        media.setAvailable(false);
    }

    public boolean isOverdue() {
        return !returned && LocalDate.now().isAfter(dueDate);
    }

    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public double calculateFine() {
        if (returned) return 0.0;

        long daysLate = getOverdueDays();
        if (daysLate <= 0) return 0.0;

        int ratePerDay = media instanceof CD ? 20 : 10; // CD=20 ، Book=10
        fine = daysLate * ratePerDay;
        return fine;
    }


    public boolean isFineAdded() { return fineAdded; }
    public void setFineAdded(boolean fineAdded) { this.fineAdded = fineAdded; }


    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }


    public void returnMedia() {
        returned = true;
        media.setAvailable(true);
    }


    public Media getMedia() { return media; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    @Override
    public String toString() {
        String type = media instanceof Book ? "[BOOK]" : "[CD]";
        String status;
        if (returned) status = "[RETURNED]";
        else if (isOverdue()) status = "[OVERDUE] " + getOverdueDays() + " day(s) late | Fine: $" + String.format("%.2f", fine);
        else status = "[ON TIME]";
        return type + " " + media.getTitle() + " | Due: " + dueDate + " " + status;
    }
}
