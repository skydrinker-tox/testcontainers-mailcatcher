package skydrinker.testcontainers.mailcatcher;

import java.util.HashMap;
import java.util.List;

public class MailcatcherMail {
    private static final String EMAIL_FORMAT_PLAIN = "plain";
    private static final String EMAIL_FORMAT_HTML = "html";
    private static final String EMAIL_FORMAT_SOURCE = "source";
    int id;
    String sender;
    List<String> recipients;
    String subject;
    String size;
    String created_at;
    List<String> formats;
    HashMap<String, String> bodies = new HashMap<>();

    /**
     *
     * @return The HTML part of the multipart email or {@code null} if the email has no HTML part
     */
    public String getHtmlBody() {
        return bodies.getOrDefault(EMAIL_FORMAT_HTML, null);
    }

    /**
     *
     * @return The Plain text part of the multipart email or {@code null} if the email has no Plain text part
     */
    public String getPlainTextBody() {
        return bodies.getOrDefault(EMAIL_FORMAT_PLAIN, null);
    }

    /**
     *
     * @return The raw sources of the multipart email or {@code null} if the email source has not been set
     */
    public String getEmailSource() {
        return bodies.getOrDefault(EMAIL_FORMAT_SOURCE, null);
    }
    public int getId() {
        return id;
    }
    public String getSender() {
        return sender;
    }
    public List<String> getRecipients() {
        return recipients;
    }
    public String getSubject() {
        return subject;
    }
    public String getSize() {
        return size;
    }
    public String getCreatedAt() {
        return created_at;
    }
    public List<String> getFormats() {
        return formats;
    }
}

