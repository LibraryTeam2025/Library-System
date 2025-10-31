package librarymanagement.application;

import librarymanagement.domain.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {
    private EmailService emailService;
    private List<Media> mediaList = new ArrayList<>();
    private List<LibraryUser> users = new ArrayList<>();

    private static final String BOOKS_FILE = "books.txt";
    private static final String CDS_FILE = "cds.txt";

    public LibraryService(EmailService emailService) {
        this.emailService = emailService;
        loadMediaFromFiles();
    }

    public boolean addMedia(Media media) {
        if (media == null) {
            System.out.println("Invalid media: object is null!");
            return false;
        }

        if (media.getId() == null || media.getTitle() == null || media.getAuthor() == null) {
            System.out.println("Invalid media: missing ID, title, or author!");
            return false;
        }

        boolean exists = mediaList.stream()
                .anyMatch(m -> m.getId() != null && m.getId().equalsIgnoreCase(media.getId()));

        if (exists) {
            System.out.println("Media with ID " + media.getId() + " already exists!");
            return false;
        }

        mediaList.add(media);
        saveMediaToFile(media);
        System.out.println(media.getTitle() + " added successfully.");
        return true;
    }

    private void saveMediaToFile(Media media) {
        String fileName = media instanceof Book ? BOOKS_FILE : CDS_FILE;
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, true))) {
            pw.println(media.getId() + "|" + media.getTitle() + "|" + media.getAuthor());
        } catch (IOException e) {
            System.out.println("Error saving media to file: " + e.getMessage());
        }
    }

    private void loadMediaFromFiles() {
        loadMediaFromFile(BOOKS_FILE, "Book");
        loadMediaFromFile(CDS_FILE, "CD");
    }

    private void loadMediaFromFile(String fileName, String type) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("No " + fileName + " found. Starting fresh.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", 3); // id|title|author
                if (parts.length != 3) continue;

                Media media = type.equals("Book")
                        ? new Book(parts[1], parts[2], parts[0])
                        : new CD(parts[1], parts[2], parts[0]);

                mediaList.add(media);
            }
            System.out.println("Loaded " + mediaList.size() + " items from " + fileName);
        } catch (IOException e) {
            System.out.println("Error loading from " + fileName + ": " + e.getMessage());
        }
    }

    public List<Media> searchMedia(String keyword) {
        List<Media> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return results;

        String lowerKeyword = keyword.toLowerCase().trim();

        List<Media> exactIdMatches = new ArrayList<>();
        List<Media> partialMatches = new ArrayList<>();

        for (Media m : mediaList) {
            if (m == null) continue;

            String title = m.getTitle() != null ? m.getTitle().toLowerCase() : "";
            String author = m.getAuthor() != null ? m.getAuthor().toLowerCase() : "";
            String id = m.getId() != null ? m.getId().toLowerCase() : "";

            if (id.equals(lowerKeyword)) {
                exactIdMatches.add(m);
            } else if (title.contains(lowerKeyword) || author.contains(lowerKeyword) || id.contains(lowerKeyword)) {
                partialMatches.add(m);
            }
        }

        results.addAll(exactIdMatches);
        results.addAll(partialMatches);
        return results;
    }

    public List<Media> getAllMedia() {
        return new ArrayList<>(mediaList);
    }

    public boolean borrowMedia(LibraryUser user, Media media) {
        if (!mediaList.contains(media)) {
            System.out.println("Media not found in library: " + media.getTitle());
            return false;
        }

        if (!media.isAvailable()) {
            System.out.println("Media is not available: " + media.getTitle());
            return false;
        }

        boolean hasOverdue = user.getBorrowedMedia().stream()
                .anyMatch(bm -> !bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate()));

        if (hasOverdue) {
            System.out.println("Cannot borrow: You have overdue media.");
            return false;
        }

        if (user.getFineBalance() > 0) {
            System.out.println("Cannot borrow: You have unpaid fines.");
            return false;
        }

        BorrowedMedia borrowedMedia = new BorrowedMedia(media);
        user.getBorrowedMedia().add(borrowedMedia);
        media.setAvailable(false);

        System.out.println(user.getName() + " borrowed: " + media.getTitle()
                + ", due date: " + borrowedMedia.getDueDate());
        return true;
    }

    public void checkOverdueMedia(LibraryUser user) {
        for (BorrowedMedia bm : user.getBorrowedMedia()) {
            if (!bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate())) {
                if (!bm.isFineAdded()) {
                    user.addFine(bm.getMedia().getFineAmount());
                    bm.setFineAdded(true);
                }
                System.out.println("Overdue: " + bm.getMedia().getTitle() +
                        " | Due: " + bm.getDueDate());
            }
        }
    }

    public void payFine(LibraryUser user, double amount) {
        user.payFine(amount);
        System.out.println(user.getName() + " paid fine: " + amount + " NIS");
    }

    public void sendReminder(LibraryUser user) {
        long overdueCount = user.getBorrowedMedia().stream()
                .filter(bm -> !bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate()))
                .count();

        if (overdueCount > 0) {
            String subject = "Overdue Reminder";
            String message = "Dear " + user.getName() + ",\n\n" +
                    "You have " + overdueCount + " overdue item(s). Please return them soon.\n" +
                    "Library Team";
            emailService.sendEmail(user.getName(), subject, message);
        }
    }

    public boolean unregisterUser(Admin admin, LibraryUser user) {
        if (!admin.isLoggedIn()) {
            System.out.println("Only logged-in admins can unregister users.");
            return false;
        }

        boolean hasActiveLoans = user.getBorrowedMedia().stream()
                .anyMatch(bm -> !bm.isReturned());

        if (hasActiveLoans) {
            System.out.println("Cannot unregister: User has active loans.");
            return false;
        }

        if (user.getFineBalance() > 0) {
            System.out.println("Cannot unregister: User has unpaid fines.");
            return false;
        }

        users.remove(user);
        System.out.println("User unregistered successfully: " + user.getName());
        return true;
    }

    public void addUser(LibraryUser user) {
        users.add(user);
        System.out.println("User added: " + user.getName());
    }

    public List<LibraryUser> getUsers() {
        return new ArrayList<>(users);
    }
}