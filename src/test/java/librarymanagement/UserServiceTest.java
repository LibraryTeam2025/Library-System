package librarymanagement;

import librarymanagement.domain.UserService;
import librarymanagement.domain.LibraryUser;
import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private static final String FILE = "test_users.txt";
    private UserService service;

    @BeforeEach
    void setup() throws IOException {
        new File(FILE).delete();
        try (PrintWriter pw = new PrintWriter(FILE)) {
            pw.println("Roa:123");
            pw.println("Yaman:456");
        }
        service = new UserService(FILE);
    }

    @AfterEach
    void cleanup() {
        new File(FILE).delete();
    }

    @Test
    void testLoginSuccess() {
        LibraryUser user = service.login("Roa", "123");
        assertNotNull(user);
        assertEquals("Roa", user.getName());
    }

    @Test
    void testLoginFailure() {
        assertNull(service.login("Roa", "wrong"));
    }

    @Test
    void testAddUserSuccess() {
        assertTrue(service.addUser("Ali", "789"));
        assertNotNull(service.login("Ali", "789"));
    }

    @Test
    void testAddUserDuplicate() {
        service.addUser("Dup", "111");
        assertFalse(service.addUser("Dup", "222"));
    }

    @Test
    void testRemoveUser() {
        service.addUser("Temp", "temp");
        assertTrue(service.removeUser("Temp"));
        assertNull(service.getUserByName("Temp"));
    }

    @Test
    void testGetUsersReturnsCopy() {
        service.getUsers().clear();
        assertEquals(2, service.getUsers().size()); // لا يؤثر
    }
}