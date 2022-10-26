# Vaadin Boot

Boots your Vaadin app in embedded Jetty from your `main()` method quickly and easily, **without Spring**.

Tired of complexity of running your app as WAR from your IDE? Tired of using Spring Boot just to start your app in a simple way?
Tired of constant debugging issues? Then this project is for you. From now on, you
can use the free [Intellij Community](https://www.jetbrains.com/idea/download) to develop your projects.

## Using In Your Apps

Vaadin Boot is published in Maven Central; simply add a dependency on it:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.mvysny.vaadin-boot:vaadin-boot:10.0")
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

Versions: Use:

* Version 10.0 for Jetty 10+, requires Java JDK 11+
* Version 9.x for Jetty 9.x, requires Java JDK 8+ (TODO not yet released)

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

By default, VaadinBoot listens on all interfaces; call `localhostOnly()` to
only listen on localhost.

### Logging

We're using slf4j. If you're using simplelogger, you can configure it like follows:
```properties
org.slf4j.simpleLogger.defaultLogLevel = info
org.slf4j.simpleLogger.showDateTime = true
org.slf4j.simpleLogger.dateTimeFormat = yyyy-MM-dd HH:mm:ss.SSS
org.slf4j.simpleLogger.log.org.atmosphere = warn
org.slf4j.simpleLogger.log.org.eclipse.jetty = warn
org.slf4j.simpleLogger.log.org.eclipse.jetty.annotations.AnnotationParser = error
```

This will suppress cluttering of stdout/logs with verbose messages from Atmosphere and Jetty.

## Command-line Args

Really dumb at the moment; if there's a port passed as the first parameter then it will be used, otherwise
the default port of 8080 will be used.

You'll have to implement more complex cmdline arg parsing yourself; good start
is to use [Apache commons-cli](https://commons.apache.org/proper/commons-cli/).

## Example Apps

Example apps using Vaadin Boot:

* Vaadin 23, Gradle: [vaadin-embedded-jetty-gradle](https://github.com/mvysny/vaadin-embedded-jetty-gradle)
* Vaadin 23, Maven: [vaadin-embedded-jetty](https://github.com/mvysny/vaadin-embedded-jetty)
* Vaadin 14, Gradle: [vaadin14-embedded-jetty-gradle](https://github.com/mvysny/vaadin14-embedded-jetty-gradle)
* Vaadin 14, Maven: [vaadin14-embedded-jetty](https://github.com/mvysny/vaadin14-embedded-jetty)

## Running Your Apps

To run your app

* from IDE: simply run the `main()` method of your Main class. This is the best way to develop your app in an IDE.
* from command line in development mode; this is the fastest way to run the app without any IDE:
  * from Gradle: run `./gradlew run` (you'll need to use the Gradle Application plugin)
  * from Maven: run `./mvnw -C exec:java` (you'll need to use the `exec-maven-plugin`)
* from command-line in production mode: this is how you should deploy your apps to production.
    The build of your app should produce a zip file; unzip the file and launch the run script.
  * Gradle Application plugin will package the app for you, there's nothing you need to do
  * With Maven you'll need to configure the Assembly plugin to build the applicatin zip or executable jar.

## Initializing services in your app

Simply add the following WebListener to your project:

```java
@WebListener
public class Bootstrap implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // will be called exactly once, before any request is served. Initialize your JVM singletons here.
        log.info("Testapp Initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // will be called exactly once, after your server stops serving http requests, but before the JVM terminates.
        log.info("Testapp shut down");
    }
}
```

## Build scripts

Hints for buildscripts (`pom.xml`/`build.gradle`) of your app. When in doubt, take a look
at the example apps mentioned above.

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

Use Maven Assembly plugin to build a zip file with all dependencies and a run script file:
```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd">
    <id>zip</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
        <dependencySet>
            <outputDirectory>libs</outputDirectory>
        </dependencySet>
    </dependencySets>
    <fileSets>
        <fileSet>
            <directory>src/main/scripts</directory>
            <outputDirectory></outputDirectory>
            <fileMode>0755</fileMode>
        </fileSet>
    </fileSets>
</assembly>
```

Then create a script named `run` and place it into `src/main/scripts/`:
```bash
#!/bin/bash
set -e -o pipefail
CP=`ls libs/*.jar|tr '\n' ':'`
java -cp $CP com.vaadin.starter.skeleton.Main "$@"
```

Then configure the assembly plugin:
```xml
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.vaadin.starter.skeleton.Main</mainClass>
                        </manifest>
                    </archive>
                    <descriptors>
                        <descriptor>src/main/assembly/uberjar.xml</descriptor>
                        <descriptor>src/main/assembly/zip.xml</descriptor>
                    </descriptors>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

Alternatively, you can package your app as a huge jar file which can then be launched via `java -jar yourapp.jar`.
See [vaadin-embedded-jetty](https://github.com/mvysny/vaadin-embedded-jetty) for a full example.

You can also use the `exec-maven-plugin` to run your app easily from Maven:
```xml
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>1.6.0</version>
                <configuration>
                    <mainClass>com.vaadin.starter.skeleton.Main</mainClass>
                </configuration>
            </plugin>
```
Afterwards you can run your app via `mvn -C exec:java`.

## Hot-Redeployment

Vaadin will say "Java live reload unavailable" with the standard OpenJDK java.
Please follow the [Live Reload](https://vaadin.com/docs/latest/configuration/live-reload/hotswap-agent)
and either use JRebel, or install a JVM which supports DCEVM+HotswapAgent (e.g. [trava-jdk](https://github.com/TravaOpenJDK/trava-jdk-11-dcevm));
you may then need to run the app with the following VM options: `-dcevm -XX:HotswapAgent=fatjar` (UNTESTED).

## Production

Make sure to have `flow-server-production-mode.jar` on classpath when running in production mode;
also make sure to build and package Vaadin production bundle into the jar file of your app:

* Make sure your zip file contains the `flow-server-production-mode.jar`.
* Make sure the jar of your app contains the folder `META-INF/VAADIN/webapp/VAADIN/build/*.js` (this is for Vaadin 23; Vaadin 14 file structure will differ) and the `META-INF/VAADIN/config/flow-build-info.json` says `"productionMode": true`.
  * Read more at [Vaadin: The missing guide](https://mvysny.github.io/Vaadin-the-missing-guide/), the "production" mode.

Vaadin Gradle plugin does all of the above automatically when `-Pvaadin.productionMode` gradle build parameter is passed in;
Maven projects usually define the `production` profile which handles everything correctly when activated
via `mvn -C clean package -Pproduction`.

## Testing

Test using [Karibu-Testing](https://github.com/mvysny/karibu-testing/) - you don't have to start
Jetty to test your app:

```java
public class MainViewTest {
    @NotNull
    private static final Routes routes = new Routes().autoDiscoverViews("com.example");

    @BeforeAll
    public static void setupApp() {
        assertFalse(Bootstrap.initialized);
        new Bootstrap().contextInitialized(null);
    }

    @AfterAll
    public static void tearDownApp() {
        new Bootstrap().contextDestroyed(null);
        assertFalse(Bootstrap.initialized);
    }

    @BeforeEach
    public void setupVaadin() {
        MockVaadin.setup(routes);
    }

    @AfterEach
    public void tearDownVaadin() {
        MockVaadin.tearDown();
    }

    @Test
    public void smoke() {
        UI.getCurrent().navigate(MainView.class);
        _assertOne(MainView.class);
        assertTrue(Bootstrap.initialized);
    }
}
```

# Developing

See [CONTRIBUTING](CONTRIBUTING.md)
