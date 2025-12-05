package librarymanagement.domain;

import java.util.ArrayList;
import java.util.List;

public class LibraryUser {
    private String name;
    private String password;
    private String email;
    private double fineBalance = 0.0;
    private final List<BorrowedMedia> borrowedMedia = new ArrayList<>();
    private boolean blocked;

    public LibraryUser(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email != null ? email.trim() : "";
    }

    public LibraryUser(String name, String email) {
        this.name = name;
        this.password = ""; // يمكن تعديلها لاحقاً
        this.email = email != null ? email.trim() : "";
    }

    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getFineBalance() { return fineBalance; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public List<BorrowedMedia> getBorrowedMediaInternal() { return borrowedMedia; }
    public List<BorrowedMedia> getBorrowedMedia() { return new ArrayList<>(borrowedMedia); }

    public void borrowMedia(Media media) {
        if (media.borrowCopy()) {
            borrowedMedia.add(new BorrowedMedia(media));
        }
    }

    public void addBorrowedMedia(BorrowedMedia borrowedMediaItem) {
        borrowedMedia.add(borrowedMediaItem);
    }

    public void returnMedia(Media media) {
        for (BorrowedMedia bm : borrowedMedia) {
            if (bm.getMedia().equals(media) && !bm.isReturned()) {
                bm.returnMedia();
                break;
            }
        }
        updateFineBalance();
    }

    public void setFineBalance(double amount) {
        fineBalance = Math.max(0, amount);
        updateBlockedStatus();
    }

    public void addFine(double amount) {
        if (amount > 0) {
            fineBalance += amount;
            updateBlockedStatus();
        }
    }

    public void payFine(double amount) {
        if (amount <= 0) return;
        fineBalance = Math.max(0, fineBalance - amount);
        updateBlockedStatus();
    }

    public boolean hasOverdueItems() {
        return borrowedMedia.stream().anyMatch(BorrowedMedia::isOverdue);
    }

    public double calculateNewFines() {
        double total = 0.0;
        for (BorrowedMedia bm : borrowedMedia) {
            if (!bm.isReturned() && !bm.isFineAdded() && bm.isOverdue()) {
                total += bm.calculateFine();
                bm.setFineAdded(true);
            }
        }
        return total;
    }

    public void updateFineBalance() {
        double newFines = calculateNewFines();
        if (newFines > 0) fineBalance += newFines;
        blocked = fineBalance > 0 || hasOverdueItems();
    }

    private void updateBlockedStatus() {
        blocked = fineBalance > 0 || hasOverdueItems();
    }

    @Override
    public String toString() {
        return name + " (" + email + ") (Fine: $" + String.format("%.2f", fineBalance) + ")";
    }
}
