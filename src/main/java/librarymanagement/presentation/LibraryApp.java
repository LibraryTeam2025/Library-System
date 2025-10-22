package librarymanagement.presentation;

import librarymanagement.domain.Admin;
import librarymanagement.domain.Book;
import librarymanagement.domain.LibraryUser;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;

import java.util.List;
import java.util.Scanner;

public class LibraryApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Admin admin = new Admin("soft", "123");
        EmailService emailService = new EmailService();
        LibraryService service = new LibraryService(emailService);

        System.out.println("Welcome to Library Management System ♥");

        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Add Book");
            System.out.println("3. Search Book");
            System.out.println("4. Register User");
            System.out.println("5. Borrow Book");
            System.out.println("6. Check Overdue Books");
            System.out.println("7. Pay Fine");
            System.out.println("8. Send Reminder for Overdue Books");
            System.out.println("9. Unregister User");
            System.out.println("10. Logout");
            System.out.println("11. Exit");
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
                        System.out.println("Login successful ♥");
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

                    boolean added = service.addBook(new Book(title, author, isbn));
                    if (added) {
                        System.out.println("Book added successfully.");
                    }
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
                    System.out.print("Enter new user name: ");
                    String name = sc.nextLine();
                    LibraryUser newUser = new LibraryUser(name);
                    service.addUser(newUser);
                    System.out.println("User registered successfully.");
                    break;

                case 5:
                    List<LibraryUser> usersForBorrow = service.getUsers();
                    if (usersForBorrow.isEmpty()) {
                        System.out.println("No users registered yet.");
                        break;
                    }
                    for (int i = 0; i < usersForBorrow.size(); i++) {
                        System.out.println((i + 1) + ". " + usersForBorrow.get(i).getName());
                    }
                    System.out.print("Select user number: ");
                    int userNum = sc.nextInt() - 1;
                    sc.nextLine();

                    if (userNum < 0 || userNum >= usersForBorrow.size()) {
                        System.out.println("Invalid user number!");
                        break;
                    }
                    LibraryUser userToBorrow = usersForBorrow.get(userNum);

                    List<Book> allBooks = service.getAllBooks();
                    System.out.println("Available books:");
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
                        service.borrowBook(userToBorrow, bookToBorrow);
                    } else {
                        System.out.println("Invalid book number!");
                    }
                    break;

                case 6:
                    List<LibraryUser> usersForCheck = service.getUsers();
                    if (usersForCheck.isEmpty()) {
                        System.out.println("No users registered yet.");
                        break;
                    }
                    for (int i = 0; i < usersForCheck.size(); i++) {
                        System.out.println((i + 1) + ". " + usersForCheck.get(i).getName());
                    }
                    System.out.print("Select user number: ");
                    int checkUserNum = sc.nextInt() - 1;
                    sc.nextLine();

                    if (checkUserNum >= 0 && checkUserNum < usersForCheck.size()) {
                        LibraryUser userToCheck = usersForCheck.get(checkUserNum);
                        service.checkOverdueBooks(userToCheck);
                    } else {
                        System.out.println("Invalid user number!");
                    }
                    break;

                case 7:
                    List<LibraryUser> usersForPay = service.getUsers();
                    if (usersForPay.isEmpty()) {
                        System.out.println("No users registered yet.");
                        break;
                    }
                    for (int i = 0; i < usersForPay.size(); i++) {
                        System.out.println((i + 1) + ". " + usersForPay.get(i).getName());
                    }
                    System.out.print("Select user number: ");
                    int payUserNum = sc.nextInt() - 1;
                    sc.nextLine();

                    if (payUserNum >= 0 && payUserNum < usersForPay.size()) {
                        LibraryUser userToPay = usersForPay.get(payUserNum);
                        System.out.println("Current fine: " + userToPay.getFineBalance());
                        System.out.print("Enter amount to pay: ");
                        double amt = sc.nextDouble();
                        sc.nextLine();
                        service.payFine(userToPay, amt);
                        System.out.println("New balance: " + userToPay.getFineBalance());
                    } else {
                        System.out.println("Invalid user number!");
                    }
                    break;

                case 8:
                    List<LibraryUser> usersForReminder = service.getUsers();
                    if (usersForReminder.isEmpty()) {
                        System.out.println("No users registered yet.");
                        break;
                    }
                    for (int i = 0; i < usersForReminder.size(); i++) {
                        System.out.println((i + 1) + ". " + usersForReminder.get(i).getName());
                    }
                    System.out.print("Select user number: ");
                    int reminderUserNum = sc.nextInt() - 1;
                    sc.nextLine();

                    if (reminderUserNum >= 0 && reminderUserNum < usersForReminder.size()) {
                        LibraryUser userToRemind = usersForReminder.get(reminderUserNum);
                        service.sendReminder(userToRemind);
                        System.out.println("Reminder sent ♥");
                    } else {
                        System.out.println("Invalid user number!");
                    }
                    break;

                case 9: // Unregister user
                    if (!admin.isLoggedIn()) {
                        System.out.println("Please login as admin first!");
                        break;
                    }
                    List<LibraryUser> usersForRemove = service.getUsers();
                    if (usersForRemove.isEmpty()) {
                        System.out.println("No users registered yet.");
                        break;
                    }
                    for (int i = 0; i < usersForRemove.size(); i++) {
                        System.out.println((i + 1) + ". " + usersForRemove.get(i).getName());
                    }
                    System.out.print("Select user number to unregister: ");
                    int removeUserNum = sc.nextInt() - 1;
                    sc.nextLine();

                    if (removeUserNum >= 0 && removeUserNum < usersForRemove.size()) {
                        LibraryUser userToRemove = usersForRemove.get(removeUserNum);
                        service.unregisterUser(admin, userToRemove);
                    } else {
                        System.out.println("Invalid user number!");
                    }
                    break;

                case 10:
                    admin.logout();
                    System.out.println("Logged out ♥");
                    break;

                case 11:
                    System.out.println("Bye!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
