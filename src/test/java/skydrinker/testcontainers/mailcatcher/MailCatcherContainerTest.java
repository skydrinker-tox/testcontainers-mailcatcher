package skydrinker.testcontainers.mailcatcher;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ContainerLaunchException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MailCatcherContainerTest {

    @Test
    public void shouldStartMailCatcher() {
        try (MailCatcherContainer mailcatcher = new MailCatcherContainer()) {
            mailcatcher.start();
        }
    }

    @Test
    public void shouldConsiderConfiguredStartupTimeout() {
        final int MAX_TIMEOUT = 1;
        Instant start = Instant.now();
        try {
            Duration duration = Duration.ofSeconds(MAX_TIMEOUT);
            try (MailCatcherContainer mailcatcher = new MailCatcherContainer().withStartupTimeout(duration)) {
                mailcatcher.start();
            }
        } catch (ContainerLaunchException ex) {
            Duration observedDuration = Duration.between(start, Instant.now());
            assertTrue(observedDuration.toSeconds() >= MAX_TIMEOUT && observedDuration.toSeconds() < 30,
                String.format("Startup time should consider configured limit of %d seconds, but took %d seconds",
                    MAX_TIMEOUT, observedDuration.toSeconds()));
        }
    }

    @Test
    void shouldStartWithEmptyMailbox() {
        try (MailCatcherContainer mailcatcher = new MailCatcherContainer()) {
            mailcatcher.start();
            List<MailcatcherMail> messageList = mailcatcher.getAllEmails();
            assertThat(messageList, equalTo(Collections.emptyList()));
        }
    }

    @Test
    void shouldGetAllEmailsWithBodies() {

        try (MailCatcherContainer mailcatcher = new MailCatcherContainer()) {
            mailcatcher.withReuse(false).start();

            String fromEmail = "mailcatcher-from@testcontainer.local";
            String toEmail = "mailcatcher-to@testcontainer.local";
            String emailSubject = "SimpleEmail Test Subject";
            String textBody = "Plain Text Body";
            String htmlBody = "<html><body>HTML Body</body></html>";

            sendEmail(mailcatcher.getHost(), mailcatcher.getSmtpPort(), fromEmail, toEmail, emailSubject, textBody, htmlBody);

            List<MailcatcherMail> messageList = mailcatcher.getAllEmails();
            assertThat(messageList.size(), equalTo(1));
            assertThat(messageList.get(0).subject, equalTo(emailSubject));
            assertThat(messageList.get(0).sender, equalTo("<mailcatcher-from@testcontainer.local>"));
            assertThat(messageList.get(0).getPlainTextBody(), equalTo(textBody));
            assertThat(messageList.get(0).getHtmlBody(), equalTo(htmlBody));
        }
    }

    @Test
    void getEmailByIdShouldReturnNullForNonExistingId() {

        try (MailCatcherContainer mailcatcher = new MailCatcherContainer()) {
            mailcatcher.start();
            MailcatcherMail mail = mailcatcher.getEmailById(1);
            assertThat(mail, nullValue());
        }
    }

    @Test
    void getEmailByIdShouldReturnMailBodies() {

        try (MailCatcherContainer mailcatcher = new MailCatcherContainer()) {
            mailcatcher.withReuse(false).start();
            String fromEmail = "mailcatcher-from@testcontainer.local";
            String toEmail = "mailcatcher-to@testcontainer.local";
            String textBody = "Plain Text Body";
            String htmlBody = "<html><body>HTML Body</body></html>";
            String emailSubject = "SimpleEmail Test Subject";

            sendEmail(mailcatcher.getHost(), mailcatcher.getSmtpPort(), fromEmail, toEmail, emailSubject, textBody, htmlBody);

            MailcatcherMail mail = mailcatcher.getEmailById(1);
            assertThat(mail, notNullValue());
            assertThat(mail.getPlainTextBody(), equalTo(textBody));
            assertThat(mail.getHtmlBody(), equalTo(htmlBody));
        }
    }

    @Test
    void getLastEmailShouldReturnLastMailOrNull() {

        try (MailCatcherContainer mailcatcher = new MailCatcherContainer()) {
            mailcatcher.start();

            MailcatcherMail mail = mailcatcher.getLastEmail();
            assertThat(mail, nullValue());

            String fromEmail = "mailcatcher-from@testcontainer.local";
            String toEmail = "mailcatcher-to@testcontainer.local";
            String textBody1 = "Body 1";
            String textBody2 = "Body 2";
            String emailSubject = "email subject";

            sendEmail(mailcatcher.getHost(), mailcatcher.getSmtpPort(), fromEmail, toEmail, emailSubject, textBody1, null);
            sendEmail(mailcatcher.getHost(), mailcatcher.getSmtpPort(), fromEmail, toEmail, emailSubject, textBody2, null);

            mail = mailcatcher.getLastEmail();
            assertThat(mail, notNullValue());
            assertThat(mail.getPlainTextBody(), equalTo(textBody2));

        }
    }

    private void sendEmail(String host, int port, String fromEmail, String toEmails, String subject, String textBody, String htmlBody) {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", false);
            prop.put("mail.smtp.starttls.enable", "false");
            prop.put("mail.smtp.host", host);
            prop.put("mail.smtp.port", port);
            Session session = Session.getInstance(prop);

            MimeMultipart msgContent = createMultipartContent(textBody, htmlBody);
            MimeMessage msg = new MimeMessage(session);
            msg.setContent(msgContent);
            //set message headers
            msg.setFrom(new InternetAddress(fromEmail, false));
            msg.setReplyTo(InternetAddress.parse(fromEmail, false));
            msg.setSubject(subject, "UTF-8");
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmails, false));
            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MimeMultipart createMultipartContent(String textBody, String htmlBody) throws MessagingException {
        MimeMultipart msgContent = new MimeMultipart();
        if (textBody != null) {
            MimeBodyPart plainPart = createPlainTextBodyPart(textBody);
            msgContent.addBodyPart(plainPart);
        }
        if (htmlBody != null) {
            MimeBodyPart htmlPart = createHtmlBodyPart(htmlBody);
            msgContent.addBodyPart(htmlPart);
        }
        return msgContent;
    }

    private MimeBodyPart createPlainTextBodyPart(String body) throws MessagingException {
        MimeBodyPart plainPart = new MimeBodyPart();
        plainPart.setContent(body, "text/plain");
        plainPart.setHeader("Content-Type", "text/plain");
        plainPart.setHeader("Content-Transfer-Encoding", "7bit");
        return plainPart;
    }

    private MimeBodyPart createHtmlBodyPart(String body) throws MessagingException {
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(body, "text/plain");
        htmlPart.setHeader("Content-Type", "text/html");
        htmlPart.setHeader("Content-Transfer-Encoding", "7bit");
        return htmlPart;
    }
}
