package librarymanagement.domain;



public abstract class Media {

    protected String title;
    protected String author;
    protected String id;  // ممكن ISBN أو CD-ID
    protected boolean available = true;

    public Media(String title, String author, String id) {
        this.title = title;
        this.author = author;
        this.id = id;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getId() { return id; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public abstract int getBorrowDays();   // كم يوم يُستعار
    public abstract double getFineAmount(); // الغرامة في حال التأخير
}