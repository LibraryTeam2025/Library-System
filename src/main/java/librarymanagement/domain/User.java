// User.java
package librarymanagement.domain;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private List<Loan> loans = new ArrayList<>();
    private List<Fine> fines = new ArrayList<>();

    public User(String username) { this.username = username; }

    public String getUsername() { return username; }
    public List<Loan> getLoans() { return loans; }
    public List<Fine> getFines() { return fines; }

    public boolean hasOverdue() {
        return loans.stream().anyMatch(Loan::isOverdue);
    }

    public boolean hasUnpaidFines() {
        return fines.stream().anyMatch(f -> !f.isPaid());
    }
}
