# MailCatcher Testcontainer

A [Testcontainers](https://www.testcontainers.org/) implementation for Mailcatcher.

[![CI build](https://github.com/skydrinker-tox/testcontainers-mailcatcher/actions/workflows/maven.yml/badge.svg)](https://github.com/skydrinker-tox/testcontainers-mailcatcher/actions/workflows/maven.yml)
[![](https://img.shields.io/github/v/release/skydrinker-tox/testcontainers-mailcatcher?label=Release)](https://github.com/skydrinker-tox/testcontainers-mailcatcher/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.skydrinker-tox/testcontainers-mailcatcher.svg?label=Maven%20Central&color=blue)](https://central.sonatype.com/search?namespace=io.github.skydrinker-tox&name=testcontainers-mailcatcher)
![](https://img.shields.io/github/license/skydrinker-tox/testcontainers-mailcatcher?color=blue)
![](https://img.shields.io/badge/dockage/mailcatcher-0.9.0-blue)

## How to use

_The `@Container` annotation used here in the readme is from the JUnit 5 support of Testcontainers.
Please refer to the Testcontainers documentation for more information._

### Default

Simply spin up a default MailCatcherContainer instance:

```java
@Container
MailCatcherContainer mailcatcherContainer = new MailCatcherContainer();
```

### Custom image

Use another MailCatcher Docker image/version than used in this Testcontainer:

```java
@Container
MailCatcherContainer mailcatcherContainer = new MailCatcherContainer("dockage/mailcatcher:0.9.0");
```

### Use mailCatcher testContainer as SMTP server

Get MailCatcher Testcontainer host and port for stubbing your STMP server:

```java
String smtpHost = mailcatcherContainer.getHost();
int smtpPort = mailcatcherContainer.getSmtpPort();
```

### Get received emails

You can get all received emails directly from the container, using

```java
List<MailcatcherMail> emailList = mailcatcherContainer.getAllEmails();
```

You can get only the last received email and access its content using

```java
MailcatcherMail email = mailcatcherContainer.getLastEmail();
String htmlEmailBody = email.getHtmlBody();
String plainTextEmailBody = email.getPlainTextBody();
```

## Extending MailCatcherContainer

In case you need a custom implementation of the default `MailCatcherContainer`, you should inherit from `ExtendableMailCatcherContainer`. This allows to set the generics and use your custom implementation without the need for type casts.  

```java
public class MyCustomMailCatcherContainer extends ExtendableMailCatcherContainer<MyCustomMailCatcherContainer> {

	public MyCustomMailCatcherContainer() {
		super();
	}

	public MyCustomMailCatcherContainer(String dockerImageName) {
		super(dockerImageName);
	}
	
}
```

## Setup

The release versions of this project are available at [Maven Central](https://search.maven.org/artifact/com.github.skydrinker-tox/testcontainers-mailcatcher).
Simply put the dependency coordinates to your `pom.xml` (or something similar, if you use e.g. Gradle or something else):

```xml
<dependency>
  <groupId>com.github.skydrinker-tox</groupId>
  <artifactId>testcontainers-mailcatcher</artifactId>
  <version>VERSION</version>
  <scope>test</scope>
</dependency>
```

### JUnit4 Dependency

The testcontainers project itself has a dependency on JUnit4 although it is not needed for this project in order to run (see this [issue](https://github.com/testcontainers/testcontainers-java/issues/970) for more details).
To avoid pulling in JUnit4 this project comes with a dependency on the `quarkus-junit4-mock` library which includes all needed classes as empty stubs. If you need JUnit4 in your project you should exclude this mock library
when declaring the dependency to `testcontainers-mailcatcher` to avoid issues. Example for maven:

```xml
<dependency>
    <!-- ... see above -->
    <exclusions>
        <exclusion>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit4-mock</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## Usage in your application framework tests

> This info is not specific to the MailCatcher Testcontainer, but using Testcontainers generally.

I mention it here, as I see people asking again and again on how to use it in their test setup, when they think they need to specify a fixed port in their properties or YAML files...  
You don't have to!  
But you have to read the Testcontainers docs and the docs of your application framework on testing resources!!

### Spring (Boot)

Dynamic context configuration with context initializers is your friend.
In particular, look for `@ContextConfiguration` and `ApplicationContextInitializer<ConfigurableApplicationContext>`:
* https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-testing-annotation-contextconfiguration
* https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-ctx-management-initializers

### Quarkus

Read the docs about the Quarkus Test Resources and use `@QuarkusTestResource` with `QuarkusTestResourceLifecycleManager`
* https://quarkus.io/guides/getting-started-testing#quarkus-test-resource

### Others

Consult the docs of your application framework testing capabilities on how to dynamically configure your stack for testing!

