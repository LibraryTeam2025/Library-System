package librarymanagement.presentation;

import librarymanagement.domain.*;
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
            System.out.println("2. Add Media");
            System.out.println("3. Search Media");
            System.out.println("4. Register User");
            System.out.println("5. Borrow Media");
            System.out.println("6. Check Overdue Media");
            System.out.println("7. Pay Fine");
            System.out.println("8. Send Reminder for Overdue Media");
            System.out.println("9. Unregister User");
            System.out.println("10. Return Media");
            System.out.println("11. Logout");
            System.out.println("12. Exit");
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
                    System.out.print("Media type (Book/CD): ");
                    String mediaTypeInput = sc.nextLine().trim();  // ← اسم آمن
                    System.out.print("Title: ");
                    String title = sc.nextLine();
                    System.out.print("Author/Artist: ");
                    String author = sc.nextLine();
                    System.out.print("ID/ISBN: ");
                    String id = sc.nextLine();

                    Media media = null;
                    if (mediaTypeInput.equalsIgnoreCase("Book")) {
                        media = new Book(title, author, id);
                    } else if (mediaTypeInput.equalsIgnoreCase("CD")) {
                        media = new CD(title, author, id);
                    } else {
                        System.out.println("Unknown media type! Use 'Book' or 'CD'");
                        break;
                    }

                    if (service.addMedia(media)) {
                        System.out.println(mediaTypeInput + " added successfully.");
                    }
                    break;
                case 3:
                    System.out.println("Search by title, author, or ID (e.g., 123 for exact ID match)");
                    System.out.print("Enter keyword: ");
                    String keyword = sc.nextLine();
                    List<Media> results = service.searchMedia(keyword);

                    if (results.isEmpty()) {
                        System.out.println("No results found for '" + keyword + "'");
                    } else {
                        System.out.println("Found " + results.size() + " result(s):");
                        for (int i = 0; i < results.size(); i++) {
                            Media m = results.get(i);
                            String mediaType = m instanceof Book ? "Book" : "CD";  // ← اسم آمن
                            String marker = m.getId().equalsIgnoreCase(keyword.trim()) ? " [EXACT ID MATCH]" : "";
                            System.out.println((i + 1) + ". [" + mediaType + "] " + m + marker);
                        }
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

                    List<Media> allMedia = service.getAllMedia();
                    System.out.println("Available media:");
                    for (int i = 0; i < allMedia.size(); i++) {
                        Media m = allMedia.get(i);
                        if (m.isAvailable()) {
                            System.out.println((i + 1) + ". " + m);
                        }
                    }
                    System.out.print("Enter media number to borrow: ");
                    int mediaIndex = sc.nextInt() - 1;
                    sc.nextLine();

                    if (mediaIndex >= 0 && mediaIndex < allMedia.size()) {
                        Media mediaToBorrow = allMedia.get(mediaIndex);
                        service.borrowMedia(userToBorrow, mediaToBorrow);
                    } else {
                        System.out.println("Invalid media number!");
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
                        service.checkOverdueMedia(userToCheck);
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

                case 9:
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
                    List<LibraryUser> usersForReturn = service.getUsers();
                    if (usersForReturn.isEmpty()) {
                        System.out.println("No users registered yet.");
                        break;
                    }
                    for (int i = 0; i < usersForReturn.size(); i++) {
                        System.out.println((i + 1) + ". " + usersForReturn.get(i).getName());
                    }
                    System.out.print("Select user number: ");
                    int returnUserNum = sc.nextInt() - 1;
                    sc.nextLine();

                    if (returnUserNum < 0 || returnUserNum >= usersForReturn.size()) {
                        System.out.println("Invalid user number!");
                        break;
                    }

                    LibraryUser userToReturn = usersForReturn.get(returnUserNum);
                    List<BorrowedMedia> borrowed = userToReturn.getBorrowedMedia();

                    if (borrowed.isEmpty()) {
                        System.out.println("This user has no borrowed media.");
                        break;
                    }

                    System.out.println("Borrowed media:");
                    for (int i = 0; i < borrowed.size(); i++) {
                        BorrowedMedia bm = borrowed.get(i);
                        System.out.println((i + 1) + ". " + bm.getMedia() + " | Due: " + bm.getDueDate());
                    }

                    System.out.print("Enter media number to return: ");
                    int mediaNum = sc.nextInt() - 1;
                    sc.nextLine();

                    if (mediaNum >= 0 && mediaNum < borrowed.size()) {
                        BorrowedMedia bm = borrowed.get(mediaNum);
                        bm.returnMedia();
                        System.out.println("Returned: " + bm.getMedia().getTitle());

                        // تحقق من الغرامات المتأخرة بعد الإرجاع
                        service.checkOverdueMedia(userToReturn);
                    } else {
                        System.out.println("Invalid media number!");
                    }
                    break;
                case 11:
                    admin.logout();
                    System.out.println("Logged out ♥");
                    break;

                case 12:
                    System.out.println("Bye!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}