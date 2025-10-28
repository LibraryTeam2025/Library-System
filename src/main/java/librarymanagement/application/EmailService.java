package librarymanagement.application;

import java.util.ArrayList;
import java.util.List;

public class EmailService {

    private List<String> sentMessages = new ArrayList<>();

    public void sendEmail(String username, String message) {
        String fullMessage = "To: " + username + " | Message: " + message;
        sentMessages.add(fullMessage);
        System.out.println(fullMessage);
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }
}