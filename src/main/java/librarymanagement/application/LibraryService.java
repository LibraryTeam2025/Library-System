package librarymanagement.application;

import librarymanagement.domain.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryService {

    private List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        books.add(book);
    }

    public List<Book> searchBook(String keyword) {
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || b.getAuthor().toLowerCase().contains(keyword.toLowerCase())
                        || b.getIsbn().equalsIgnoreCase(keyword))
                .collect(Collectors.toList());
    }

    public List<Book> getAllBooks() {
        return books;
    }

}



