package librarymanagement;

import librarymanagement.domain.Admin;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    @Test
    void testLoginSuccess() {
        Admin admin = new Admin("admin", "1234");
        assertTrue(admin.login("admin", "1234"));
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testLoginFailure() {
        Admin admin = new Admin("admin", "1234");
        assertFalse(admin.login("wrong", "123"));
        assertFalse(admin.isLoggedIn());
    }

    @Test
    void testLogout() {
        Admin admin = new Admin("admin", "1234");
        admin.login("admin", "1234");
        admin.logout();
        assertFalse(admin.isLoggedIn());
    }


}
