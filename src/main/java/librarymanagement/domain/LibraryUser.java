package librarymanagement.domain;

import java.util.ArrayList;
import java.util.List;

public class LibraryUser {
    private String name;
    private double fineBalance = 0;

    private List<BorrowedMedia> borrowedMedia = new ArrayList<>();

    public List<BorrowedMedia> getBorrowedMedia() {
        return borrowedMedia;
    }

    public double calculateTotalFine() {
        return borrowedMedia.stream()
                .mapToDouble(BorrowedMedia::calculateFine)
                .sum();
    }


    public String getName() {
        return name;
    }

    public LibraryUser(String name) {
        this.name = name;
    }

    public void addFine(double amount) {
        fineBalance += amount;
    }

    public void payFine(double amount) {
        if (amount <= 0) {
            System.out.println("Enter a positive amount.");
            return;
        }
        fineBalance -= amount;
        if (fineBalance < 0) fineBalance = 0;
        System.out.println("Fine paid: " + amount + " | Remaining fine: " + fineBalance);
    }

    public double getFineBalance() {
        return fineBalance;
    }
}