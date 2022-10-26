# Vaadin Boot

Boots your Vaadin app in embedded Jetty from your `main()` method quickly and easily.

Tired of complexity of running your app as WAR from your IDE? Tired of using Spring Boot just to start your app in a simple way?
Tired of constant debugging issues? Then this project is for you.

## Using In Your Apps

TODO NOT YET PUBLISHED

Vaadin Boot is published in Maven Central; simply add a dependency on it:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    compile("com.github.mvysny.vaadin-boot:vaadin-boot:10.0")
}
```
Or Maven:
```xml
<project>
	<dependencies>
		<dependency>
			<groupId>com.github.mvysny.vaadin-boot</groupId>
			<artifactId>vaadin-boot</artifactId>
			<version>10.0</version>
		</dependency>
    </dependencies>
</project>
```

Then, add the following `Main` class to your project:
```java
public class Main {
    public static void main(String[] args) throws Exception {
        new VaadinBoot().withArgs(args).run();
    }
}
```

Then add an empty file named `src/main/resources/webapp/ROOT` to your project -
Vaadin Boot will then serve static files from this folder.

## Example Apps

Example apps using Vaadin Boot:

* Vaadin 23, Gradle: [vaadin-embedded-jetty-gradle](https://github.com/mvysny/vaadin-embedded-jetty-gradle)
* Vaadin 23, Maven: [vaadin-embedded-jetty](https://github.com/mvysny/vaadin-embedded-jetty)
* Vaadin 14, Gradle: [vaadin14-embedded-jetty-gradle](https://github.com/mvysny/vaadin14-embedded-jetty-gradle)
* Vaadin 14, Maven: [vaadin14-embedded-jetty](https://github.com/mvysny/vaadin14-embedded-jetty)

## Initializing Your Apps

Simply add the following WebListener to your project:

```java
@WebListener
public class Bootstrap implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Testapp Initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Testapp shut down");
    }
}
```

## Packaging Your Apps

### Gradle

Simply use the [Gradle Application Plugin](https://docs.gradle.org/current/userguide/application_plugin.html)
to build your app:
```groovy
plugins {
    id 'java'
    id 'application'
    id 'com.vaadin' version '23.2.6'
}
application {
    mainClassName = "com.yourapp.Main"
}
```

This will cause Gradle to build your app as a zip file with all dependencies and a run script.
Please see the example apps for more details.

### Maven

TODO - see the example apps please.

## Testing

TODO

# TODO

1. More tests for testapp: start the app and assert it's listening on http 8080; simple wget will do.
2. Assert that all weblisteners were run properly

# Developing

Make sure to test the Boot with all example apps:

1. Run `./gradlew` to clean up any Vaadin production build leftovers
2. Run the `Main` class as a traditional `main()` from Intellij and test that Enter shuts down the app correctly
3. Run `./gradlew clean build testapp:run -Pvaadin.productionMode` and test that CTRL+C kills the app
4. Unzip `testapp/build/distributions/testapp-*.zip`, then run it and test that both CTRL+C and Enter correctly shuts down the app.
