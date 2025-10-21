package librarymanagement.presentation;

import librarymanagement.domain.Admin;
import librarymanagement.domain.Book;
import librarymanagement.domain.LibraryUser;
import librarymanagement.application.LibraryService;

import java.util.List;
import java.util.Scanner;

public class LibraryApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Admin admin = new Admin("soft", "123");
        LibraryService service = new LibraryService();
        LibraryUser user = new LibraryUser("Student");

        System.out.println("Welcome to Library Management System ♥ ");

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Add Book");
            System.out.println("3. Search Book");
            System.out.println("4. Borrow Book");
            System.out.println("5. Check Overdue Books");
            System.out.println("6. Pay Fine");
            System.out.println("7. Logout");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Username: ");
                    String u = sc.nextLine();
                    System.out.print("Password: ");
                    String p = sc.nextLine();
                    if (admin.login(u, p)) {
                        System.out.println("Login successful.");
                    } else {
                        System.out.println("Invalid credentials.");
                    }
                    break;

                case 2:
                    if (!admin.isLoggedIn()) {
                        System.out.println("Please login first!");
                        break;
                    }
                    System.out.print("Title: ");
                    String title = sc.nextLine();
                    System.out.print("Author: ");
                    String author = sc.nextLine();
                    System.out.print("ISBN: ");
                    String isbn = sc.nextLine();
                    service.addBook(new Book(title, author, isbn));
                    break;

                case 3:
                    System.out.print("Search keyword: ");
                    String key = sc.nextLine();
                    List<Book> results = service.searchBook(key);
                    if (results.isEmpty()) {
                        System.out.println("No results found.");
                    } else {
                        System.out.println("Search results:");
                        results.forEach(System.out::println);
                    }
                    break;

                case 4:
                    System.out.println("Available books:");
                    List<Book> allBooks = service.getAllBooks();
                    for (int i = 0; i < allBooks.size(); i++) {
                        Book b = allBooks.get(i);
                        if (b.isAvailable()) {
                            System.out.println((i + 1) + ". " + b);
                        }
                    }
                    System.out.print("Enter book number to borrow: ");
                    int bookIndex = sc.nextInt() - 1;
                    sc.nextLine();
                    if (bookIndex >= 0 && bookIndex < allBooks.size()) {
                        Book bookToBorrow = allBooks.get(bookIndex);
                        service.borrowBook(user, bookToBorrow);
                    } else {
                        System.out.println("Invalid book number.");
                    }
                    break;

                case 5:
                    service.checkOverdueBooks(user);
                    break;
                case 6:
                    System.out.println("Current fine balance: " + user.getFineBalance());
                    System.out.print("Enter amount to pay: ");
                    double amt = sc.nextDouble();
                    sc.nextLine();
                    service.payFine(user, amt);
                    break;
                case 7:
                    admin.logout();
                    System.out.println("Logged out.");
                    break;

                case 8:
                    System.out.println("Bye!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
