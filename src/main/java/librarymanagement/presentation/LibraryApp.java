package librarymanagement.presentation;

import librarymanagement.domain.Admin;
import librarymanagement.domain.Book;
import librarymanagement.domain.BorrowedBook;
import librarymanagement.domain.LibraryUser;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LibraryApp {

    private static List<LibraryUser> users = new ArrayList<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Admin admin = new Admin("soft", "123");

        // إنشاء المستخدمين
        LibraryUser student = new LibraryUser("Student");
        LibraryUser roaa = new LibraryUser("Roaa");
        users.add(student);
        users.add(roaa);

        // إنشاء الخدمة والبريد
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        // إضافة كتاب متأخر للمستخدم Roaa
        Book lateBook = new Book("LateBook", "Author", "123");
        service.addBook(lateBook);
        BorrowedBook bb = new BorrowedBook(lateBook);
        bb.setDueDate(LocalDate.now().minusDays(2)); // متأخر يومين
        roaa.getBorrowedBooks().add(bb);
        lateBook.setAvailable(false);

        System.out.println("Welcome to Library Management System ♥ ");

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Add Book");
            System.out.println("3. Search Book");
            System.out.println("4. Borrow Book");
            System.out.println("5. Check Overdue Books");
            System.out.println("6. Pay Fine");
            System.out.println("7. Send reminder for overdue books");
            System.out.println("8. Logout");
            System.out.println("9. Exit");
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

                        System.out.println("Select user to borrow:");
                        for (int i = 0; i < users.size(); i++) {
                            System.out.println((i + 1) + ". " + users.get(i).getName());
                        }
                        int userIndex = sc.nextInt() - 1;
                        sc.nextLine();

                        if (userIndex >= 0 && userIndex < users.size()) {
                            LibraryUser selectedUser = users.get(userIndex);
                            service.borrowBook(selectedUser, bookToBorrow);
                        } else {
                            System.out.println("Invalid user selection.");
                        }
                    } else {
                        System.out.println("Invalid book number.");
                    }
                    break;

                case 5:
                    System.out.println("Select user to check overdue books:");
                    for (int i = 0; i < users.size(); i++) {
                        System.out.println((i + 1) + ". " + users.get(i).getName());
                    }
                    int uIndex = sc.nextInt() - 1;
                    sc.nextLine();
                    if (uIndex >= 0 && uIndex < users.size()) {
                        service.checkOverdueBooks(users.get(uIndex));
                    }
                    break;

                case 6:
                    System.out.println("Select user to pay fine:");
                    for (int i = 0; i < users.size(); i++) {
                        System.out.println((i + 1) + ". " + users.get(i).getName() + " | Balance: " + users.get(i).getFineBalance());
                    }
                    int uPay = sc.nextInt() - 1;
                    sc.nextLine();
                    if (uPay >= 0 && uPay < users.size()) {
                        LibraryUser payUser = users.get(uPay);
                        System.out.print("Enter amount to pay: ");
                        double amt = sc.nextDouble();
                        sc.nextLine();
                        service.payFine(payUser, amt);
                        System.out.println("New balance: " + payUser.getFineBalance());
                    }
                    break;

                case 7:
                    System.out.print("Enter user name to send reminder: ");
                    String userName = sc.nextLine();
                    LibraryUser targetUser = findUserByName(userName);
                    if (targetUser != null) {
                        service.sendReminder(targetUser);
                        System.out.println("Reminder sent!");
                    } else {
                        System.out.println("User not found.");
                    }
                    break;

                case 8:
                    admin.logout();
                    System.out.println("Logged out.");
                    break;

                case 9:
                    System.out.println("Bye!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static LibraryUser findUserByName(String name) {
        for (LibraryUser u : users) {
            if (u.getName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return null;
    }
}
