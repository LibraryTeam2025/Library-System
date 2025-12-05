package librarymanagement.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailService {
    private final List<String> sentMessages = new ArrayList<>();
    private final String fromEmail;
    private final String appPassword;

    public EmailService() {
        Map<String, String> env = loadEnvFromFile();
        this.fromEmail   = env.getOrDefault("SMTP_EMAIL", System.getenv("SMTP_EMAIL"));
        this.appPassword = env.getOrDefault("SMTP_PASSWORD", System.getenv("SMTP_PASSWORD"));

        if (fromEmail == null || appPassword == null || fromEmail.isEmpty() || appPassword.isEmpty()) {
            System.out.println("Warning: Email data not found! Emails will be printed to the console only.");

        }
    }

    private Map<String, String> loadEnvFromFile() {
        Map<String, String> map = new HashMap<>();
        File file = new File("pass.env");
        if (!file.exists()) {
            System.out.println("pass.env file not found in the project folder!");
            return map;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim().replaceAll("\"", ""));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to read pass.env file: " + e.getMessage());

        }
        return map;
    }
    private void sendRealEmail(String toEmail, String subject, String message) {
        if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
            System.out.println("Invalid email address: " + toEmail);
            return;
        }

        String log = String.format("=== EMAIL SENT ===\nTo: %s\nSubject: %s\nMessage:\n%s\n==================",
                toEmail, subject, message);
        sentMessages.add(log);
        System.out.println(log);

        if (fromEmail == null || appPassword == null || fromEmail.isEmpty() || appPassword.isEmpty()) {
            System.out.println("SMTP credentials missing → Email only printed in console (not sent)");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(subject);
            msg.setText(message);

            Transport.send(msg);
            System.out.println("SUCCESS: Email actually sent to " + toEmail);

        } catch (AuthenticationFailedException e) {
            System.out.println("Authentication failed! Wrong email or App Password.");
            e.printStackTrace();
        } catch (MessagingException e) {
            System.out.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void sendEmail(String toEmail, String subject, String message) {
        sendRealEmail(toEmail, subject, message);
    }

    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }
}