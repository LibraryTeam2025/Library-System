package librarymanagement.domain;


import java.time.LocalDate;
public class Loan {

    private Book book;
    private User user;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

    public Loan(User user, Book book) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(28);
        this.returned = false;
        user.getLoans().add(this);
    }

    public Book getBook() { return book; }
    public User getUser() { return user; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }

    public void setReturned(boolean returned) { this.returned = returned; }
    public boolean isOverdue() { return !returned && LocalDate.now().isAfter(dueDate); }
}
