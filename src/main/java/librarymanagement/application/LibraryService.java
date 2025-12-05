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
        for (LibraryUser user : users) checkOverdueMedia(user);
        saveFines();
    }

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

    public boolean addMedia(Media media) {
        if (media == null || media.getId() == null || media.getTitle() == null || media.getAuthor() == null) return false;

        boolean exists = mediaList.stream().anyMatch(m -> m.getId().equalsIgnoreCase(media.getId()));
        if (exists) return false;

        mediaList.add(media);
        saveMediaToFile(media);
        return true;
    }

    public List<Media> getAvailableMedia() {
        List<Media> available = new ArrayList<>();
        for (Media m : mediaList) if (m.getAvailableCopies() > 0) available.add(m);
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
        if (!media.borrowCopy()) return false;

        checkOverdueMedia(user);

        if (user.isBlocked() || user.getFineBalance() > 0) {
            media.returnCopy();
            return false;
        }

        BorrowedMedia borrowed = new BorrowedMedia(media);
        user.getBorrowedMediaInternal().add(borrowed);

        userService.saveBorrowedMedia();
        saveFines();
        saveAllMedia();

        return true;
    }

    public void returnMedia(LibraryUser user, BorrowedMedia borrowed) {
        borrowed.returnMedia();
        borrowed.getMedia().returnCopy();

        checkOverdueMedia(user);
        if (user.getFineBalance() == 0 && !user.hasOverdueItems()) {
            user.setBlocked(false);
        }

        saveAllMedia();
    }

    private void saveAllMedia() {
        try (PrintWriter pwBooks = new PrintWriter(new FileWriter(BOOKS_FILE, false))) {
            for (Media m : mediaList) {
                if (m instanceof Book) {
                    pwBooks.println(m.getId() + "|" + m.getTitle() + "|" + m.getAuthor() + "|" + m.getTotalCopies() + "|" + m.getAvailableCopies());
                }
            }
        } catch (IOException ignored) {}

        try (PrintWriter pwCDs = new PrintWriter(new FileWriter(CDS_FILE, false))) {
            for (Media m : mediaList) {
                if (m instanceof CD) {
                    pwCDs.println(m.getId() + "|" + m.getTitle() + "|" + m.getAuthor() + "|" + m.getTotalCopies() + "|" + m.getAvailableCopies());
                }
            }
        } catch (IOException ignored) {}
    }

    public void checkOverdueMedia(LibraryUser user) {
        double newFines = 0;
        for (BorrowedMedia bm : user.getBorrowedMediaInternal()) {
            if (!bm.isReturned() && !bm.isFineAdded() && bm.isOverdue()) {
                newFines += bm.calculateFine();
                bm.setFineAdded(true);
            }
        }
        if (newFines > 0) user.addFine(newFines);
        user.setBlocked(user.getFineBalance() > 0 || user.hasOverdueItems());
    }

    public void payFine(LibraryUser user, double amount) {
        if (user == null || amount <= 0) return;
        double payment = Math.min(amount, user.getFineBalance());
        user.setFineBalance(user.getFineBalance() - payment);

        for (BorrowedMedia bm : user.getBorrowedMediaInternal()) {
            if (!bm.isReturned() && bm.isOverdue()) bm.setFineAdded(true);
        }

        boolean hasOverdue = user.getBorrowedMedia().stream().anyMatch(BorrowedMedia::isOverdue);
        user.setBlocked(user.getFineBalance() > 0 || hasOverdue);

        userService.saveUsers();
        userService.saveBorrowedMedia();
        saveFines();
    }

    private void loadFines() {
        File file = new File(FINES_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 2) continue;
                LibraryUser user = getUserByName(parts[0]);
                if (user != null) {
                    user.setFineBalance(Double.parseDouble(parts[1]));
                    user.setBlocked(user.getFineBalance() > 0 || user.hasOverdueItems());
                }
            }
        } catch (IOException ignored) {}
    }

    public void saveFines() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FINES_FILE))) {
            for (LibraryUser user : users) pw.println(user.getName() + "|" + user.getFineBalance());
        } catch (IOException ignored) {}
    }

    private void saveMediaToFile(Media media) {
        String fileName = media instanceof Book ? BOOKS_FILE : CDS_FILE;
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, false))) {
            for (Media m : mediaList) {
                if ((media instanceof Book && m instanceof Book) || (media instanceof CD && m instanceof CD)) {
                    pw.println(m.getId() + "|" + m.getTitle() + "|" + m.getAuthor() + "|" + m.getTotalCopies() + "|" + m.getAvailableCopies());
                }
            }
        } catch (IOException ignored) {}
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

                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                int totalCopies = Integer.parseInt(parts[3]);
                int availableCopies = parts.length >= 5 ? Integer.parseInt(parts[4]) : totalCopies;

                Media media;
                if (type.equals("Book")) {
                    media = new Book(parts[1], parts[2], parts[0], totalCopies);
                } else {
                    media = new CD(parts[1], parts[2], parts[0], totalCopies);
                }

                media.setAvailableCopies(availableCopies);
                mediaList.add(media);
            }
        } catch (IOException ignored) {}
    }



    public void sendReminder(LibraryUser user) {
        long count = user.getBorrowedMedia().stream()
                .filter(bm -> !bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate()))
                .count();

        if (count > 0 && !user.getEmail().trim().isEmpty()) {
            String subject = "Important Reminder: You have overdue library items";
            String message = "Dear " + user.getName() + ",\n\nYou have " + count +
                    " overdue item(s).\nPlease return them as soon as possible.\n\nLibrary Team";
            emailService.sendEmail(user.getEmail(), subject, message);
        }
    }
}
