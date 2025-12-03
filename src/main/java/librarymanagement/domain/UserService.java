package librarymanagement.domain;

import librarymanagement.application.LibraryService;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class UserService {
    private static UserService instance;
    private final List<LibraryUser> users = new ArrayList<>();
    private final String usersFile;
    private final String borrowedFile;
    private LibraryService libraryService;

    public UserService(String usersFile, String borrowedFile) {
        this.usersFile = usersFile;
        this.borrowedFile = borrowedFile;
        instance = this;
        loadUsers();
    }

    public static UserService getInstance() { return instance; }

    public void setLibraryService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public boolean addUser(String name, String password, String email) {
        if (users.stream().anyMatch(u -> u.getName().equalsIgnoreCase(name))) return false;
        LibraryUser newUser = new LibraryUser(name, password, email);
        users.add(newUser);
        saveUsers();
        saveBorrowedMedia();
        return true;
    }
    public LibraryUser login(String name, String password) {
        LibraryUser user = users.stream()
                .filter(u -> u.getName().equalsIgnoreCase(name) && u.getPassword().equals(password))
                .findFirst().orElse(null);

        if (user != null && libraryService != null) {
            // Recalculate overdue fines as soon as user logs in
            libraryService.checkOverdueMedia(user);
        }

        return user;
    }

    public LibraryUser getUserByName(String name) {
        return users.stream()
                .filter(u -> u.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public boolean removeUser(String name) {
        boolean removed = users.removeIf(u -> u.getName().equalsIgnoreCase(name));
        saveUsers();
        saveBorrowedMedia();
        return removed;
    }

    public List<LibraryUser> getUsers() {
        return new ArrayList<>(users);
    }

    private void loadUsers() {
        File file = new File(usersFile);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(":", 5);
                if (p.length < 2) continue;
                String name = p[0];
                String pass = p[1];
                String email = p.length >= 3 ? p[2] : "";
                double fine = p.length >= 4 ? Double.parseDouble(p[3]) : 0.0;
                boolean blocked = p.length >= 5 && Boolean.parseBoolean(p[4]);

                LibraryUser user = new LibraryUser(name, pass, email);
                user.setFineBalance(fine);
                user.setBlocked(blocked);
                users.add(user);
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public void saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile))) {
            for (LibraryUser u : users) {
                pw.println(u.getName() + ":" + u.getPassword() + ":" + u.getEmail() + ":" +
                        u.getFineBalance() + ":" + u.isBlocked());
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    public void loadBorrowedMedia() {
        if (libraryService == null) return;
        for (LibraryUser user : users) {
            user.getBorrowedMediaInternal().clear();
        }
        File file = new File(borrowedFile);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 5) continue;
                String username = parts[0];
                String mediaId = parts[1];
                LocalDate borrowDate = LocalDate.parse(parts[2]);
                LocalDate dueDate = LocalDate.parse(parts[3]);
                boolean returned = Boolean.parseBoolean(parts[4]);
                double fine = parts.length >= 6 ? Double.parseDouble(parts[5]) : 0.0;

                LibraryUser user = getUserByName(username);
                Media media = libraryService.getMediaById(mediaId);
                if (user != null && media != null) {
                    BorrowedMedia bm = new BorrowedMedia(media, borrowDate, dueDate);
                    if (returned) {
                        bm.returnMedia();
                        media.setAvailable(true);
                    } else {
                        media.setAvailable(false);
                    }
                    bm.setFine(fine);
                    if (fine > 0) bm.setFineAdded(true);
                    user.getBorrowedMediaInternal().add(bm);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading borrowed media: " + e.getMessage());
        }

        for (LibraryUser u : users) {
            libraryService.checkOverdueMedia(u);
        }
        saveBorrowedMedia();
    }

    public void saveBorrowedMedia() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(borrowedFile))) {
            for (LibraryUser user : users) {
                for (BorrowedMedia bm : user.getBorrowedMedia()) {
                    pw.println(user.getName() + "|" +
                            bm.getMedia().getId() + "|" +
                            bm.getBorrowDate() + "|" +
                            bm.getDueDate() + "|" +
                            bm.isReturned() + "|" +
                            bm.getFine());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving borrowed media: " + e.getMessage());
        }
    }
}