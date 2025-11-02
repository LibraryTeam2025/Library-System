package librarymanagement.presentation;

import librarymanagement.domain.*;
import librarymanagement.application.LibraryService;
import librarymanagement.application.EmailService;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class LibraryApp {

    // ====================== Warm Colors (Brown + Beige + Golden) ======================
    private static final String RESET = "\u001B[0m";
    private static final String BROWN = "\u001B[38;5;130m";     // Warm Brown
    private static final String BEIGE = "\u001B[38;5;223m";     // Soft Beige
    private static final String DARK_BROWN = "\u001B[38;5;94m"; // Deep Brown
    private static final String LIGHT_BEIGE = "\u001B[38;5;230m"; // Light Beige
    private static final String ERROR = "\u001B[38;5;124m";     // Deep Red-Brown

    private static final Scanner sc = new Scanner(System.in);
    private static final AdminService adminService = new AdminService("admins.txt");
    private static final UserService userService = new UserService("users.txt");
    private static final EmailService emailService = new EmailService();
    private static final LibraryService service = new LibraryService(emailService, userService);
    private static Admin currentAdmin = null;
    private static LibraryUser currentUser = null;
    private static String ADMIN_MASTER_KEY;

    // Load Master Key
    static {
        try (BufferedReader br = new BufferedReader(new FileReader("masterkey.txt"))) {
            String key = br.readLine();
            ADMIN_MASTER_KEY = (key != null && !key.trim().isEmpty()) ? key.trim() : "default123";
        } catch (Exception e) {
            printWarning("Warning: masterkey.txt not found. Using fallback: default123");
            ADMIN_MASTER_KEY = "default123";
        }
    }

    public static void main(String[] args) {
        clearScreen();
        printWelcomeBanner();

        while (true) {
            showMainMenu();
            int choice = getIntInput();
            switch (choice) {
                case 1 -> adminFlow();
                case 2 -> userFlow();
                case 3 -> adminSignUp();
                case 4 -> userSignUp();
                case 5 -> exitSystem();
                default -> printError("Invalid choice!");
            }
        }
    }

    // ====================== Visual Effects ======================
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void delay(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }

    private static void printWelcomeBanner() {
        clearScreen();
        String banner =
                BROWN + "╔══════════════════════════════════════════════════╗\n" +
                        "║" + BEIGE + "        Library Management System ♥               " + BROWN + "║\n" +
                        "╚══════════════════════════════════════════════════╝" + RESET;
        System.out.println(center(banner, 60));
        delay(800);
        System.out.println(LIGHT_BEIGE + "\n          Press Enter to continue..." + RESET);
        sc.nextLine();
        clearScreen();
    }

    private static void printHeader(String title) {
        clearScreen();
        String line = DARK_BROWN + "═".repeat(60) + RESET;
        System.out.println(line);
        System.out.println(center(BEIGE + "  " + title + "  " + RESET, 60));
        System.out.println(line);
    }

    private static String center(String text, int width) {
        String clean = text.replaceAll("\u001B\\[[;\\d]*m", "");
        int padding = (width - clean.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private static void printSuccess(String msg) {
        System.out.println(BEIGE + "   " + msg + " ♥" + RESET);  // بيج + قلب
    }

    private static void printError(String msg) {
        System.out.println(ERROR + "   Error: " + msg + RESET);
    }

    private static void printWarning(String msg) {
        System.out.println(BROWN + "   Warning: " + msg + RESET);
    }

    // ====================== Menus ======================
    private static void showMainMenu() {
        printHeader("Main Menu ♥ ");
        String[] options = {
                "1. Admin Login",
                "2. User Login",
                "3. Admin Sign Up (First Time / Master Key)",
                "4. User Sign Up",
                "5. Exit"
        };
        for (String opt : options) {
            System.out.println(BROWN + "  ➤ " + BEIGE + opt + RESET);  // سهم عريض
        }
        System.out.print(DARK_BROWN + "\n   Select: " + RESET);
    }

    private static void printAdminMenu() {
        printHeader("Admin Dashboard (" + currentAdmin.getUsername() + ")");
        String[] opts = {
                "1. Add Media (Book/CD)",
                "2. Unregister User",
                "3. Register New User",
                "4. Send Overdue Reminder",
                "5. Logout"
        };
        for (String opt : opts) {
            System.out.println(BROWN + "   ➤ " + BEIGE + opt + RESET);
        }
        System.out.print(DARK_BROWN + "\n   Choose: " + RESET);
    }

    private static void printUserMenu() {
        printHeader("User Dashboard (" + currentUser.getName() + ")");
        String[] opts = {
                "1. Borrow Media",
                "2. Return Media",
                "3. Check Overdue Items",
                "4. Pay Fine",
                "5. Search Media",
                "6. Logout"
        };
        for (String opt : opts) {
            System.out.println(BROWN + "  ➤ " + BEIGE + opt + RESET);
        }
        System.out.print(DARK_BROWN + "\n   Choose: " + RESET);
    }

    // ====================== Admin Flow ======================
    private static void adminFlow() {
        if (adminService.getAdmins().isEmpty()) {
            printError("No admin found. Please create one first.");
            delay(1500);
            return;
        }
        if (!adminLogin()) {
            delay(1000);
            return;
        }

        while (true) {
            printAdminMenu();
            int choice = getIntInput();
            switch (choice) {
                case 1 -> addMedia();
                case 2 -> unregisterUser();
                case 3 -> registerNewUser();
                case 4 -> sendOverdueReminder();
                case 5 -> { adminLogout(); return; }
                default -> printError("Invalid option!");
            }
            delay(800);
        }
    }

    // ====================== User Flow ======================
    private static void userFlow() {
        if (!userLogin()) {
            delay(1000);
            return;
        }

        while (true) {
            printUserMenu();
            int choice = getIntInput();
            switch (choice) {
                case 1 -> borrowMedia();
                case 2 -> returnMedia();
                case 3 -> checkOverdueItems();
                case 4 -> payFine();
                case 5 -> searchMedia();
                case 6 -> { currentUser = null; printSuccess("Logged out successfully."); delay(1000); return; }
                default -> printError("Invalid option!");
            }
            delay(800);
        }
    }

    // ====================== Login ======================
    private static boolean adminLogin() {
        printHeader("Admin Login ♥");
        System.out.print(BROWN + "   Username: " + RESET);
        String username = sc.nextLine().trim();
        System.out.print(BROWN + "   Password: " + RESET);
        String password = sc.nextLine().trim();

        currentAdmin = adminService.login(username, password);
        if (currentAdmin != null) {
            printSuccess("Welcome, " + username);
            return true;
        } else {
            printError("Invalid credentials.");
            return false;
        }
    }

    private static boolean userLogin() {
        printHeader("User Login ♥");
        System.out.print(BROWN + "   Name: " + RESET);
        String name = sc.nextLine().trim();
        System.out.print(BROWN + "   Password: " + RESET);
        String password = sc.nextLine().trim();

        currentUser = userService.login(name, password);
        if (currentUser != null) {
            printSuccess("Welcome, " + name);
            return true;
        } else {
            printError("Invalid credentials.");
            return false;
        }
    }

    // ====================== Sign Up ======================
    private static void adminSignUp() {
        printHeader("Create New Admin ♥");
        if (adminService.getAdmins().isEmpty()) {
            System.out.println(LIGHT_BEIGE + "   First admin setup - no master key required" + RESET);
        } else {
            System.out.print(ERROR + "   Enter Master Key: " + RESET);
            String key = sc.nextLine().trim();
            if (!ADMIN_MASTER_KEY.equals(key)) {
                printError("Invalid Master Key. Access denied.");
                delay(1500);
                return;
            }
            printSuccess("Master Key accepted.");
        }

        System.out.print(BROWN + "   New Admin Username: " + RESET);
        String username = sc.nextLine().trim();
        if (username.isEmpty()) { printError("Username cannot be empty."); return; }

        System.out.print(BROWN + "   New Admin Password: " + RESET);
        String password = sc.nextLine().trim();
        if (password.isEmpty()) { printError("Password cannot be empty."); return; }

        if (adminService.addAdmin(username, password)) {
            printSuccess("Admin '" + username + "' created successfully!");
        } else {
            printError("Username already exists.");
        }
        delay(1500);
    }

    private static void userSignUp() {
        printHeader("Register New User ♥");
        System.out.print(BROWN + "   New User Name: " + RESET);
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { printError("Name cannot be empty."); return; }

        System.out.print(BROWN + "   Set Password: " + RESET);
        String password = sc.nextLine().trim();

        if (userService.addUser(name, password)) {
            printSuccess("User '" + name + "' registered successfully!");
            LibraryUser newUser = userService.getUserByName(name);
            service.addUser(newUser);
        } else {
            printError("User already exists.");
        }
        delay(1500);
    }

    // ====================== Admin Actions ======================
    private static void addMedia() {
        printHeader("Add Media ♥");
        System.out.print(BROWN + "   Media type (Book/CD): " + RESET);
        String type = sc.nextLine().trim();
        System.out.print(BROWN + "   Title: " + RESET);
        String title = sc.nextLine();
        System.out.print(BROWN + "   Author/Artist: " + RESET);
        String author = sc.nextLine();
        System.out.print(BROWN + "   ID/ISBN: " + RESET);
        String id = sc.nextLine();

        Media media = type.equalsIgnoreCase("Book") ? new Book(title, author, id)
                : type.equalsIgnoreCase("CD") ? new CD(title, author, id) : null;

        if (media == null) {
            printError("Invalid media type! Use 'Book' or 'CD'.");
            return;
        }
        if (service.addMedia(media)) {
            printSuccess(type + " added successfully!");
        } else {
            printError("Failed to add media.");
        }
    }

    private static void unregisterUser() {
        printHeader("Unregister User ♥");
        List<LibraryUser> users = userService.getUsers();
        if (users.isEmpty()) {
            printError("No users to unregister.");
            return;
        }
        printUserList(users, "Select user to unregister:");
        int idx = getIntInput() - 1;
        if (isValidIndex(idx, users)) {
            if (userService.removeUser(users.get(idx).getName())) {
                printSuccess("User removed successfully.");
            } else {
                printError("Failed to remove user.");
            }
        }
    }

    private static void registerNewUser() {
        printHeader("Register New User (Admin) ♥");
        System.out.print(BROWN + "   Enter new user name: " + RESET);
        String name = sc.nextLine().trim();
        System.out.print(BROWN + "   Set password for user: " + RESET);
        String password = sc.nextLine().trim();

        if (userService.addUser(name, password)) {
            printSuccess("User '" + name + "' registered successfully.");
            LibraryUser newUser = userService.getUserByName(name);
            service.addUser(newUser);
        } else {
            printError("User already exists.");
        }
    }

    private static void sendOverdueReminder() {
        printHeader("Send Overdue Reminder ♥");
        List<LibraryUser> users = userService.getUsers();
        if (users.isEmpty()) {
            printError("No users available.");
            return;
        }
        printUserList(users, "Send reminder to:");
        int idx = getIntInput() - 1;
        if (isValidIndex(idx, users)) {
            service.sendReminder(users.get(idx));
            printSuccess("Reminder sent successfully!");
        }
    }

    private static void adminLogout() {
        if (currentAdmin != null) {
            currentAdmin.logout();
            currentAdmin = null;
        }
        printSuccess("Admin logged out successfully.");
    }

    // ====================== User Actions ======================
    private static void borrowMedia() {
        printHeader("Borrow Media ♥");
        List<Media> available = service.getAllMedia().stream()
                .filter(Media::isAvailable).toList();
        if (available.isEmpty()) {
            printError("No media available for borrowing.");
            return;
        }

        System.out.println(BEIGE + "   Available media:" + RESET);
        for (int i = 0; i < available.size(); i++) {
            System.out.println(BROWN + "   [" + (i+1) + "] " + available.get(i) + RESET);
        }
        System.out.print(DARK_BROWN + "   Select media: " + RESET);
        int idx = getIntInput() - 1;
        if (idx >= 0 && idx < available.size()) {
            service.borrowMedia(currentUser, available.get(idx));
            printSuccess("Media borrowed successfully!");
        } else {
            printError("Invalid selection.");
        }
    }

    private static void returnMedia() {
        printHeader("Return Media ♥");
        List<BorrowedMedia> borrowed = currentUser.getBorrowedMedia();
        if (borrowed.isEmpty()) {
            printError("You have no borrowed items.");
            return;
        }

        System.out.println(BEIGE + "   Your borrowed media:" + RESET);
        for (int i = 0; i < borrowed.size(); i++) {
            System.out.println(BROWN + "   [" + (i+1) + "] " + borrowed.get(i).getMedia() + " | Due: " + borrowed.get(i).getDueDate() + RESET);
        }
        System.out.print(DARK_BROWN + "   Select item to return: " + RESET);
        int idx = getIntInput() - 1;
        if (idx >= 0 && idx < borrowed.size()) {
            borrowed.get(idx).returnMedia();
            printSuccess("Item returned successfully.");
            service.checkOverdueMedia(currentUser);
        } else {
            printError("Invalid selection.");
        }
    }

    private static void checkOverdueItems() {
        printHeader("Check Overdue Items ♥");
        service.checkOverdueMedia(currentUser);
    }

    private static void payFine() {
        printHeader("Pay Fine ♥");
        System.out.println(BEIGE + "   Your current fine: $" + currentUser.getFineBalance() + RESET);
        System.out.print(DARK_BROWN + "   Amount to pay: " + RESET);
        double amount = sc.nextDouble(); sc.nextLine();
        service.payFine(currentUser, amount);
        printSuccess("Payment successful! New balance: $" + currentUser.getFineBalance());
    }

    private static void searchMedia() {
        printHeader("Search Media ♥");
        System.out.print(BROWN + "   Search (title/author/ID): " + RESET);
        String keyword = sc.nextLine();
        List<Media> results = service.searchMedia(keyword);
        if (results.isEmpty()) {
            printError("No results found.");
        } else {
            System.out.println(BEIGE + "   Found " + results.size() + " result(s):" + RESET);
            for (int i = 0; i < results.size(); i++) {
                Media m = results.get(i);
                String type = m instanceof Book ? "Book" : "CD";
                System.out.println(BROWN + "   [" + (i+1) + "] [" + type + "] " + m + RESET);
            }
        }
    }

    // ====================== Helper Methods ======================
    private static void printUserList(List<LibraryUser> users, String title) {
        System.out.println(BEIGE + "   " + title + RESET);
        for (int i = 0; i < users.size(); i++) {
            System.out.println(BROWN + "   [" + (i+1) + "] " + users.get(i).getName() + RESET);
        }
    }

    private static boolean isValidIndex(int idx, List<?> list) {
        if (idx >= 0 && idx < list.size()) return true;
        printError("Invalid selection!");
        return false;
    }

    private static int getIntInput() {
        while (!sc.hasNextInt()) {
            printError("Please enter a number!");
            sc.next();
        }
        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }

    private static void exitSystem() {
        clearScreen();
        String farewell =
                BROWN + "╔══════════════════════════════════════════════════╗\n" +
                        "║" + BEIGE + "   Thank you for using the Library System!  ♥     " + BROWN + "║\n" +
                        "╚══════════════════════════════════════════════════╝" + RESET;
        System.out.println(center(farewell, 60));
        delay(1500);
        sc.close();
        System.exit(0);
    }
}