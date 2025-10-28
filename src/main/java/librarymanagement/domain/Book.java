package librarymanagement.domain;

public class Book extends Media {
    public Book(String title, String author, String isbn) {
        super(title, author, isbn);
    }

    @Override
    public int getBorrowDays() {
        return 28; // نفس المدة الأصلية
    }

    @Override
    public double getFineAmount() {
        return 10.0; // غرامة الكتب
    }

    @Override
    public String toString() {
        return getTitle() + " by " + getAuthor() + " (ID: " + getId() + ")";
    }
}