package librarymanagement.domain;

public class CD extends Media {

    public CD(String title, String artist, String id) {
        super(title, artist, id, new CDFineStrategy());
    }

    @Override
    public int getBorrowDays() {
        return 7;
    }

    @Override
    public String toString() {
        return "[CD] " + getTitle() + " by " + getAuthor() + " (ID: " + getId() + ")";
    }
}
