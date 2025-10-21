package librarymanagement;

import librarymanagement.domain.Admin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    @Test
    void testLoginSuccess() {
        Admin admin = new Admin("soft", "123");
        assertTrue(admin.login("soft", "123"));
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testLoginFailure() {
        Admin admin = new Admin("soft", "123");
        assertFalse(admin.login("wrong", "123"));
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testLogout() {
        Admin admin = new Admin("soft", "123");
        admin.login("soft", "123");
        admin.logout();
        assertFalse(admin.isLoggedIn());
    }


}
