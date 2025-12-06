package librarymanagement;

import librarymanagement.domain.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceTest {

    private static final String TEST_FILE = "test_admins.txt";
    private AdminService service;

    @BeforeEach
    void setup() throws IOException {
        new File(TEST_FILE).delete();

        try (PrintWriter pw = new PrintWriter(new FileWriter(TEST_FILE))) {
            pw.println("admin1|admin1@mail.com|pass1|OWNER");
            pw.println("admin2|admin2@mail.com|pass2|SMALL_ADMIN");
        }

        service = new AdminService(TEST_FILE);
    }

    @AfterEach
    void cleanup() {
        new File(TEST_FILE).delete();
    }

    @Test
    void testLoginSuccess() {
        Admin admin = service.login("admin1@mail.com", "pass1");
        assertNotNull(admin);
        assertTrue(admin.isLoggedIn());

        Admin admin2 = service.login("admin2", "pass2");
        assertNotNull(admin2);
        assertTrue(admin2.isLoggedIn());
    }

    @Test
    void testLoginFailure() {
        assertNull(service.login("admin1@mail.com", "wrongpass"));
        assertNull(service.login("nonexistent@mail.com", "pass1"));
        assertNull(service.login("fakeuser", "pass1"));
    }

    @Test
    void testAddSuperAdminSuccess() {
        boolean added = service.addSuperAdmin("superowner", "super@mail.com", "owner123");
        assertTrue(added);

        Admin admin = service.login("super@mail.com", "owner123");
        assertNotNull(admin);
        assertTrue(admin.isOwner());
        assertTrue(admin.isLoggedIn());
    }

    @Test
    void testAddSmallAdminSuccess() {
        boolean added = service.addSmallAdmin("helper", "helper@mail.com", "help321");
        assertTrue(added);

        Admin admin = service.login("helper@mail.com", "help321");
        assertNotNull(admin);
        assertTrue(admin.isSmallAdmin());
    }

    @Test
    void testAddAdminDuplicate() {
        assertTrue(service.addSuperAdmin("first", "dup@mail.com", "pass123"));

        assertFalse(service.addSmallAdmin("second", "dup@mail.com", "otherpass"));
        assertFalse(service.addSuperAdmin("third", "dup@mail.com", "again"));

        long count = service.getAdmins().stream()
                .filter(a -> a.getEmail().equalsIgnoreCase("dup@mail.com"))
                .count();
        assertEquals(1, count);
    }

    @Test
    void testGetAdminsReturnsIndependentCopy() {
        var originalList = service.getAdmins();
        assertEquals(2, originalList.size());
        service.addSuperAdmin("temp", "temp@mail.com", "temp");
        assertEquals(2, originalList.size());
        assertEquals(3, service.getAdmins().size());
        assertTrue(originalList.stream().anyMatch(a -> a.getEmail().equals("admin1@mail.com")));
    }
    @Test
    void testConstructor_FileDoesNotExist_LoadsEmptyList() {
        new File(TEST_FILE).delete();
        AdminService service = new AdminService(TEST_FILE);
        assertTrue(service.getAdmins().isEmpty());
    }

    @Test
    void testLoadFromFile_CorruptedLine_IsIgnored() throws IOException {
        try (PrintWriter pw = new PrintWriter(TEST_FILE)) {
            pw.println("valid|valid@mail.com|pass|OWNER");
            pw.println("invalid-line-without-pipes");
            pw.println("another|another@mail.com|pass|SMALL_ADMIN");
        }

        AdminService service = new AdminService(TEST_FILE);
        assertEquals(2, service.getAdmins().size());
    }

    @Test
    void testLoadFromFile_IOException_IsHandledSilently() throws IOException {
        File unreadable = new File(TEST_FILE);
        unreadable.createNewFile();
        unreadable.setReadable(false);
        assertDoesNotThrow(() -> new AdminService(TEST_FILE));

        unreadable.setReadable(true);
        unreadable.delete();
    }

    @Test
    void testSaveToFile_IOException_IsHandledSilently() throws IOException {
        File file = new File(TEST_FILE);
        file.createNewFile();
        file.setWritable(false);

        AdminService service = new AdminService(TEST_FILE);
        assertDoesNotThrow(() -> service.addSuperAdmin("test", "test@mail.com", "pass"));

        file.setWritable(true);
        file.delete();
    }

    @Test
    void testGetAdmins_ReturnsIndependentCopy_NotAffectedByExternalModifications() {
        List<Admin> externalList = service.getAdmins();
        assertEquals(2, externalList.size());

        externalList.clear();

        assertEquals(2, service.getAdmins().size());
    }
}