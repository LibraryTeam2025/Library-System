package librarymanagement.application;

import librarymanagement.domain.Book;
import librarymanagement.domain.LibraryUser;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.Admin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {
    private EmailService emailService;
    private List<Book> books = new ArrayList<>();
    private List<LibraryUser> users = new ArrayList<>();

    public LibraryService(EmailService emailService) {
        this.emailService = emailService;
    }

    public boolean addBook(Book book) {
        boolean exists = books.stream()
                .anyMatch(b -> b.getIsbn().equalsIgnoreCase(book.getIsbn()));
        if (exists) {
            System.out.println("Book with ISBN " + book.getIsbn() + " already exists!");
            return false;
        }

        books.add(book);
        return true;
    }




    public void addUser(LibraryUser user) {
        users.add(user);
        System.out.println("User added: " + user.getName());
    }

    public List<Book> searchBook(String keyword) {
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase())
                    || b.getAuthor().toLowerCase().contains(keyword.toLowerCase())
                    || b.getIsbn().equalsIgnoreCase(keyword)) {
                results.add(b);
            }
        }
        return results;
    }

    public List<Book> getAllBooks() {
        return books;
    }

    // --- تعديل borrowBook ليشمل القيود ---
    public boolean borrowBook(LibraryUser user, Book book) {
        if (!books.contains(book)) {
            System.out.println("Book not found in library: " + book.getTitle());
            return false;
        }

        if (!book.isAvailable()) {
            System.out.println("Book is not available: " + book.getTitle());
            return false;
        }

        boolean hasOverdue = user.getBorrowedBooks().stream()
                .anyMatch(bb -> !bb.isReturned() && LocalDate.now().isAfter(bb.getDueDate()));

        if (hasOverdue) {
            System.out.println("Cannot borrow books: You have overdue books.");
            return false;
        }

        if (user.getFineBalance() > 0) {
            System.out.println("Cannot borrow books: You have unpaid fines.");
            return false;
        }

        BorrowedBook borrowedBook = new BorrowedBook(book);
        borrowedBook.setDueDate(LocalDate.now().plusDays(28));
        user.getBorrowedBooks().add(borrowedBook);
        book.setAvailable(false);
        System.out.println(user.getName() + " borrowed: " + book.getTitle()
                + ", due date: " + borrowedBook.getDueDate());
        return true;
    }

    public void checkOverdueBooks(LibraryUser user) {
        List<BorrowedBook> borrowed = user.getBorrowedBooks();
        for (BorrowedBook bb : borrowed) {
            if (!bb.isReturned() && LocalDate.now().isAfter(bb.getDueDate())) {
                System.out.println("Overdue book: " + bb.getBook().getTitle()
                        + " | Due date: " + bb.getDueDate());
                user.addFine(5);
            }
        }
    }

    public void payFine(LibraryUser user, double amount) {
        user.payFine(amount);
        System.out.println(user.getName() + " paid fine: $" + amount);
    }

    public void sendReminder(LibraryUser user) {
        long overdueCount = user.getBorrowedBooks().stream()
                .filter(bb -> !bb.isReturned() && LocalDate.now().isAfter(bb.getDueDate()))
                .count();

        if (overdueCount > 0) {
            String message = "You have " + overdueCount + " overdue book(s).";
            emailService.sendEmail(user.getName(), message);
        }
    }

    public boolean unregisterUser(Admin admin, LibraryUser user) {
        if (!admin.isLoggedIn()) {
            System.out.println("Only admins can unregister users.");
            return false;
        }

        boolean hasActiveLoans = user.getBorrowedBooks().stream()
                .anyMatch(bb -> !bb.isReturned());

        if (hasActiveLoans) {
            System.out.println("Cannot unregister: User has active loans.");
            return false;
        }

        if (user.getFineBalance() > 0) {
            System.out.println("Cannot unregister: User has unpaid fines.");
            return false;
        }

        users.remove(user);
        System.out.println("User unregistered successfully: " + user.getName());
        return true;
    }

    public List<LibraryUser> getUsers() {
        return users;
    }
}
