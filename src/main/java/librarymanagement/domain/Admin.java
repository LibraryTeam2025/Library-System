package librarymanagement.domain;

public class Admin {
    private String username;
    private String password;
    private boolean loggedIn = false;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean login(String user, String pass) {
        if (username.equals(user) && password.equals(pass)) {
            loggedIn = true;
            return true;
        }
        return false;
    }

    public void logout() {
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        return username;
    }
}