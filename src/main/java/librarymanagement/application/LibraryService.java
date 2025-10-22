package librarymanagement.application;

import librarymanagement.domain.Book;
import librarymanagement.domain.User;
import librarymanagement.domain.Loan;
import librarymanagement.domain.Fine;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;

public class LibraryService {

    private List<Book> books = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    public void addBook(Book book) { books.add(book); }

    public List<Book> searchBook(String keyword) {
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || b.getAuthor().toLowerCase().contains(keyword.toLowerCase())
                        || b.getIsbn().equalsIgnoreCase(keyword))
                .collect(Collectors.toList()); // <-- change here
    }

    public List<Book> getAllBooks() { return books; }

    // Borrow book
    public Loan borrowBook(User user, Book book) {
        if(!book.isAvailable()) throw new RuntimeException("Book not available");
        if(user.hasOverdue()) throw new RuntimeException("User has overdue books");
        if(user.hasUnpaidFines()) throw new RuntimeException("User has unpaid fines");

        Loan loan = new Loan(user, book);
        book.setAvailable(false);
        loans.add(loan);
        return loan;
    }

    // Pay fine
    public void payFine(User user, int amount) {
        user.getFines().forEach(f -> {
            if(!f.isPaid()) f.pay(amount);
        });
    }

    // Check overdue books
    public List<Loan> checkOverdueBooks() {
        List<Loan> overdue = new ArrayList<>();
        for(Loan l : loans) if(l.isOverdue()) overdue.add(l);
        return overdue;
    }
}
