package librarymanagement;

import librarymanagement.application.EmailService;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new EmailService();
        emailService.clearSentMessages();
    }

    @Test
    void testSendEmailWithSubject() {
        emailService.sendEmail("Roa", "Test Subject", "Hello");
        List<String> sent = emailService.getSentMessages();
        assertEquals(1, sent.size());
        assertTrue(sent.get(0).contains("To: Roa"));
        assertTrue(sent.get(0).contains("Subject: Test Subject"));
        assertTrue(sent.get(0).contains("Hello"));
    }

    @Test
    void testSendEmailDefaultSubject() {
        emailService.sendEmail("Roa", "Hello");
        List<String> sent = emailService.getSentMessages();
        assertEquals(1, sent.size());
        assertTrue(sent.get(0).contains("Subject: Library Notification"));
    }

    @Test
    void testGetSentCount() {
        assertEquals(0, emailService.getSentCount());
        emailService.sendEmail("A", "B");
        assertEquals(1, emailService.getSentCount());
    }

    @Test
    void testClearSentMessages() {
        emailService.sendEmail("A", "B");
        emailService.clearSentMessages();
        assertEquals(0, emailService.getSentCount());
    }
}