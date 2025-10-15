package librarymanagement.presentation;

import librarymanagement.domain.Admin;
import librarymanagement.domain.Book;
import librarymanagement.application.LibraryService;

import java.util.List;
import java.util.Scanner;

public class LibraryApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Admin admin = new Admin("admin", "1234");
        LibraryService service = new LibraryService();

        System.out.println("Welcome to Library Management System!");

        while (true) {
            System.out.println("\n1. Login\n2. Add Book\n3. Search Book\n4. Logout\n5. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Username: ");
                    String u = sc.nextLine();
                    System.out.print("Password: ");
                    String p = sc.nextLine();
                    if (admin.login(u, p)) {
                        System.out.println("✅ Login successful.");
                    } else {
                        System.out.println("❌ Invalid credentials.");
                    }
                    break;

                case 2:
                    if (!admin.isLoggedIn()) {
                        System.out.println("⚠️ Please login first!");
                        break;
                    }
                    System.out.print("Title: ");
                    String title = sc.nextLine();
                    System.out.print("Author: ");
                    String author = sc.nextLine();
                    System.out.print("ISBN: ");
                    String isbn = sc.nextLine();
                    service.addBook(new Book(title, author, isbn));
                    System.out.println("✅ Book added.");
                    break;

                case 3:
                    System.out.print("Search keyword: ");
                    String key = sc.nextLine();
                    List<Book> results = service.searchBook(key);
                    if (results.isEmpty()) {
                        System.out.println("No results found.");
                    } else {
                        results.forEach(System.out::println);
                    }
                    break;

                case 4:
                    admin.logout();
                    System.out.println("👋 Logged out.");
                    break;

                case 5:
                    System.out.println("Bye!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }


}
