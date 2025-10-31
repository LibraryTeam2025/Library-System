package librarymanagement.domain;

import java.util.ArrayList;
import java.util.List;

public class LibraryUser {
    private String name;
    private String password;
    private double fineBalance = 0;
    private List<BorrowedMedia> borrowedMedia = new ArrayList<>();

    public LibraryUser(String name, String password) {
        this.name = name;
        this.password = (password != null) ? password : "";
    }

    public LibraryUser(String name) {
        this(name, "");
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public double getFineBalance() {
        return fineBalance;
    }

    public List<BorrowedMedia> getBorrowedMedia() {
        return borrowedMedia;
    }

    public void addFine(double amount) {
        if (amount > 0) {
            fineBalance += amount;
        }
    }

    public void payFine(double amount) {
        if (amount <= 0) {
            System.out.println("Enter a positive amount.");
            return;
        }
        fineBalance -= amount;
        if (fineBalance < 0) fineBalance = 0;
        System.out.println("Fine paid: $" + amount + " | Remaining fine: $" + fineBalance);
    }

    public double calculateTotalFine() {
        return borrowedMedia.stream()
                .mapToDouble(BorrowedMedia::calculateFine)
                .sum();
    }

    @Override
    public String toString() {
        return name;
    }
}