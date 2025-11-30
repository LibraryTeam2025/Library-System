package librarymanagement.domain;

public class Admin extends Person {

    public enum Role { OWNER, SMALL_ADMIN }

    private final Role role;
    private boolean loggedIn = false;

    public Admin(String name, String email, String password, Role role) {
        super(name, email, password);
        this.role = role;
    }

    public boolean login(String identifier, String password) {
        boolean match = (email.equalsIgnoreCase(identifier) || name.equalsIgnoreCase(identifier))
                && this.password.equals(password);
        if (match) loggedIn = true;
        return match;
    }

    public void logout() {
        loggedIn = false;
    }

    public boolean isLoggedIn() { return loggedIn; }

    public Role getRole() { return role; }
    public boolean isOwner() { return role == Role.OWNER; }
    public boolean isSmallAdmin() { return role == Role.SMALL_ADMIN; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}