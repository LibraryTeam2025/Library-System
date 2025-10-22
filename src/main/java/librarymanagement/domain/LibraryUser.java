package librarymanagement.domain;

import java.util.ArrayList;
import java.util.List;

public class LibraryUser {
    private String name;
    private List<BorrowedBook> borrowedBooks = new ArrayList<>();
    private double fineBalance = 0;



    public String getName() {
        return name;
    }

    public LibraryUser(String name) {
        this.name = name;
    }

    public void borrowBook(Book book) {
        if (!canBorrow()) {
            System.out.println("Cannot borrow: pending fines = " + fineBalance);
            return;
        }
        if (book.isAvailable()) {
            BorrowedBook borrowed = new BorrowedBook(book);
            borrowedBooks.add(borrowed);
            System.out.println(name + " borrowed: " + borrowed);
        } else {
            System.out.println("Book is not available: " + book.getTitle());
        }
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

    public boolean canBorrow() {
        return fineBalance <= 0;
    }

    public List<BorrowedBook> getBorrowedBooks() {
        return borrowedBooks;
    }

    public double getFineBalance() {
        return fineBalance;
    }
}