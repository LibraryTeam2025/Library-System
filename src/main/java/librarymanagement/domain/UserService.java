package librarymanagement.domain;

import java.io.*;
import java.util.*;

public class UserService {
    private List<LibraryUser> users = new ArrayList<>();
    private String filename;

    public UserService(String filename) {
        this.filename = filename;
        loadUsers();
    }

    public boolean addUser(String name, String password) {
        if (users.stream().anyMatch(u -> u.getName().equalsIgnoreCase(name))) {
            return false;
        }
        LibraryUser newUser = new LibraryUser(name, password);
        users.add(newUser);
        return saveUsers();
    }

    public LibraryUser login(String name, String password) {
        return users.stream()
                .filter(u -> u.getName().equalsIgnoreCase(name) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public LibraryUser getUserByName(String name) {
        return users.stream()
                .filter(u -> u.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean removeUser(String name) {
        return users.removeIf(u -> u.getName().equalsIgnoreCase(name)) && saveUsers();
    }

    public List<LibraryUser> getUsers() {
        return new ArrayList<>(users);
    }

    private void loadUsers() {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("No users file found. Starting with empty list.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    users.add(new LibraryUser(parts[0], parts[1]));
                } else {
                    users.add(new LibraryUser(parts[0], ""));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }

    private boolean saveUsers() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (LibraryUser u : users) {
                pw.println(u.getName() + ":" + u.getPassword());
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
            return false;
        }
    }
}