package librarymanagement.application;

import librarymanagement.domain.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryService {
    private EmailService emailService;
    private List<Media> mediaList = new ArrayList<>();
    private List<LibraryUser> users = new ArrayList<>();

    public LibraryService(EmailService emailService) {
        this.emailService = emailService;
    }
    // ✅ إضافة وسائط (كتاب أو CD) — آمنة من القيم الفارغة
    public boolean addMedia(Media media) {
        if (media == null) {
            System.out.println("❌ Invalid media: object is null!");
            return false;
        }

        // تحقق من أن الحقول الأساسية موجودة
        if (media.getId() == null || media.getTitle() == null || media.getAuthor() == null) {
            System.out.println("❌ Invalid media: missing ID, title, or author!");
            return false;
        }

        // تحقق من وجود نفس الـ ID مسبقًا
        boolean exists = mediaList.stream()
                .anyMatch(m -> m.getId() != null && m.getId().equalsIgnoreCase(media.getId()));

        if (exists) {
            System.out.println("⚠️ Media with ID " + media.getId() + " already exists!");
            return false;
        }

        // أضف الوسيط الجديد
        mediaList.add(media);
        System.out.println("✅ " + media.getTitle() + " added successfully.");
        return true;
    }


    // ✅ إضافة مستخدم
    public void addUser(LibraryUser user) {
        users.add(user);
        System.out.println("User added: " + user.getName());
    }
    // ✅ البحث عن وسائط (آمن من NullPointerException) + يعطي الأولوية للـ ID
    public List<Media> searchMedia(String keyword) {
        List<Media> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) return results;

        String lowerKeyword = keyword.toLowerCase().trim();

        List<Media> exactIdMatches = new ArrayList<>();
        List<Media> partialMatches = new ArrayList<>();

        for (Media m : mediaList) {
            if (m == null) continue;

            String title = m.getTitle() != null ? m.getTitle().toLowerCase() : "";
            String author = m.getAuthor() != null ? m.getAuthor().toLowerCase() : "";
            String id = m.getId() != null ? m.getId().toLowerCase() : "";

            // تطابق تام مع الـ ID → أولوية عالية
            if (id.equals(lowerKeyword)) {
                exactIdMatches.add(m);
            }
            // تطابق جزئي في العنوان أو المؤلف أو الـ ID
            else if (title.contains(lowerKeyword) || author.contains(lowerKeyword) || id.contains(lowerKeyword)) {
                partialMatches.add(m);
            }
        }

        // أضف التطابقات التامة بالـ ID أولًا
        results.addAll(exactIdMatches);
        results.addAll(partialMatches);

        return results;
    }
    public List<Media> getAllMedia() {
        return mediaList;
    }

    // ✅ استعارة وسائط (كتاب أو CD)
    public boolean borrowMedia(LibraryUser user, Media media) {
        if (!mediaList.contains(media)) {
            System.out.println("Media not found in library: " + media.getTitle());
            return false;
        }

        if (!media.isAvailable()) {
            System.out.println("Media is not available: " + media.getTitle());
            return false;
        }

        boolean hasOverdue = user.getBorrowedMedia().stream()
                .anyMatch(bm -> !bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate()));

        if (hasOverdue) {
            System.out.println("Cannot borrow: You have overdue media.");
            return false;
        }

        if (user.getFineBalance() > 0) {
            System.out.println("Cannot borrow: You have unpaid fines.");
            return false;
        }

        BorrowedMedia borrowedMedia = new BorrowedMedia(media);
        user.getBorrowedMedia().add(borrowedMedia);
        media.setAvailable(false);

        System.out.println(user.getName() + " borrowed: " + media.getTitle()
                + ", due date: " + borrowedMedia.getDueDate());
        return true;
    }

    public void checkOverdueMedia(LibraryUser user) {
        for (BorrowedMedia bm : user.getBorrowedMedia()) {
            if (!bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate())) {
                // ✅ أضف الغرامة مرة واحدة فقط
                if (!bm.isFineAdded()) {
                    user.addFine(bm.getMedia().getFineAmount());
                    bm.setFineAdded(true);
                }

                System.out.println("Overdue: " + bm.getMedia().getTitle() +
                        " | Due: " + bm.getDueDate());
            }
        }
    }



    // ✅ دفع الغرامة
    public void payFine(LibraryUser user, double amount) {
        user.payFine(amount);
        System.out.println(user.getName() + " paid fine: " + amount + " NIS");
    }

    // ✅ إرسال تذكير عبر الإيميل
    public void sendReminder(LibraryUser user) {
        long overdueCount = user.getBorrowedMedia().stream()
                .filter(bm -> !bm.isReturned() && LocalDate.now().isAfter(bm.getDueDate()))
                .count();

        if (overdueCount > 0) {
            String message = "You have " + overdueCount + " overdue media item(s).";
            emailService.sendEmail(user.getName(), message);
        }
    }

    // ✅ حذف مستخدم بواسطة الأدمن
    public boolean unregisterUser(Admin admin, LibraryUser user) {
        if (!admin.isLoggedIn()) {
            System.out.println("Only admins can unregister users.");
            return false;
        }

        boolean hasActiveLoans = user.getBorrowedMedia().stream()
                .anyMatch(bm -> !bm.isReturned());

        if (hasActiveLoans) {
            System.out.println("Cannot unregister: User has active loans.");
            return false;
        }

        if (user.getFineBalance() > 0) {
            System.out.println("Cannot unregister: User has unpaid fines.");
            return false;
        }

        users.remove(user);
        System.out.println("User unregistered successfully: " + user.getName());
        return true;
    }

    public List<LibraryUser> getUsers() {
        return users;
    }
}