package librarymanagement.domain;

import java.io.*;
import java.util.*;

public class AdminService {
    private List<Admin> admins = new ArrayList<>();
    private final String filename;

    public AdminService(String filename) {
        this.filename = filename;
        loadAdmins(filename);
    }

    private void loadAdmins(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    admins.add(new Admin(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (FileNotFoundException fnf) {

        } catch (IOException e) {
            System.out.println("Error reading admins file: " + e.getMessage());
        }
    }

    public synchronized Admin login(String username, String password) {
        for (Admin admin : admins) {
            if (admin.login(username, password)) {
                return admin;
            }
        }
        return null;
    }

    public List<Admin> getAdmins() {
        return Collections.unmodifiableList(admins);
    }

    public synchronized boolean addAdmin(String username, String password) {
        for (Admin a : admins) {

            if (a != null && aIsSame(a, username)) {
                return false;
            }
        }

        Admin newAdmin = new Admin(username, password);
        admins.add(newAdmin);

        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(username + "," + password);
            out.flush();
            return true;
        } catch (IOException e) {
            admins.remove(newAdmin);
            System.out.println("Error writing to admins file: " + e.getMessage());
            return false;
        }
    }

    private boolean aIsSame(Admin a, String username) {
        try {
            java.lang.reflect.Field f = Admin.class.getDeclaredField("username");
            f.setAccessible(true);
            Object val = f.get(a);
            return username.equals(String.valueOf(val));
        } catch (Exception ex) {
            return false;
        }
    }
}