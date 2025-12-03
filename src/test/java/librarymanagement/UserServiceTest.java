package librarymanagement;

import librarymanagement.domain.UserService;
import librarymanagement.domain.LibraryUser;
import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private static final String USERS_FILE = "test_users.txt";
    private static final String BORROWED_FILE = "test_borrowed.txt";
    private UserService service;

    @BeforeEach
    void setup() throws IOException {
        new File(USERS_FILE).delete();
        new File(BORROWED_FILE).delete();

        try (PrintWriter pw = new PrintWriter(USERS_FILE)) {
            pw.println("Roa:123:roa@mail.com");
            pw.println("Yaman:456:yaman@mail.com");
        }

        service = new UserService(USERS_FILE, BORROWED_FILE);
    }

    @AfterEach
    void cleanup() {
        new File(USERS_FILE).delete();
        new File(BORROWED_FILE).delete();
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
        assertNull(service.login("NonExistent", "123"));
    }

    @Test
    void testAddUserSuccess() {
        assertTrue(service.addUser("Ali", "789", "ali@mail.com"));
        LibraryUser user = service.login("Ali", "789");
        assertNotNull(user);
        assertEquals("Ali", user.getName());
    }

    @Test
    void testAddUserDuplicate() {
        service.addUser("Dup", "111", "dup@mail.com");
        assertFalse(service.addUser("Dup", "222", "dup2@mail.com"));
    }

    @Test
    void testRemoveUser() {
        service.addUser("Temp", "temp", "temp@mail.com");
        assertTrue(service.removeUser("Temp"));
        assertNull(service.getUserByName("Temp"));
    }

    @Test
    void testGetUsersReturnsCopy() {
        service.getUsers().clear();
        assertEquals(2, service.getUsers().size());
    }
}
