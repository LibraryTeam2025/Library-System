package librarymanagement.presentation;

import librarymanagement.domain.*;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;

import java.util.List;
import java.util.Scanner;

public class LibraryApp {

    private static final Scanner sc = new Scanner(System.in);
    private static final AdminService adminService = new AdminService("admins.txt");
    private static final UserService userService = new UserService("users.txt");
    private static final EmailService emailService = new EmailService();
    private static final LibraryService service = new LibraryService(emailService);
    private static Admin currentAdmin = null;
    private static LibraryUser currentUser = null;

    public static void main(String[] args) {
        System.out.println("\n Welcome to Library Management System ♥\n");

        while (true) {
            showMainMenu();
            int role = getIntInput();
            switch (role) {
                case 1 -> adminFlow();
                case 2 -> userFlow();
                case 3 -> {
                    System.out.println("Thank you for using the system Goodbye ♥");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid choice!\n");
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("═".repeat(50));
        System.out.println("       LIBRARY MANAGEMENT SYSTEM");
        System.out.println("═".repeat(50));
        System.out.println("1. Admin Login");
        System.out.println("2. User Login");
        System.out.println("3. Exit");
        System.out.print("Select your choice: ");
    }
// admin list
    private static void adminFlow() {
        if (!adminLogin()) return;

        while (true) {
            printAdminMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1 -> addMedia();
                case 2 -> unregisterUser();
                case 3 -> registerNewUser();
                case 4 -> sendOverdueReminder();
                case 5 -> {
                    adminLogout();
                    return;
                }
                default -> System.out.println("Invalid option!\n");
            }
        }
    }

    private static void printAdminMenu() {
        System.out.println("\n" + "═".repeat(45));
        System.out.println("           ADMIN DASHBOARD (" + currentAdmin.getUsername() + ")");
        System.out.println("═".repeat(45));
        System.out.println("1. Add Media (Book/CD)");
        System.out.println("2. Unregister User");
        System.out.println("3. Register New User");
        System.out.println("4. Send Overdue Reminder");
        System.out.println("5. Logout");
        System.out.print("Choose: ");
    }

    //user list
    private static void userFlow() {
        if (!userLogin()) return;

        while (true) {
            printUserMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1 -> borrowMedia();
                case 2 -> returnMedia();
                case 3 -> checkOverdueItems();
                case 4 -> payFine();
                case 5 -> searchMedia();
                case 6 -> {
                    currentUser = null;
                    System.out.println("Logged out. Returning to main menu...\n");
                    return;
                }
                default -> System.out.println("Invalid option!\n");
            }
        }
    }

    private static void printUserMenu() {
        System.out.println("\n" + "═".repeat(45));
        System.out.println("           USER DASHBOARD (" + currentUser.getName() + ")");
        System.out.println("═".repeat(45));
        System.out.println("1. Borrow Media");
        System.out.println("2. Return Media");
        System.out.println("3. Check Overdue Items");
        System.out.println("4. Pay Fine");
        System.out.println("5. Search Media");
        System.out.println("6. Logout");
        System.out.print("Choose: ");
    }

    // admin login
    private static boolean adminLogin() {
        System.out.print("Admin Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        currentAdmin = adminService.login(username, password);
        if (currentAdmin != null) {
            System.out.println("Login successful! \nWelcome, " + username + " ♥\n");
            return true;
        } else {
            System.out.println("Invalid admin credentials.\n");
            return false;
        }
    }

    private static boolean userLogin() {
        System.out.print("User Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        currentUser = userService.login(name, password);
        if (currentUser != null) {
            System.out.println("Login successful! \nWelcome, " + name + " ♥\n");
            return true;
        } else {
            System.out.println("Invalid user credentials.\n");
            return false;
        }
    }
// what can admin do it
    private static void addMedia() {
        System.out.print("Media type (Book/CD): ");
        String type = sc.nextLine().trim();
        System.out.print("Title: ");
        String title = sc.nextLine();
        System.out.print("Author/Artist: ");
        String author = sc.nextLine();
        System.out.print("ID/ISBN: ");
        String id = sc.nextLine();

        Media media = type.equalsIgnoreCase("Book") ? new Book(title, author, id)
                : type.equalsIgnoreCase("CD") ? new CD(title, author, id) : null;

        if (media == null) {
            System.out.println("Invalid media type! Use 'Book' or 'CD'.\n");
            return;
        }
        if (service.addMedia(media)) {
            System.out.println(type + " added successfully!\n");
        } else {
            System.out.println("Failed to add media.\n");
        }
    }

    private static void unregisterUser() {
        List<LibraryUser> users = userService.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users to unregister.\n");
            return;
        }
        printUserList(users, "Select user to unregister:");
        int idx = getIntInput() - 1;
        if (isValidIndex(idx, users)) {
            if (userService.removeUser(users.get(idx).getName())) {
                System.out.println("User removed successfully.\n");
            } else {
                System.out.println("Failed to remove user.\n");
            }
        }
    }

    private static void registerNewUser() {
        System.out.print("Enter new user name: ");
        String name = sc.nextLine().trim();
        System.out.print("Set password for user: ");
        String password = sc.nextLine().trim();

        if (userService.addUser(name, password)) {
            System.out.println("User '" + name + "' registered successfully.\n");
        } else {
            System.out.println("User already exists.\n");
        }
    }

    private static void sendOverdueReminder() {
        List<LibraryUser> users = userService.getUsers();
        if (users.isEmpty()) {
            System.out.println("No users available.\n");
            return;
        }
        printUserList(users, "Send reminder to:");
        int idx = getIntInput() - 1;
        if (isValidIndex(idx, users)) {
            service.sendReminder(users.get(idx));
            System.out.println("Reminder sent successfully ♥\n");
        }
    }

    private static void adminLogout() {
        if (currentAdmin != null) {
            currentAdmin.logout();
            currentAdmin = null;
        }
        System.out.println("Admin logged out successfully.\n");
    }

    // what can user do:
    private static void borrowMedia() {
        List<Media> available = service.getAllMedia().stream()
                .filter(Media::isAvailable).toList();
        if (available.isEmpty()) {
            System.out.println("No media available for borrowing.\n");
            return;
        }

        System.out.println("Available media:");
        for (int i = 0; i < available.size(); i++) {
            System.out.println((i + 1) + ". " + available.get(i));
        }
        System.out.print("Select media to borrow: ");
        int mediaIdx = getIntInput() - 1;
        if (mediaIdx >= 0 && mediaIdx < available.size()) {
            service.borrowMedia(currentUser, available.get(mediaIdx));
            System.out.println("Media borrowed successfully!\n");
        } else {
            System.out.println("Invalid selection.\n");
        }
    }

    private static void returnMedia() {
        List<BorrowedMedia> borrowed = currentUser.getBorrowedMedia();
        if (borrowed.isEmpty()) {
            System.out.println("You have no borrowed items.\n");
            return;
        }

        System.out.println("Your borrowed media:");
        for (int i = 0; i < borrowed.size(); i++) {
            System.out.println((i + 1) + ". " + borrowed.get(i).getMedia() + " | Due: " + borrowed.get(i).getDueDate());
        }
        System.out.print("Select item to return: ");
        int idx = getIntInput() - 1;
        if (idx >= 0 && idx < borrowed.size()) {
            borrowed.get(idx).returnMedia();
            System.out.println("Item returned successfully.\n");
            service.checkOverdueMedia(currentUser);
        } else {
            System.out.println("Invalid selection.\n");
        }
    }

    private static void checkOverdueItems() {
        service.checkOverdueMedia(currentUser);
        System.out.println();
    }

    private static void payFine() {
        System.out.println("Your current fine: $" + currentUser.getFineBalance());
        System.out.print("Amount to pay: ");
        double amount = sc.nextDouble(); sc.nextLine();
        service.payFine(currentUser, amount);
        System.out.println("Payment successful! New balance: $" + currentUser.getFineBalance() + "\n");
    }

    private static void searchMedia() {
        System.out.print("Search (title/author/ID): ");
        String keyword = sc.nextLine();
        List<Media> results = service.searchMedia(keyword);
        if (results.isEmpty()) {
            System.out.println("No results found.\n");
        } else {
            System.out.println("Found " + results.size() + " result(s):");
            for (int i = 0; i < results.size(); i++) {
                Media m = results.get(i);
                String type = m instanceof Book ? "Book" : "CD";
                System.out.println((i + 1) + ". [" + type + "] " + m);
            }
            System.out.println();
        }
    }

    private static void printUserList(List<LibraryUser> users, String title) {
        System.out.println(title);
        for (int i = 0; i < users.size(); i++) {
            System.out.println((i + 1) + ". " + users.get(i).getName());
        }
    }

    private static boolean isValidIndex(int idx, List<?> list) {
        if (idx >= 0 && idx < list.size()) return true;
        System.out.println("Invalid selection!\n");
        return false;
    }

    private static int getIntInput() {
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a number: ");
            sc.next();
        }
        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }
}