package skydrinker.testcontainers.mailcatcher;


public class MailCatcherContainer extends ExtendableMailCatcherContainer<MailCatcherContainer> {

    public MailCatcherContainer() {
        super();
    }

    public MailCatcherContainer(String dockerImageName) {
        super(dockerImageName);
    }

}
