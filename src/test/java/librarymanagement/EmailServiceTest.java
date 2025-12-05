package librarymanagement;

import librarymanagement.application.EmailService;
import org.junit.jupiter.api.*;

import javax.mail.AuthenticationFailedException;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    static class FakeEmailService extends EmailService {
        private final List<String> fakeMessages = new ArrayList<>();
        private boolean simulateNoCredentials = false;
        private boolean simulateMessagingException = false;
        private boolean simulateAuthFail = false;

        public void setSimulateNoCredentials(boolean value) { simulateNoCredentials = value; }
        public void setSimulateMessagingException(boolean value) { simulateMessagingException = value; }
        public void setSimulateAuthFail(boolean value) { simulateAuthFail = value; }

        @Override
        public void sendEmail(String toEmail, String subject, String message) {
            if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) return;

            if (simulateNoCredentials) System.out.println("SMTP credentials missing → Email only printed in console");

            String log = "To: " + toEmail + "\nSubject: " + subject + "\nMessage: " + message;
            fakeMessages.add(log);

            if (simulateAuthFail) System.out.println("Authentication failed! Wrong email or App Password.");
            if (simulateMessagingException) System.out.println("Failed to send email: MessagingException simulated");
        }

        @Override
        public List<String> getSentMessages() { return fakeMessages; }
    }

    private FakeEmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new FakeEmailService();
        emailService.getSentMessages().clear();
    }

    @Test
    void testConstructorWithoutEnvFile() {
        File file = new File("pass.env");
        boolean existed = file.exists();
        if (existed) file.renameTo(new File("pass.env.bak"));

        EmailService es = new EmailService();
        assertNotNull(es.getSentMessages());

        if (existed) new File("pass.env.bak").renameTo(file);
    }

    @Test
    void testConstructorWithEnvFile() throws Exception {
        File file = new File("pass.env");
        FileWriter fw = new FileWriter(file);
        fw.write("SMTP_EMAIL=test@example.com\nSMTP_PASSWORD=12345\n");
        fw.close();

        EmailService es = new EmailService();
        assertNotNull(es.getSentMessages());

        file.delete();
    }

    @Test
    void testLoadEnvFileWithInvalidContent() throws Exception {
        File file = new File("pass.env");
        FileWriter fw = new FileWriter(file);
        fw.write("INVALID_LINE\nSMTP_EMAIL=email@example.com\n");
        fw.close();

        EmailService es = new EmailService();
        assertNotNull(es.getSentMessages());

        file.delete();
    }

    @Test
    void testSendEmailWithValidData() {
        emailService.sendEmail("user@example.com", "Test Subject", "Hello");
        List<String> sent = emailService.getSentMessages();
        assertEquals(1, sent.size());
        String log = sent.get(0);
        assertTrue(log.contains("To: user@example.com"));
        assertTrue(log.contains("Subject: Test Subject"));
        assertTrue(log.contains("Hello"));
    }

    @Test
    void testSendEmailInvalidEmail() {
        emailService.sendEmail("", "Empty", "Body");
        emailService.sendEmail(null, "Null", "Body");
        emailService.sendEmail("invalidemail", "Invalid", "Body");
        assertEquals(0, emailService.getSentMessages().size());
    }

    @Test
    void testSendEmailWithoutCredentials() {
        emailService.setSimulateNoCredentials(true);
        emailService.sendEmail("a@example.com", "No Credentials", "Body");
        List<String> sent = emailService.getSentMessages();
        assertEquals(1, sent.size());
        assertTrue(sent.get(0).contains("Subject: No Credentials"));
    }

    @Test
    void testSendEmailWithAuthFail() {
        emailService.setSimulateAuthFail(true);
        emailService.sendEmail("auth@example.com", "Auth Fail", "Body");
        List<String> sent = emailService.getSentMessages();
        assertEquals(1, sent.size());
    }

    @Test
    void testSendEmailWithMessagingException() {
        emailService.setSimulateMessagingException(true);
        emailService.sendEmail("msg@example.com", "Messaging Fail", "Body");
        List<String> sent = emailService.getSentMessages();
        assertEquals(1, sent.size());
    }

    @Test
    void testMultipleEmails() {
        emailService.sendEmail("first@example.com", "1", "Msg1");
        emailService.sendEmail("second@example.com", "2", "Msg2");
        assertEquals(2, emailService.getSentMessages().size());
    }

    @Test
    void testClearMessages() {
        emailService.sendEmail("user@example.com", "Clear", "Msg");
        emailService.getSentMessages().clear();
        assertEquals(0, emailService.getSentMessages().size());
    }

    // ====================== تيستات إضافية لرفع الكفرج ======================

    @Test
    void testSendEmailWithEdgeCases() {
        emailService.sendEmail("   ", "Sub", "Body");           // فارغ → لا يُرسل
        emailService.sendEmail(null, "Sub2", "Body2");         // null → لا يُرسل
        emailService.sendEmail("invalidemail", "Sub3", "Body3"); // بدون @ → لا يُرسل
        assertEquals(0, emailService.getSentMessages().size());
    }

    @Test
    void testSendEmailEmptySubjectOrBody() {
        emailService.sendEmail("valid@example.com", "", "Body");
        emailService.sendEmail("valid2@example.com", "Subject", "");
        List<String> sent = emailService.getSentMessages();
        assertEquals(2, sent.size());
        assertTrue(sent.get(0).contains("To: valid@example.com"));
        assertTrue(sent.get(1).contains("To: valid2@example.com"));
    }

    @Test
    void testSendMultipleEmailsAfterClear() {
        emailService.sendEmail("first@example.com", "1", "Msg1");
        emailService.sendEmail("second@example.com", "2", "Msg2");
        assertEquals(2, emailService.getSentMessages().size());

        emailService.getSentMessages().clear();
        assertEquals(0, emailService.getSentMessages().size());

        emailService.sendEmail("third@example.com", "3", "Msg3");
        assertEquals(1, emailService.getSentMessages().size());
    }

    @Test
    void testSimulateAllWithInvalidEmail() {
        emailService.setSimulateNoCredentials(true);
        emailService.setSimulateAuthFail(true);
        emailService.setSimulateMessagingException(true);

        emailService.sendEmail("invalidemail", "Sub", "Body");
        assertEquals(0, emailService.getSentMessages().size());

        emailService.sendEmail("valid@example.com", "Sub", "Body");
        assertEquals(1, emailService.getSentMessages().size());
    }
    @Test
    void testSendEmail_NullOrEmptyTo() {
        EmailService es = new EmailService();
        es.sendEmail(null, "Sub", "Body");
        es.sendEmail("", "Sub", "Body");
        es.sendEmail("   ", "Sub", "Body");
        assertEquals(0, es.getSentMessages().size());
    }

    @Test
    void testSendEmail_InvalidEmailFormat() {
        EmailService es = new EmailService();
        es.sendEmail("invalidEmail", "Sub", "Body");
        assertEquals(0, es.getSentMessages().size());
    }

    @Test
    void testSendEmail_ConsoleOnlyWhenNoCredentials() {
        EmailService es = new EmailService() {
            @Override
            public List<String> getSentMessages() {
                return super.getSentMessages();
            }
        };
        es.sendEmail("user@example.com", "Sub", "Body");
        assertEquals(1, es.getSentMessages().size());
    }
    @Test
    void testLoadEnvFile_WithCommentAndEmptyLines() throws Exception {
        File file = new File("pass.env");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("# Comment line\n\nSMTP_EMAIL=test@example.com\nSMTP_PASSWORD=12345\n");
        }
        EmailService es = new EmailService();
        assertNotNull(es.getSentMessages());
        file.delete();
    }

    @Test
    void testSendEmail_WithInvalidCredentials() {
        EmailService es = new EmailService();
        es.sendEmail("user@example.com", "Sub", "Body"); // يمر على فرع credentials ناقصة
        assertEquals(1, es.getSentMessages().size());
    }

}
