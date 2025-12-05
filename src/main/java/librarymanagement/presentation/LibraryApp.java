package librarymanagement.presentation;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import librarymanagement.util.EnvLoader;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class LibraryApp {

    private static final String RESET = "\u001B[0m";
    private static final String BROWN = "\u001B[38;5;130m";
    private static final String BEIGE = "\u001B[38;5;223m";
    private static final String DARK_BROWN = "\u001B[38;5;94m";
    private static final String LIGHT_BEIGE = "\u001B[38;5;230m";
    private static final String ERROR = "\u001B[38;5;124m";
    private static final String GREEN = "\u001B[38;5;120m";
    private static final String RED = "\u001B[38;5;196m";

    private static final Scanner sc = new Scanner(System.in);
    private static final AdminService adminService = new AdminService("admins.txt");
    private static final EmailService emailService = new EmailService();
    private static final UserService userService;
    private static final LibraryService service;

    private static Admin currentAdmin = null;
    private static LibraryUser currentUser = null;

    static {
        userService = new UserService("users.txt", "borrowed.txt");
        service = new LibraryService(emailService, userService);
        userService.setLibraryService(service);
        userService.loadBorrowedMedia();
    }

    public static void main(String[] args) {
        clearScreen();
        setupDefaultAdminsFromEnv();
        printWelcomeBanner();

        while (true) {
            showMainMenu();
            int choice = getIntInput();
            switch (choice) {
                case 1 -> adminFlow();
                case 2 -> userFlow();
                case 3 -> userSignUp();
                case 4 -> exitSystem();
                default -> printError("Invalid choice! Try again.");
            }
        }
    }

    private static void setupDefaultAdminsFromEnv() {
        if (!adminService.getAdmins().isEmpty()) return;

        Map<String, String> env = EnvLoader.load();
        String ownerEmail = env.get("OWNER_EMAIL");
        String ownerPass  = env.get("OWNER_PASS");
        String smallEmail = env.get("SMALL_ADMIN_EMAIL");
        String smallPass  = env.get("SMALL_ADMIN_PASS");

        if (ownerEmail == null || ownerPass == null || smallEmail == null || smallPass == null) {
            clearScreen();
            printHeader("CRITICAL ERROR");
            System.out.println(ERROR + "   pass.env file not found or incomplete!" + RESET);
            System.out.println(ERROR + "   Create pass.env in project root with correct data." + RESET);
            delay(6000);
            System.exit(1);
        }

        adminService.addSuperAdmin("Eleen Bzoor", ownerEmail.trim(), ownerPass.trim());
        adminService.addSmallAdmin("Yaman Abu Asal", smallEmail.trim(), smallPass.trim());

        if (userService.getUserByName("Yaman Abu Asal") == null) {
            userService.addUser("Yaman Abu Asal", smallPass.trim(), smallEmail.trim());
            service.addUser(userService.getUserByName("Yaman Abu Asal"));
        }

        clearScreen();
        printHeader("Setup Complete");
        System.out.println(BEIGE + "   Default accounts created successfully!" + RESET);
        System.out.println(LIGHT_BEIGE + "   OWNER       : " + ownerEmail.trim() + RESET);
        System.out.println(LIGHT_BEIGE + "   SMALL ADMIN : " + smallEmail.trim() + RESET);
        System.out.println(LIGHT_BEIGE + "\n   Press Enter to continue..." + RESET);
        sc.nextLine();
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void delay(int ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }

    private static void printWelcomeBanner() {
        clearScreen();
        String banner = BROWN +
                "╔══════════════════════════════════════════════════╗\n" +
                "║" + BEIGE + "      Library Management System              " + BROWN + "     ║\n" +
                "╚══════════════════════════════════════════════════╝" + RESET;
        System.out.println(center(banner, 60));
        delay(800);
        System.out.println(LIGHT_BEIGE + "\n           Press Enter to continue..." + RESET);
        sc.nextLine();
        clearScreen();
    }

    private static String center(String text, int width) {
        String clean = text.replaceAll("\u001B\\[[;\\d]*m", "");
        int padding = (width - clean.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private static void printHeader(String title) {
        clearScreen();
        String line = DARK_BROWN + "═".repeat(60) + RESET;
        System.out.println(line);
        System.out.println(center(BEIGE + "  " + title + "  " + RESET, 60));
        System.out.println(line + "\n");
    }

    private static void printSuccess(String msg) {
        System.out.println(BEIGE + "   " + msg + " " + RESET);
    }

    private static void printError(String msg) {
        System.out.println(ERROR + "   Error: " + msg + RESET);
    }

    private static int getIntInput() {
        while (!sc.hasNextInt()) {
            printError("Please enter a valid number!");
            sc.next();
        }
        int n = sc.nextInt();
        sc.nextLine();
        return n;
    }

    private static void showMainMenu() {
        printHeader("Main Menu");
        System.out.println(BROWN + "   1. Admin Login" + RESET);
        System.out.println(BROWN + "   2. User Login" + RESET);
        System.out.println(BROWN + "   3. User Sign Up" + RESET);
        System.out.println(BROWN + "   4. Exit" + RESET);
        System.out.print(DARK_BROWN + "\n   ➤ Choose: " + RESET);
    }

    private static boolean adminLogin() {
        printHeader("Admin Login");
        System.out.print(BROWN + "   Your Email : " + RESET);
        String id = sc.nextLine().trim();
        System.out.print(BROWN + "   Your Password: " + RESET);
        String pass = sc.nextLine();

        currentAdmin = adminService.login(id, pass);
        if (currentAdmin != null) {
            printSuccess("Welcome back, " + currentAdmin.getName() + "!");
            delay(1200);
            return true;
        }
        printError("Invalid email or password.");
        delay(1500);
        return false;
    }

    private static void printAdminMenu() {
        String role = currentAdmin.isOwner() ? "[OWNER]" : "[SMALL ADMIN]";
        printHeader("Admin Panel - " + currentAdmin.getName() + " " + role);

        if (currentAdmin.isOwner()) {
            System.out.println(BROWN + "   1. Register New Small Admin" + RESET);
            System.out.println(BROWN + "   2. Delete User" + RESET);
            System.out.println(BROWN + "   3. Logout" + RESET);
        } else {
            System.out.println(BROWN + "   1. Add Book/CD" + RESET);
            System.out.println(BROWN + "   2. Search Book/CD" + RESET);
            System.out.println(BROWN + "   3. Borrow Book/CD" + RESET);
            System.out.println(BROWN + "   4. Return Book/CD" + RESET);
            System.out.println(BROWN + "   5. My Overdue Items" + RESET);
            System.out.println(BROWN + "   6. Pay Fine" + RESET);
            System.out.println(BROWN + "   7. View Overdue Users" + RESET);
            System.out.println(BROWN + "   8. Send Overdue Reminder" + RESET);
            System.out.println(BROWN + "   9. Logout" + RESET);
        }
        System.out.print(DARK_BROWN + "\n   ➤ Choose: " + RESET);
    }

    private static void adminFlow() {
        if (!adminLogin()) return;

        while (true) {
            printAdminMenu();
            int choice = getIntInput();

            if (currentAdmin.isOwner()) {
                switch (choice) {
                    case 1 -> registerNewSmallAdmin();
                    case 2 -> unregisterUser();
                    case 3 -> { logoutAdmin(); return; }
                    default -> printError("Invalid choice!");
                }
            } else {
                switch (choice) {
                    case 1 -> addMedia();
                    case 2 -> searchMedia();
                    case 3 -> borrowMediaAsAdmin();
                    case 4 -> returnMediaAsAdmin();
                    case 5 -> checkOverdueItemsForAdmin();
                    case 6 -> payFineForAdmin();
                    case 7 -> viewOverdueUsers();
                    case 8 -> sendOverdueReminderToAnyone();
                    case 9 -> { logoutAdmin(); return; }
                    default -> printError("Invalid choice!");
                }
            }
            delay(800);
        }
    }

    private static void logoutAdmin() {
        if (currentAdmin != null) currentAdmin.logout();
        currentAdmin = null;
        printSuccess("Logged out successfully.");
        delay(1200);
    }

    private static void registerNewSmallAdmin() {
        printHeader("Register New Small Admin");
        System.out.print(BROWN + "   Name: " + RESET);
        String name = sc.nextLine().trim();
        System.out.print(BROWN + "   Email: " + RESET);
        String email = sc.nextLine().trim();
        System.out.print(BROWN + "   Password: " + RESET);
        String pass = sc.nextLine();

        if (adminService.addSmallAdmin(name, email, pass)) {

            LibraryUser linkedUser = userService.getUserByName(name);
            if (linkedUser == null) {
                linkedUser = new LibraryUser(name, "", email);
                userService.getUsers().add(linkedUser);
                userService.saveUsers();
            }

            printSuccess("Small Admin '" + name + "' created successfully!");
        } else {
            printError("Email already exists.");
        }
        delay(1800);
    }


    private static boolean userLogin() {
        printHeader("User Login");
        System.out.print(BROWN + "   Name: " + RESET);
        String name = sc.nextLine().trim();
        System.out.print(BROWN + "   Password: " + RESET);
        String pass = sc.nextLine();

        currentUser = userService.login(name, pass);
        if (currentUser != null) {
            // Double-check fines after login (in case date changed)
            service.checkOverdueMedia(currentUser);

            if (currentUser.getFineBalance() > 0 || currentUser.hasOverdueItems()) {
                printError("You have overdue items or unpaid fines!");
                System.out.println(RED + "   Total Fine: $" + String.format("%.2f", currentUser.getFineBalance()) + " NIS" + RESET);
                delay(3000);
            } else {
                printSuccess("Welcome back, " + currentUser.getName() + "!");
            }

            service.addUser(currentUser);
            delay(1200);
            return true;
        }
        printError("Invalid name or password.");
        delay(1500);
        return false;
    }


    private static void printUserMenu() {
        printHeader("User Panel - " + currentUser.getName());
        System.out.println(BROWN + "   1. Borrow Book/CD" + RESET);
        System.out.println(BROWN + "   2. Return Book/CD" + RESET);
        System.out.println(BROWN + "   3. My Overdue Items" + RESET);
        System.out.println(BROWN + "   4. Pay Fine" + RESET);
        System.out.println(BROWN + "   5. Search Media" + RESET);
        System.out.println(BROWN + "   6. Logout" + RESET);
        System.out.print(DARK_BROWN + "\n   ➤ Choose: " + RESET);
    }

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
                case 6 -> { currentUser = null; printSuccess("Logged out."); delay(1200); return; }
                default -> printError("Invalid choice!");
            }
            delay(800);
        }
    }

    private static void userSignUp() {
        printHeader("User Registration");
        System.out.print(BROWN + " Name: " + RESET);
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            printError("Name cannot be empty!");
            delay(1500);
            return;
        }

        System.out.print(BROWN + " Email: " + RESET);
        String email = sc.nextLine().trim();
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            printError("Invalid email format!");
            delay(1500);
            return;
        }

        System.out.print(BROWN + " Password: " + RESET);
        String pass = sc.nextLine();

        if (userService.addUser(name, pass, email)) {
            printSuccess("User '" + name + "' registered with email: " + email);
            service.addUser(userService.getUserByName(name));
        } else {
            printError("Username already exists!");
        }
        delay(1800);
    }

    private static void addMedia() {
        printHeader("Add New Media");

        System.out.print(BROWN + "   Type (Book/CD): " + RESET);
        String type = sc.nextLine().trim();

        System.out.print(BROWN + "   Title: " + RESET);
        String title = sc.nextLine();

        System.out.print(BROWN + "   Author/Artist: " + RESET);
        String author = sc.nextLine();

        System.out.print(BROWN + "   ID/ISBN: " + RESET);
        String id = sc.nextLine();

        System.out.print(BROWN + "   Number of copies: " + RESET);
        int copies = Integer.parseInt(sc.nextLine().trim());

        Media media = null;

        if (type.equalsIgnoreCase("Book")) {
            media = new Book(title, author, id, copies);
        } else if (type.equalsIgnoreCase("CD")) {
            media = new CD(title, author, id, copies);
        }

        if (media == null) {
            printError("Invalid type! Use 'Book' or 'CD'.");
        } else if (service.addMedia(media)) {
            printSuccess(type + " added successfully!");
        } else {
            printError("Media with this ID already exists.");
        }

        delay(1800);
    }

    private static void unregisterUser() {
        printHeader("Delete User");
        List<LibraryUser> users = userService.getUsers();
        if (users.isEmpty()) {
            printError("No users to delete.");
            delay(1500);
            return;
        }

        System.out.println(BEIGE + "   Select user to delete:" + RESET);
        for (int i = 0; i < users.size(); i++) {
            System.out.println(BROWN + "   [" + (i + 1) + "] " + users.get(i).getName() + RESET);
        }

        int idx = getIntInput() - 1;
        if (idx >= 0 && idx < users.size()) {
            if (userService.removeUser(users.get(idx).getName())) {
                printSuccess("User deleted successfully.");
            } else {
                printError("Failed to delete user.");
            }
        } else {
            printError("Invalid selection.");
        }
        delay(1800);
    }

    private static void sendOverdueReminderToAnyone() {
        printHeader("Send Overdue Reminder");
        List<LibraryUser> users = userService.getUsers();
        if (users.isEmpty()) {
            printError("No users registered yet.");
            delay(1500);
            return;
        }

        System.out.println(BEIGE + "   Choose user:" + RESET);
        for (int i = 0; i < users.size(); i++) {
            System.out.println(BROWN + "   [" + (i + 1) + "] " + users.get(i).getName() + RESET);
        }

        int idx = getIntInput() - 1;
        if (idx >= 0 && idx < users.size()) {
            service.sendReminder(users.get(idx));
            printSuccess("Reminder sent to " + users.get(idx).getName() + "!");
        } else {
            printError("Invalid selection.");
        }
        delay(1800);
    }

    private static void borrowMedia() {
        printHeader("Borrow Media");

        List<Media> available = service.getAvailableMedia();
        if (available.isEmpty()) {
            printError("No media available.");
            delay(1500);
            return;
        }
        for (int i = 0; i < available.size(); i++) {
            Media m = available.get(i);
            System.out.println("[" + (i + 1) + "] " + m.getType() + " - " + m.getTitle() + " | Available: " + m.getAvailableCopies());
        }

        System.out.print("➤ Select: ");
        int choice = getIntInput();

        if (choice < 1 || choice > available.size()) {
            printError("Invalid choice!");
            delay(1500);
            return;
        }

        Media selected = available.get(choice - 1);
        if (currentUser.getFineBalance() > 0 || currentUser.isBlocked()) {
            printError("Cannot borrow: You have unpaid fines or your account is blocked.");
            delay(1500);
            return;
        }

        boolean success = service.borrowMedia(currentUser, selected);
        if (success) {
            userService.saveBorrowedMedia();

            BorrowedMedia borrowed = currentUser.getBorrowedMediaInternal()
                    .stream()
                    .filter(b -> b.getMedia() == selected && !b.isReturned())
                    .reduce((first, second) -> second)
                    .orElse(null);

            if (borrowed != null) {
                printSuccess("Borrowed successfully!");
                System.out.println("Borrow Date: " + borrowed.getBorrowDate());
                System.out.println("Due Date: " + borrowed.getDueDate());
            } else {
                printSuccess("Borrowed successfully!");
            }

        } else {
            printError("Cannot borrow this media.");
        }

        delay(1500);
    }


    private static void borrowMediaAsAdmin() {
        currentUser = userService.getUserByName(currentAdmin.getName());

        if (currentUser == null) {
            currentUser = new LibraryUser(currentAdmin.getName(), "", currentAdmin.getEmail());
            userService.getUsers().add(currentUser);
            userService.saveUsers();
        }

        borrowMedia();
    }

    private static void returnMedia() {
        printHeader("Return Media");
        List<BorrowedMedia> borrowed = currentUser.getBorrowedMedia().stream()
                .filter(b -> !b.isReturned())
                .toList();

        if (borrowed.isEmpty()) {
            printError("You have no borrowed items.");
            delay(1500);
            return;
        }

        for (int i = 0; i < borrowed.size(); i++) {
            System.out.println(BROWN + "   [" + (i + 1) + "] " + borrowed.get(i).getMedia().getTitle() +
                    " | Due: " + borrowed.get(i).getDueDate() + RESET);
        }

        System.out.print(DARK_BROWN + "   ➤ Select item: " + RESET);
        int idx = getIntInput() - 1;
        if (idx >= 0 && idx < borrowed.size()) {
            BorrowedMedia bm = borrowed.get(idx);
            bm.returnMedia();
            printSuccess("Returned successfully!");

            service.checkOverdueMedia(currentUser);
            if (currentUser.getFineBalance() == 0 && !currentUser.hasOverdueItems()) {
                currentUser.setBlocked(false);
            }

            userService.saveBorrowedMedia();


        } else {
            printError("Invalid selection.");
        }
        delay(1800);
    }

    private static void returnMediaAsAdmin() {
        currentUser = currentAdmin.getUserAccount();
        if (currentUser == null) return;
        returnMedia();
    }


    private static void checkOverdueItems() {
        printHeader("My Overdue Items");

        currentUser.updateFineBalance();

        boolean hasOverdue = false;
        for (BorrowedMedia bm : currentUser.getBorrowedMedia()) {
            if (!bm.isReturned() && bm.isOverdue()) {
                hasOverdue = true;
                System.out.println("  " + (bm.getMedia() instanceof Book ? "Book" : "CD") + " : " + bm.getMedia().getTitle());
                System.out.println("     Due Date   : " + bm.getDueDate());
                System.out.println("     Late by    : " + bm.getOverdueDays() + " day(s)");
                System.out.println("     Fine       : $" + String.format("%.2f", bm.getFine()) + " NIS");
                System.out.println("     ───────────────────────────────────");
            }
        }

        if (!hasOverdue) {
            System.out.println(GREEN + "No overdue items. You're all good!" + RESET);
        } else {
            System.out.println("\n>>> Total Fine Balance: $" + String.format("%.2f", currentUser.getFineBalance()) + " NIS <<<");
        }

        delay(3000);
    }

    private static void checkOverdueItemsForAdmin() {
        printHeader("My Overdue Items");

        currentUser = userService.getUserByName(currentAdmin.getName());
        if (currentUser == null) {
            printError("No user account linked to this admin!");
            delay(2000);
            return;
        }

        currentUser.updateFineBalance();

        boolean hasOverdue = false;
        for (BorrowedMedia bm : currentUser.getBorrowedMedia()) {
            if (!bm.isReturned() && bm.isOverdue()) {
                hasOverdue = true;
                System.out.println(BROWN + "   • " + bm.getMedia().getTitle() + RESET +
                        " | Due: " + bm.getDueDate() + " | Fine: $" + String.format("%.2f", bm.getFine()));
            }
        }

        if (!hasOverdue) {
            System.out.println(GREEN + "   No overdue items. Great job!" + RESET);
        } else {
            System.out.println(RED + "\n   Total Fine: $" + String.format("%.2f", currentUser.getFineBalance()) + RESET);
        }

        delay(4000);
    }

    private static void payFine() {
        printHeader("Pay Fine");
        currentUser.updateFineBalance();
        double fine = currentUser.getFineBalance();
        System.out.println(BEIGE + "   Current fine: $" + String.format("%.2f", fine) + RESET);

        if (fine <= 0) {
            printSuccess("No fine to pay!");
            delay(1500);
            return;
        }

        System.out.print(DARK_BROWN + "   Amount to pay: $" + RESET);
        double amount = getDoubleInput();
        if (amount <= 0) {
            printError("Invalid amount!");
            delay(1500);
            return;
        }
        if (amount > fine) amount = fine;

        currentUser.setFineBalance(fine - amount);
        currentUser.setBlocked(currentUser.getFineBalance() > 0 || currentUser.hasOverdueItems());

        userService.saveUsers();
        userService.saveBorrowedMedia();
        service.saveFines();

        System.out.println(currentUser.getName() + " paid $" + amount + ". Remaining: $" + currentUser.getFineBalance());
        delay(2000);
    }



    private static void payFineForAdmin() {
        currentUser = userService.getUserByName(currentAdmin.getName());
        if (currentUser == null) {
            printError("No user account linked to this admin!"); delay(1500); return;
        }
        payFine();
    }


    private static void viewOverdueUsers() {
        printHeader("Overdue Users List");
        List<LibraryUser> allUsers = userService.getUsers();
        boolean found = false;

        System.out.println(BEIGE + "   Users with overdue items:" + RESET);
        System.out.println(DARK_BROWN + "   ═════════════════════════════════════" + RESET);

        for (LibraryUser u : allUsers) {
            long count = u.getBorrowedMedia().stream()
                    .filter(bm -> !bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate()))
                    .count();
            if (count > 0) {
                found = true;
                System.out.printf(BROWN + "   • %-20s " + RESET + LIGHT_BEIGE + "Overdue: %d | Fine: $%.2f%n" + RESET,
                        u.getName(), count, u.getFineBalance());
            }
        }

        if (!found) {
            System.out.println(LIGHT_BEIGE + "   Everyone is on time!" + RESET);
        }

        System.out.println(LIGHT_BEIGE + "\n   Press Enter to continue..." + RESET);
        sc.nextLine();
    }

    private static void searchMedia() {
        printHeader("Search Media");
        System.out.print(BROWN + "   Keyword (title/author/ID): " + RESET);
        String keyword = sc.nextLine().trim();

        List<Media> results = service.searchMedia(keyword);
        if (results.isEmpty()) {
            printError("No results found.");
        } else {
            System.out.println(BEIGE + "   Found " + results.size() + " result(s):" + RESET);
            results.forEach(m -> System.out.println(BROWN + "   • " + m.getType() + " - " + m.getTitle() + RESET));
        }
        delay(3000);
    }

    private static void exitSystem() {
        clearScreen();
        String bye = BROWN +
                "╔══════════════════════════════════════════════════╗\n" +
                "║" + BEIGE + "   Thank you for using Library System!        " + BROWN + "║\n" +
                "╚══════════════════════════════════════════════════╝" + RESET;
        System.out.println(center(bye, 60));
        delay(2000);
        sc.close();
        System.exit(0);
    }

    private static double getDoubleInput() {
        while (!sc.hasNextDouble()) {
            printError("Please enter a valid amount!");
            sc.next();
        }
        double val = sc.nextDouble();
        sc.nextLine();
        return val;
    }
}
