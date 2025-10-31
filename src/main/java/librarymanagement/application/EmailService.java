package librarymanagement.application;

import java.util.ArrayList;
import java.util.List;

public class EmailService {

    private List<String> sentMessages = new ArrayList<>();

    public void sendEmail(String username, String subject, String message) {
        String fullMessage = String.format(
                "=== EMAIL SENT ===\n" +
                        "To: %s\n" +
                        "Subject: %s\n" +
                        "Message:\n%s\n" +
                        "==================",
                username, subject, message
        );

        sentMessages.add(fullMessage);
        System.out.println(fullMessage);
    }

    public void sendEmail(String username, String message) {
        sendEmail(username, "Library Notification", message);
    }

    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages); // نسخة آمنة
    }

    public int getSentCount() {
        return sentMessages.size();
    }
    public void clearSentMessages() {
        sentMessages.clear();
        System.out.println("Sent messages cleared.");
    }
}