package librarymanagement.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public class BorrowedMedia {
    private Media media;                // ← الآن نحتفظ بالـ Media نفسه
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned = false;
    private boolean fineAdded = false; // ✅ لمنع تكرار الغرامة
    public BorrowedMedia(Media media) {
        this.media = media;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(media.getBorrowDays());
        this.returned = false;
        media.setAvailable(false);      // الوسيط يصبح غير متاح عند استعارة
    }

    public boolean isFineAdded() {
        return fineAdded;
    }

    public void setFineAdded(boolean fineAdded) {
        this.fineAdded = fineAdded;
    }
    // 🔹 حساب الغرامة
    public double calculateFine() {
        if (returned) return 0;
        long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return daysLate > 0 ? daysLate * media.getFineAmount() : 0;
    }

    public void returnMedia() {
        returned = true;
        media.setAvailable(true);       // الوسيط يصبح متاح عند الإرجاع
    }

    // getters
    public Media getMedia() { return media; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

}