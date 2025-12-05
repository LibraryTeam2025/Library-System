package librarymanagement.domain;

public class Book extends Media {

    public Book(String title, String author, String isbn, int copies) {
        super(title, author, isbn, copies, new BookFineStrategy());
    }

    @Override
    public int getBorrowDays() {
        return 28;
    }

    @Override
    public String toString() {
        return "[Book] " + getTitle() + " by " + getAuthor() + " (ISBN: " + getId() + ", Available: " + getAvailableCopies() + ")";
    }
}
