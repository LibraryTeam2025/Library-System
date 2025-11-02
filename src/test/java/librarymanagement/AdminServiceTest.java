
    package librarymanagement;

    import librarymanagement.domain.AdminService;
    import librarymanagement.domain.Admin;
    import org.junit.jupiter.api.*;
    import java.io.*;
    import static org.junit.jupiter.api.Assertions.*;
    public class
    AdminServiceTest {

        private static final String FILE = "test_admins.txt";
        private AdminService service;

        @BeforeEach
        void setup() throws IOException {
            new File(FILE).delete();
            try (PrintWriter pw = new PrintWriter(FILE)) {
                pw.println("admin1,pass1");
                pw.println("admin2,pass2");
            }
            service = new AdminService(FILE);
        }

        @AfterEach
        void cleanup() {
            new File(FILE).delete();
        }

        @Test
        void testLoginSuccess() {
            Admin admin = service.login("admin1", "pass1");
            assertNotNull(admin);
            assertTrue(admin.isLoggedIn());
        }

        @Test
        void testAddAdminSuccess() {
            assertTrue(service.addAdmin("newadmin", "newpass"));
            assertNotNull(service.login("newadmin", "newpass"));
        }

        @Test
        void testAddAdminDuplicate() {
            service.addAdmin("dup", "123");
            assertFalse(service.addAdmin("dup", "456"));
        }

        @Test
        void testGetAdminsUnmodifiable() {
            assertThrows(UnsupportedOperationException.class, () -> {
                service.getAdmins().add(new Admin("x", "y"));
            });
        }
    }

