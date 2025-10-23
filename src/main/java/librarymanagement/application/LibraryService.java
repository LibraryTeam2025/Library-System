package librarymanagement.application;

import librarymanagement.domain.Book;
import librarymanagement.domain.LibraryUser;
import librarymanagement.domain.BorrowedBook;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {
    private EmailService emailService;
    private List<Book> books = new ArrayList<>();

    // Constructor
    public LibraryService(EmailService emailService) {
        this.emailService = emailService;
    }

    // إضافة كتاب جديد للمكتبة
    public void addBook(Book book) {
        books.add(book);
        System.out.println("Book added: " + book);
    }

    // البحث عن كتاب حسب العنوان، المؤلف، أو ISBN
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

    // عرض كل الكتب
    public List<Book> getAllBooks() {
        return books;
    }

    // استعارة كتاب من قبل مستخدم
    public void borrowBook(LibraryUser user, Book book) {
        if (!books.contains(book)) {
            System.out.println("Book not found in library: " + book.getTitle());
            return;
        }

        if (!book.isAvailable()) {
            System.out.println("Book is not available: " + book.getTitle());
            return;
        }

        BorrowedBook borrowedBook = new BorrowedBook(book);
        user.getBorrowedBooks().add(borrowedBook);
        book.setAvailable(false);
        System.out.println(user.getClass().getSimpleName() + " borrowed: " + book.getTitle()
                + ", due date: " + borrowedBook.getDueDate());
    }

    // التحقق من الكتب المتأخرة وإضافة غرامة
    public void checkOverdueBooks(LibraryUser user) {
        List<BorrowedBook> borrowed = user.getBorrowedBooks();
        for (BorrowedBook bb : borrowed) {
            if (!bb.isReturned() && LocalDate.now().isAfter(bb.getDueDate())) {
                System.out.println("Overdue book: " + bb.getBook().getTitle()
                        + " | Due date: " + bb.getDueDate());
                user.addFine(5); // مثال: غرامة 5 لكل كتاب متأخر
            }
        }
    }

    // دفع الغرامة
    public void payFine(LibraryUser user, double amount) {
        user.payFine(amount);
    }

    // إرسال تذكير بالكتب المتأخرة
    public void sendReminder(LibraryUser user) {
        long overdueCount = user.getBorrowedBooks().stream()
                .filter(bb -> !bb.isReturned() && LocalDate.now().isAfter(bb.getDueDate()))
                .count();

        if (overdueCount > 0) {
            String message = "You have " + overdueCount + " overdue book(s).";
            emailService.sendEmail(user.getName(), message);
        }
    }
}
