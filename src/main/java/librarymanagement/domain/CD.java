package librarymanagement.domain;

public class CD extends Media {

    public CD(String title, String artist, String id) {
        super(title, artist, id);
    }

    @Override
    public int getBorrowDays() {
        return 7; // استعارة لمدة 7 أيام
    }

    @Override
    public double getFineAmount() {
        return 20.0; // غرامة CD المتأخر
    }

    @Override
    public String toString() {
        return getTitle() + " by " + getAuthor() + " (CD ID: " + getId() + ")";
    }
}