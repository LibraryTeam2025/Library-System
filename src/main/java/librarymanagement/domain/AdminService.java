package librarymanagement.domain;


import librarymanagement.domain.Admin.Role;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {
    private final List<Admin> admins = new ArrayList<>();
    private final String filename;

    public AdminService(String filename) {
        this.filename = filename;
        loadFromFile();
    }

    public boolean addSuperAdmin(String name, String email, String password) {
        if (getAdminByEmail(email) != null) return false;
        admins.add(new Admin(name, email, password, Role.OWNER));
        saveToFile();
        return true;
    }

    public boolean addSmallAdmin(String name, String email, String password) {
        if (getAdminByEmail(email) != null) return false;
        admins.add(new Admin(name, email, password, Role.SMALL_ADMIN));
        saveToFile();
        return true;
    }

    public Admin login(String identifier, String password) {
        for (Admin admin : admins) {
            if (admin.login(identifier, password)) {
                return admin;
            }
        }
        return null;
    }

    public List<Admin> getAdmins() {
        return new ArrayList<>(admins);
    }

    public Admin getAdminByEmail(String email) {
        return admins.stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    private void loadFromFile() {
        if (!new File(filename).exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 4) {
                    Role role = p[3].equals("OWNER") ? Role.OWNER : Role.SMALL_ADMIN;
                    admins.add(new Admin(p[0], p[1], p[2], role));
                }
            }
        } catch (Exception ignored) {}
    }

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (Admin a : admins) {
                pw.println(a.getName() + "|" + a.getEmail() + "|" + a.getPassword() + "|" + a.getRole());
            }
        } catch (Exception ignored) {}
    }
}