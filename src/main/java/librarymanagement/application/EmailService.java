package librarymanagement.application;

import librarymanagement.domain.LibraryUser;
import librarymanagement.domain.UserService;
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
        // بدل ما يعتمد على System.getenv فقط، نجرب نقرأ من ملف pass.env يدوياً
        Map<String, String> env = loadEnvFromFile();
        this.fromEmail   = env.getOrDefault("SMTP_EMAIL", System.getenv("SMTP_EMAIL"));
        this.appPassword = env.getOrDefault("SMTP_PASSWORD", System.getenv("SMTP_PASSWORD"));

        // لو لسه null نطبع تحذير واضح
        if (fromEmail == null || appPassword == null || fromEmail.isEmpty() || appPassword.isEmpty()) {
            System.out.println("تحذير: بيانات الإيميل غير موجودة! الإيميلات هتطبع في الكونسول فقط.");
        }
    }

    // دالة صغيرة جديدة نحطها تحت الـ constructor
    private Map<String, String> loadEnvFromFile() {
        Map<String, String> map = new HashMap<>();
        File file = new File("pass.env");
        if (!file.exists()) {
            System.out.println("ملف pass.env غير موجود في مجلد المشروع!");
            return map;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim().replaceAll("\"", "")); // يحذف أي علامات تنصيص
                }
            }
        } catch (Exception e) {
            System.out.println("فشل قراءة ملف pass.env: " + e.getMessage());
        }
        return map;
    }
    private void sendRealEmail(String toEmail, String subject, String message) {
        if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
            System.out.println("Invalid email address: " + toEmail);
            return;
        }

        // طباعة في الكونسول (للتجربة)
        String log = String.format("=== EMAIL SENT ===\nTo: %s\nSubject: %s\nMessage:\n%s\n==================",
                toEmail, subject, message);
        sentMessages.add(log);
        System.out.println(log);

        // إذا ما كان في بيانات SMTP → بس يطبع ويطلع
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
                return new PasswordAuthentication(fromEmail, appPassword); // ← لازم يكون App Password
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
//    // للكود القديم (بالاسم) → ما بنستخدمه حاليًا، بس خليته عشان ما يطلع خطأ في أي مكان ثاني
//    public void sendEmail(String username, String subject, String message) {
//        LibraryUser user = UserService.getInstance().getUserByName(username);
//        String email = (user != null) ? user.getEmail() : "";
//        sendRealEmail(email, subject, message);
//    }

    // للـ sendReminder والاستخدام الجديد (بالإيميل مباشرة) → هذي اللي بنستخدمها
    public void sendEmail(String toEmail, String subject, String message) {
        sendRealEmail(toEmail, subject, message);
    }

    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }
}