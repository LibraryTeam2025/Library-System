package librarymanagement.application;

import librarymanagement.domain.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {
    private final EmailService emailService;
    private final UserService userService;
    private final List<Media> mediaList = new ArrayList<>();
    private final List<LibraryUser> users = new ArrayList<>();

    private static final String BOOKS_FILE = "books.txt";
    private static final String CDS_FILE = "cds.txt";
    private static final String FINES_FILE = "users_fines.txt";

    public LibraryService(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
        this.users.addAll(userService.getUsers());
        loadMediaFromFiles();
        userService.setLibraryService(this);
        userService.loadBorrowedMedia();
        loadFines();

        // Recalculate fines for ALL users when program starts (very important!)
        for (LibraryUser user : users) {
            checkOverdueMedia(user);
        }
        saveFines(); // Save any newly calculated fines
    }

    // Users
    public LibraryUser getUserByName(String name) {
        if (name == null) return null;
        return users.stream().filter(u -> u.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void addUser(LibraryUser user) {
        if (!users.contains(user)) users.add(user);
    }

    public List<LibraryUser> getUsers() {
        return new ArrayList<>(users);
    }

    // Media
    public boolean addMedia(Media media) {
        if (media == null || media.getId() == null || media.getTitle() == null || media.getAuthor() == null) {
            System.out.println("Invalid media: missing required fields.");
            return false;
        }
        boolean exists = mediaList.stream().anyMatch(m -> m.getId().equalsIgnoreCase(media.getId()));
        if (exists) {
            System.out.println("Media with ID " + media.getId() + " already exists!");
            return false;
        }
        media.setAvailable(true);
        mediaList.add(media);
        saveMediaToFile(media);
        System.out.println(media.getTitle() + " added successfully.");
        return true;
    }

    public List<Media> getAvailableMedia() {
        List<Media> available = new ArrayList<>();
        for (Media m : mediaList) if (m.isAvailable()) available.add(m);
        return available;
    }

    public Media getMediaById(String id) {
        if (id == null) return null;
        return mediaList.stream().filter(m -> m.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public List<Media> searchMedia(String keyword) {
        List<Media> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return results;
        String lower = keyword.toLowerCase().trim();
        for (Media m : mediaList) {
            if ((m.getId() != null && m.getId().toLowerCase().contains(lower)) ||
                    (m.getTitle() != null && m.getTitle().toLowerCase().contains(lower)) ||
                    (m.getAuthor() != null && m.getAuthor().toLowerCase().contains(lower))) {
                results.add(m);
            }
        }
        return results;
    }
    public boolean borrowMedia(LibraryUser user, Media media) {
        if (!media.isAvailable()) {
            System.out.println("Media is not available: " + media.getTitle());
            return false;
        }

        // CRITICAL: Recalculate any new fines before allowing borrow
        checkOverdueMedia(user);

        if (user.isBlocked() || user.getFineBalance() > 0) {
            System.out.println("Cannot borrow: You have unpaid fines or overdue items.");
            System.out.println("Please pay your fine first: $" + String.format("%.2f", user.getFineBalance()));
            return false;
        }

        BorrowedMedia borrowed = new BorrowedMedia(media);
        user.getBorrowedMediaInternal().add(borrowed);
        media.setAvailable(false);

        System.out.println(user.getName() + " borrowed: " + media.getTitle()
                + " | Due Date: " + borrowed.getDueDate());

        // Save changes
        userService.saveBorrowedMedia();
        saveFines();

        return true;
    }
    public void returnMedia(LibraryUser user, BorrowedMedia borrowed) {
        borrowed.returnMedia();
        borrowed.getMedia().setAvailable(true);

        checkOverdueMedia(user);

        if (user.getFineBalance() == 0 && !user.hasOverdueItems()) {
            user.setBlocked(false);
        }

        System.out.println(user.getName() + " returned: " + borrowed.getMedia().getTitle());
    }

    public void checkOverdueMedia(LibraryUser user) {
        double newFines = 0;

        for (BorrowedMedia bm : user.getBorrowedMediaInternal()) {
            if (!bm.isReturned() && !bm.isFineAdded() && bm.isOverdue()) {
                newFines += bm.calculateFine();
                bm.setFineAdded(true);
            }
        }

        if (newFines > 0) {
            user.addFine(newFines);
        }

        user.setBlocked(user.getFineBalance() > 0 || user.hasOverdueItems());
    }

    public void payFine(LibraryUser user, double amount) {
        if (user == null || amount <= 0) return;

        double payment = Math.min(amount, user.getFineBalance());
        user.setFineBalance(user.getFineBalance() - payment);

        for (BorrowedMedia bm : user.getBorrowedMediaInternal()) {
            if (!bm.isReturned() && bm.isOverdue()) {
                bm.setFineAdded(true);
            }
        }

        boolean hasOverdue = user.getBorrowedMedia().stream().anyMatch(BorrowedMedia::isOverdue);
        user.setBlocked(user.getFineBalance() > 0 || hasOverdue);

        userService.saveUsers();
        userService.saveBorrowedMedia();
        saveFines();

        System.out.println(user.getName() + " paid $" + payment + ". Remaining fine: $" + user.getFineBalance());
    }



    private void updateAllFines() {
        for (LibraryUser u : users) checkOverdueMedia(u);
    }

    private void loadFines() {
        File file = new File(FINES_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 2) continue;

                String name = parts[0];
                double savedFine = Double.parseDouble(parts[1]);
                LibraryUser user = getUserByName(name);
                if (user != null) {
                    user.setFineBalance(savedFine);
                    user.setBlocked(savedFine > 0 || user.hasOverdueItems());
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading fines: " + e.getMessage());
        }
    }

    public void saveFines() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FINES_FILE))) {
            for (LibraryUser user : users) {
                pw.println(user.getName() + "|" + user.getFineBalance());
            }
        } catch (IOException e) {
            System.out.println("Error saving fines: " + e.getMessage());
        }
    }


    // Media Files
    private void saveMediaToFile(Media media) {
        String fileName = media instanceof Book ? BOOKS_FILE : CDS_FILE;
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, true))) {
            pw.println(media.getId() + "|" + media.getTitle() + "|" + media.getAuthor());
        } catch (IOException e) {
            System.out.println("Error saving media: " + e.getMessage());
        }
    }

    private void loadMediaFromFiles() {
        loadMediaFromFile(BOOKS_FILE, "Book");
        loadMediaFromFile(CDS_FILE, "CD");
    }

    private void loadMediaFromFile(String fileName, String type) {
        File file = new File(fileName);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", 3);
                if (parts.length != 3) continue;

                Media media = type.equals("Book")
                        ? new Book(parts[1], parts[2], parts[0])
                        : new CD(parts[1], parts[2], parts[0]);

                media.setAvailable(true);
                mediaList.add(media);
            }
        } catch (IOException e) {
            System.out.println("Error loading " + fileName + ": " + e.getMessage());
        }
    }

    public void sendReminder(LibraryUser user) {
        long overdueCount = user.getBorrowedMedia().stream()
                .filter(bm -> !bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate()))
                .count();

        if (overdueCount > 0 && !user.getEmail().trim().isEmpty()) {
            String subject = "تذكير هام: لديك مواد متأخرة في المكتبة";
            String message = "عزيزي " + user.getName() + "،\n\n" +
                    "لديك " + overdueCount + " عنصر متأخر عن موعد الإرجاع.\n" +
                    "الرجاء إرجاعها في أقرب وقت لتجنب غرامات إضافية.\n\n" +
                    "شكراً لتعاونك\nفريق المكتبة";

            emailService.sendEmail(user.getEmail(), subject, message);  // ← إرسال حقيقي
        } else {
            System.out.println(user.getName() + " لا يوجد تأخير أو لا يملك إيميل.");
        }
    }
}
