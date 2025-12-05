package librarymanagement.domain;

public class CD extends Media {

    public CD(String title, String artist, String id, int copies) {
        super(title, artist, id, copies, new CDFineStrategy());
    }

    @Override
    public int getBorrowDays() {
        return 7;
    }

    @Override
    public String toString() {
        return "[CD] " + getTitle() + " by " + getAuthor() + " (ID: " + getId() + ", Available: " + getAvailableCopies() + ")";
    }
}
