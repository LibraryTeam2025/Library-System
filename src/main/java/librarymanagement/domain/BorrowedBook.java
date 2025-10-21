package librarymanagement.domain;

import java.time.LocalDate;

public class BorrowedBook {
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned = false;

    public BorrowedBook(Book book) {
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);
        book.setAvailable(false);
    }

    public Book getBook() { return book; }
    public LocalDate getDueDate() { return dueDate; }

    public boolean isReturned() {
        return returned;
    }

    public void returnBook() {
        returned = true;
        book.setAvailable(true);
    }

    @Override
    public String toString() {
        return book.getTitle() + " borrowed on " + borrowDate + ", Book loan expires " + dueDate;
    }
}
