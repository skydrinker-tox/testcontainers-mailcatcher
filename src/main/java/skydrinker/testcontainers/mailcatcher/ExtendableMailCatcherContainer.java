package skydrinker.testcontainers.mailcatcher;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.*;

@SuppressWarnings({"resource", "unused"})
public abstract class ExtendableMailCatcherContainer<SELF extends ExtendableMailCatcherContainer<SELF>> extends GenericContainer<SELF> {

    private static final String MAILCATCHER_IMAGE = "dockage/mailcatcher";
    private static final String MAILCATCHER_VERSION = "0.9.0";

    private static final int MAILCATCHER_SMTP_PORT = 1025;
    private static final int MAILCATCHER_HTTP_PORT = 1080;
    private static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofSeconds(30);

    private static final String MESSAGES_API_PATH = "/messages";
    private Duration startupTimeout = DEFAULT_STARTUP_TIMEOUT;

    /**
     * Create a {@code MailCatcherContainer} with default image and version tag
     */
    public ExtendableMailCatcherContainer() {
        this(MAILCATCHER_IMAGE + ":" + MAILCATCHER_VERSION);
    }

    /**
     * Create a {@code MailCatcherContainer} by passing the full docker image name
     *
     * @param dockerImageName Full docker image name, e.g. dockage/mailcatcher:0.9.0
     */
    public ExtendableMailCatcherContainer(String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(MAILCATCHER_SMTP_PORT, MAILCATCHER_HTTP_PORT);
        withLogConsumer(new Slf4jLogConsumer(logger()));
    }

    @Override
    protected void configure() {
        HostPortWaitStrategy waitStrategy = Wait.forListeningPorts(MAILCATCHER_HTTP_PORT, MAILCATCHER_SMTP_PORT);
        setWaitStrategy(waitStrategy.withStartupTimeout(startupTimeout));
    }

    @Override
    public SELF withCommand(String cmd) {
        throw new IllegalStateException("You are trying to set custom container commands, which is not supported by this Testcontainer.");
    }

    @Override
    public SELF withCommand(String... commandParts) {
        throw new IllegalStateException("You are trying to set custom container commands, which is not supported by this Testcontainer.");
    }

    public SELF withStartupTimeout(Duration startupTimeout) {
        this.startupTimeout = startupTimeout;
        return self();
    }

    public int getHttpPort() {
        return getMappedPort(MAILCATCHER_HTTP_PORT);
    }

    public int getSmtpPort() {
        return getMappedPort(MAILCATCHER_SMTP_PORT);
    }

    public Duration getStartupTimeout() {
        return startupTimeout;
    }

    public String getMessagesApiUrl() {
        return String.format("http://%s:%s%s", getHost(), getHttpPort(), MESSAGES_API_PATH);
    }

    /**
     *
     * @return All received emails as an {@link ArrayList} of {@link MailcatcherMail}
     * @see MailcatcherMail
     */
    public List<MailcatcherMail> getAllEmails() {
        List<MailcatcherMail> emailsSummaries = getAllEmailsSummary();
        List<MailcatcherMail> emailsWithBodies = new ArrayList<>();
        for (MailcatcherMail mail : emailsSummaries) {
            emailsWithBodies.add(getEmailById(mail.id));
        }
        return emailsWithBodies;
    }

    protected List<MailcatcherMail> getAllEmailsSummary() {
        ValidatableResponse messagesResponse = RestAssured.given().when().get(getMessagesApiUrl()).then();
        return Arrays.asList(messagesResponse.extract().body().as(MailcatcherMail[].class));
    }

    /**
     * @param emailId The id of the email to retrieve
     * @return The email with id {@code emailId}
     * @see MailcatcherMail
     */
    public MailcatcherMail getEmailById(int emailId) {
        String jsonEmailEndpointUrl = String.format("%s/%s.json", getMessagesApiUrl(), emailId);
        try {
            MailcatcherMail mail = RestAssured.given().when().get(jsonEmailEndpointUrl).then().statusCode(200).extract().body().as(MailcatcherMail.class);
            for (String emailFormat : mail.formats) {
                String singleMailEndpointUrl = String.format("%s/%s.%s", getMessagesApiUrl(), emailId, emailFormat);
                String body = RestAssured.given().when().get(singleMailEndpointUrl).then().statusCode(200).extract().body().asString();
                mail.bodies.put(emailFormat, body);
            }
            return mail;
        } catch (AssertionError e) {
            //TDOO : Throw mailNotFoundException instead of returning null
            return null;
        }

    }

    /**
     * @return Last received email
     * @see MailcatcherMail
     */
    public MailcatcherMail getLastEmail() {
        List<MailcatcherMail> emailsSummaries = getAllEmailsSummary();
        if (emailsSummaries.isEmpty()) {
            return null;
        }
        return getEmailById(emailsSummaries.get(emailsSummaries.size() - 1).id);
    }

    @SuppressWarnings({"ConstantValue", "UnreachableCode"})
    public String getMailCatcherDefaultVersion() {
        return MAILCATCHER_VERSION.equals("0.9.0") ? "0.9.0-SNAPSHOT" : MAILCATCHER_VERSION;
    }

}
