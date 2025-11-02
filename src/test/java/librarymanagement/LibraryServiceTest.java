package librarymanagement;

import librarymanagement.application.*;
import librarymanagement.domain.*;
import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {

    private LibraryService service;
    private EmailService emailService;
    private UserService userService;

    private static final String TEST_USERS_FILE = "test_users.txt";
    private static final String BOOKS_FILE = "books.txt";
    private static final String CDS_FILE = "cds.txt";

    @BeforeEach
    void setUp() throws Exception {
        // حذف كل الملفات قبل كل اختبار
        deleteFile(TEST_USERS_FILE);
        deleteFile(BOOKS_FILE);
        deleteFile(CDS_FILE);

        emailService = new EmailService();
        userService = new UserService(TEST_USERS_FILE);
        service = new LibraryService(emailService, userService);
    }

    @AfterEach
    void tearDown() {
        deleteFile(TEST_USERS_FILE);
        deleteFile(BOOKS_FILE);
        deleteFile(CDS_FILE);
    }

    private void deleteFile(String path) {
        try { Files.deleteIfExists(new File(path).toPath()); }
        catch (Exception e) { /* ignore */ }
    }

    @Test
    void testAddAndSearchBook() {
        Book book = new Book("Java", "Yaman", "111");
        assertTrue(service.addMedia(book), "يجب أن تُضاف الوسيط بنجاح");
        assertFalse(service.addMedia(book), "لا يمكن إضافة وسيط مكرر");

        List<Media> results = service.searchMedia("java");
        assertEquals(1, results.size(), "يجب أن يُرجع نتيجة واحدة");
        assertEquals("Java", results.get(0).getTitle());

        results = service.searchMedia("111");
        assertEquals(1, results.size(), "البحث بالـ ID يجب أن يعمل");
    }

    @Test
    void testSearchExactIdPriority() {
        service.addMedia(new Book("Python", "G", "python"));
        service.addMedia(new Book("Java Book", "J", "java"));

        List<Media> results = service.searchMedia("java");
        assertEquals("java", results.get(0).getId(), "الـ ID يجب أن يكون الأولوية");
        assertEquals("Java Book", results.get(0).getTitle());
    }

    @Test
    void testSearchNoResults() {
        assertTrue(service.searchMedia("xyz").isEmpty(), "البحث عن شيء غير موجود يرجع فارغ");
    }

    @Test
    void testGetAllMedia() {
        assertTrue(service.getAllMedia().isEmpty(), "القائمة يجب أن تكون فارغة في البداية");

        service.addMedia(new Book("A", "B", "1"));
        assertEquals(1, service.getAllMedia().size(), "بعد الإضافة يجب أن يكون هناك عنصر واحد");
    }

    @Test
    void testBorrowCD() {
        LibraryUser user = new LibraryUser("Roa");
        userService.addUser("Roa", ""); // أضف المستخدم
        service.addUser(user);

        CD cd = new CD("Hits", "Art", "CD1");
        service.addMedia(cd);

        assertTrue(service.borrowMedia(user, cd), "يجب أن يتم الاستعارة");

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        assertEquals(LocalDate.now().plusDays(7), bm.getDueDate(), "CD: 7 أيام");
        assertFalse(cd.isAvailable(), "الـ CD يجب أن لا يكون متاحًا");
    }

    @Test
    void testReturnMedia() {
        LibraryUser user = new LibraryUser("Roa");
        service.addUser(user);

        Book book = new Book("X", "Y", "1");
        service.addMedia(book);
        service.borrowMedia(user, book);

        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        bm.returnMedia(); // استخدم returnMedia()

        assertTrue(book.isAvailable(), "الكتاب يجب أن يعود متاحًا");
    }

    @Test
    void testCannotBorrowWithFine() {
        LibraryUser user = new LibraryUser("Roa");
        service.addUser(user);
        user.addFine(10.0); // أضف غرامة

        Book book = new Book("Java", "Y", "1");
        service.addMedia(book);

        assertFalse(service.borrowMedia(user, book), "لا يمكن الاستعارة مع غرامة");
    }

    @Test
    void testPayFine() {
        LibraryUser user = new LibraryUser("Roa");
        service.addUser(user);
        user.addFine(25.0);

        service.payFine(user, 15.0);
        assertEquals(10.0, user.getFineBalance(), 0.01);

        service.payFine(user, 10.0);
        assertEquals(0.0, user.getFineBalance(), 0.01);
    }

    @Test
    void testSendReminder() throws IllegalAccessException, NoSuchFieldException {
        LibraryUser user = new LibraryUser("Roa");
        service.addUser(user);

        Book book = new Book("Old", "X", "1");
        service.addMedia(book);
        service.borrowMedia(user, book);

        // اجعل التاريخ متأخرًا (نحتاج تعديل DueDate للاختبار)
        BorrowedMedia bm = user.getBorrowedMedia().get(0);
        java.lang.reflect.Field dueDateField = BorrowedMedia.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(bm, LocalDate.now().minusDays(1)); // متأخر يوم

        service.sendReminder(user);

        // تحقق من إرسال الإيميل
        assertTrue(emailService.getSentMessages().stream()
                .anyMatch(m -> m.contains("overdue")));
    }
}