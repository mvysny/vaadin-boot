# Vaadin Boot

Boots your Vaadin app in embedded Jetty from your `main()` method quickly and easily, **without Spring**.

Tired of complexity of running your app as WAR from your IDE? Tired of using Spring Boot just to start your app in a simple way?
Tired of constant debugging issues? Then this project is for you. From now on, you
can use the free [Intellij Community](https://www.jetbrains.com/idea/download) to develop your projects.

Who is Vaadin Boot for? Vaadin Boot is for developers that [prefer to create their own solution, DIY](https://mvysny.github.io/frameworkless-diy/)
exactly to fit their needs. You don't like to start by including a pre-fabricated application framework,
complex and abstract enough to handle hundreds of use-cases.
You don't have hundreds of use-cases: you only have one. You only use what you need.
There lies maximum simplicity which you own, understand and can rely on.

## Using In Your Apps

Vaadin Boot is published in Maven Central; simply add a dependency on it:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.mvysny.vaadin-boot:vaadin-boot:10.1")
}
```
Or Maven:
```xml
<project>
	<dependencies>
		<dependency>
			<groupId>com.github.mvysny.vaadin-boot</groupId>
			<artifactId>vaadin-boot</artifactId>
			<version>10.1</version>
		</dependency>
    </dependencies>
</project>
```

Compatibility chart: Vaadin Boot follows Jetty versioning.

| Vaadin-Boot version                                                               | Min Java | Servlet Spec     | Supported Vaadin | Jetty |
|-----------------------------------------------------------------------------------|----------|------------------|------------------|-------|
| 10.x                                                                              | Java 11+ | javax.servlet    | Vaadin 14-23     | 10.x  |
| 11.x (not yet released, see [#4](https://github.com/mvysny/vaadin-boot/issues/4)) | Java 17+ | jakarta.servlet  | Vaadin 24+       | 11.x  |

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

### Missing `/src/main/webapp`?

Since we're not packaging to WAR, the `src/main/webapp` folder is ignored. We need to package the `webapp` folder
in a way so that it's served from the jar itself. Indeed, the directory is located at `src/main/resources/webapp`.
Make sure to add an empty file named `src/main/resources/webapp/ROOT` - that will allow Vaadin Boot
to quickly figure out the precise location of your webapp directory in the class loader.

### Command-line Args

Really dumb at the moment; if there's a port passed as the first parameter then it will be used, otherwise
the default port of 8080 will be used.

You'll have to implement more complex cmdline arg parsing yourself; good start
is to use [Apache commons-cli](https://commons.apache.org/proper/commons-cli/).

### Example Apps

Example apps using Vaadin Boot:

* Vaadin 23, Gradle: [vaadin-boot-example-gradle](https://github.com/mvysny/vaadin-boot-example-gradle)
* Vaadin 23, Maven: [vaadin-boot-example-maven](https://github.com/mvysny/vaadin-boot-example-maven)
* Vaadin 14, Gradle: [vaadin14-boot-example-gradle](https://github.com/mvysny/vaadin14-boot-example-gradle)
* Vaadin 14, Maven: [vaadin14-boot-example-maven](https://github.com/mvysny/vaadin14-boot-example-maven)

## Preparing environment

Please install Java JDK 11 or higher.

The Vaadin build requires node.js and npm to build the 'frontend bundle'.
However, that will happen automatically so there's nothing you need to do: Vaadin plugin will automatically download
node.js and npm for you (node.js will be downloaded and run from `$HOME/.vaadin`).

## Running your apps

Grab the sources of your app from the git repository.
To run your app quickly from command-line, without having to run your IDE:

1. Run `./gradlew run` (or `./mvnw -C exec:java` for Maven)
    * Note you'll need to use the Gradle Application plugin, or the `exec-maven-plugin`
2. Your app will be running on [http://localhost:8080](http://localhost:8080).

To run the app from your IDE (we recommend using [Intellij IDEA](https://www.jetbrains.com/idea/download), the Community edition):

1. Import the project into your IDE
2. Run `./gradle vaadinPrepareFrontend` in the project once (or `./mvnw -C vaadin:prepare-frontend` for Maven), to configure Vaadin paths.
3. Run/Debug the `Main` class as an application (run the `main()` method).
   The app will use npm to download all javascript libraries (may take a long time)
   and will start in development mode.
4. Your app will be running on [http://localhost:8080](http://localhost:8080).

When deploying your app to production: see the "Production" chapter below. In short:

1. Build your app in production mode, via `./gradlew clean build -Pvaadin.productionMode` or `./mvnw -C clean package -Pproduction`.
2. The build of your app should produce a zip file; unzip the file and launch the run script.

## Develop with pleasure

You can download and install the [Intellij IDEA Community Edition](https://www.jetbrains.com/idea/download), then import this project into it.

Open the app in your IDE, and debug the `Main` class as an application (run the `main()` method in debugging mode).
Then, open your browser and hit [http://localhost:8080](http://localhost:8080).

This will activate two things:

* Contrary to what Vaadin says ("Java live reload unavailable"), Vaadin will automatically
  detect changes in your CSS/JavaScript files, will rebuild the JavaScript bundle and will
  reload the page to apply the new values.
* When you do changes in your java files and recompile (Ctrl+F9 in Intellij),
  [Java HotSwap](https://docs.oracle.com/javase/8/docs/technotes/guides/jpda/enhancements1.4.html#hotswap)
  will update classes in your running app. Just press F5 in your browser to reload the page and
  to see your changes.

There are lots of pre-existing Vaadin components; you can check out the
[Beverage Buddy](https://github.com/mvysny/beverage-buddy-vok/) example app for more
examples of component usage. You should also read the [full Vaadin documentation](https://vaadin.com/docs/flow/Overview.html).

The browser is a very powerful IDE which can help you debug CSS- and layout-related issue. Take your time and read slowly through the following tutorials, to get acquinted with the browser
developer tools:

* [Chrome Developer Tools tutorial](https://developers.google.com/web/tools/chrome-devtools/)
* [Firefox Developer Tools tutorial](https://developer.mozilla.org/en-US/docs/Tools)

### Advanced HotSwapping

The default Java HotSwap is limited to Java method in-body code changes only. However,
that is more than enough for even a professional development. Optionally, if you need
better HotSwapping capabilities, please try following the links below:

* Please follow the [Live Reload](https://vaadin.com/docs/latest/configuration/live-reload/hotswap-agent)
* (Ubuntu): install `openjdk-11-jre-dcevm` and run your app with the `-dcevm` VM parameter.
* [Use HotSwapAgent](http://hotswapagent.org/mydoc_setup_intellij_idea.html)
* Install a JVM which supports DCEVM+HotswapAgent (e.g. [trava-jdk](https://github.com/TravaOpenJDK/trava-jdk-11-dcevm));
  you may then need to run the app with the following VM options: `-dcevm -XX:HotswapAgent=fatjar`.
* Use JRebel

### Initializing Your Apps

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

### Logging

Vaadin-Boot is using the [slf4j](https://www.slf4j.org/) logging framework by default, and your
apps should use it too. We initially recommend you to use the SLF4J [SimpleLogger](https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html)
logger (use it by adding this dependency to your project: `implementation("org.slf4j:slf4j-simple:2.0.0")`.

You can configure SimpleLogger with the following file (placed into `src/main/resources/simplelogger.properties`):
```properties
org.slf4j.simpleLogger.defaultLogLevel = info
org.slf4j.simpleLogger.showDateTime = true
org.slf4j.simpleLogger.dateTimeFormat = yyyy-MM-dd HH:mm:ss.SSS
org.slf4j.simpleLogger.log.org.atmosphere = warn
org.slf4j.simpleLogger.log.org.eclipse.jetty = warn
org.slf4j.simpleLogger.log.org.eclipse.jetty.annotations.AnnotationParser = error
```

This will suppress cluttering of stdout/logs with verbose messages from Atmosphere and Jetty.

### REST via Javalin

We'll use [Javalin](https://javalin.io) 4.x since Javalin 5.x uses Servlet API 5 which is not compatible with Vaadin 23.

Add Javalin to your build script:
```groovy
    implementation("io.javalin:javalin:4.6.7") {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }
```

Then add the following class to your project:
```java
@WebServlet(name = "MyJavalinServlet", urlPatterns = {"/rest/*"})
public class MyJavalinServlet extends HttpServlet {
    private final JavalinServlet javalin = Javalin.createStandalone()
            .get("/rest", ctx -> ctx.result("Hello!"))
            .javalinServlet();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        javalin.service(req, resp);
    }
}
```

Vaadin-Boot will automatically discover the servlet and initialize it properly. To test, you can run

```bash
$ curl -v localhost:8080/rest
```

Testing: follow this example, to only initialize the REST servlet (no need to initialize the Vaadin servlet as well):
```kotlin
class MyJavalinServletTest {
    private var server: Server? = null

    @BeforeEach
    fun startJetty() {
        val ctx = WebAppContext()
        ctx.baseResource = EmptyResource.INSTANCE
        ctx.addServlet(MyJavalinServlet::class.java, "/rest/*")
        server = Server(30123)
        server!!.handler = ctx
        server!!.start()
    }

    @AfterEach
    fun stopJetty() {
        server?.stop()
    }

    @Test
    fun testRest() {
        assertEquals("Hello!", URL("http://localhost:30123/rest").readText())
    }
}
```

## Packaging Your Apps

This part documents hints for buildscripts (`pom.xml`/`build.gradle`) of your app. When in doubt, take a look
at the example apps mentioned above.

### Gradle

Vaadin-Boot runs embedded Jetty itself. Therefore, Vaadin-Boot-based apps do not use the Gretty Gradle plugin
and do not package themselves as WAR files - instead the apps are packaged as a Java application:
a zip file with all jar dependencies and a run script.

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

The [Vaadin Gradle Plugin](https://vaadin.com/docs/latest/flow/guide/start/gradle)
is used to package all JavaScript stuff into a JavaScript bundle. See the Plugin
home page for more details.

> **Info:** **Eclipse**+BuildShip may need a workaround in order for this project to work,
> please see [this vaadin thread](https://vaadin.com/forum/thread/18241436) for more info.
> This applies to **Visual Studio Code** as well since it also uses Eclipse bits and BuildShip
> underneath - see [Bug #4](https://github.com/mvysny/vaadin14-embedded-jetty-gradle/issues/4)
> for more details.

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
See [vaadin-boot-example-maven](https://github.com/mvysny/vaadin-boot-example-maven) for a full example.

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

## Production

By default, Vaadin is running in development mode. Vaadin will start a DevServer which detects
changes done in your CSS and JavaScript files and rebuilds the JavaScript bundle automatically.
This is great for development but not fit for production. Please read [Deploying to Production](https://vaadin.com/docs/latest/production)
for more details.

All Vaadin apps follow the following convention when building for production:

* Gradle: build your app with `./gradlew clean build -Pvaadin.productionMode`. Vaadin Gradle Plugin
  will automatically build your app in production mode and will include `flow-server-production-mode.jar`.
* Maven: build your app with `mvn -C clean package -Pproduction`. You need to add the `production`
   profile which handles everything correctly when activated. Please see example apps for details.

In both cases, the JavaScript bundle is built at build time as opposed at runtime with devmode.
You can easily verify that your app has been built in production mode:

* When you run the app, Vaadin will log to stdout that it's running in production mode
* The `flow-server-production-mode.jar` jar file is packaged in the zip file of your app.
* The `yourapp.jar/META-INF/VAADIN/config/flow-build.info.json` will say `"productionMode":true`
* There are JavaScript files in `yourapp.jar/META-INF/VAADIN/webapp/VAADIN/build/` (this applies to Vaadin 23; for Vaadin 14 the file structure is a bit different)
    * Read more at [Vaadin: The missing guide](https://mvysny.github.io/Vaadin-the-missing-guide/), the "production" mode.

[Jetty is perfectly capable](https://www.eclipse.org/jetty/) of running in production as documented
on the Jetty web page.

### Docker

Packaging your apps as docker images is incredibly easy. We use [Docker Multi-stage builds](https://docs.docker.com/build/building/multi-stage/):

* We initialize the build environment and build the app in one docker image;
* We copy the result app to a new image and throw away the build environment completely, to not clutter our production image.

Example `Dockerfile` for a Gradle-based app:
```dockerfile
# The "Build" stage. Copies the entire project into the container, into the /app/ folder, and builds it.
FROM openjdk:11 AS BUILD
COPY . /app/
WORKDIR /app/
RUN ./gradlew clean test --no-daemon --info --stacktrace
RUN ./gradlew build -Pvaadin.productionMode --no-daemon --info --stacktrace
WORKDIR /app/build/distributions/
RUN ls -la
RUN unzip app.zip
# At this point, we have the app (executable bash scrip plus a bunch of jars) in the
# /app/build/distributions/app/ folder.

# The "Run" stage. Start with a clean image, and copy over just the app itself, omitting gradle, npm and any intermediate build files.
FROM openjdk:11
COPY --from=BUILD /app/build/distributions/app /app/
WORKDIR /app/bin
EXPOSE 8080
ENTRYPOINT ./app
```

You then run the following commands from terminal: first one will build the docker image, the second one will run your app in Docker:

```bash
$ docker build --no-cache -t test/yourapp:latest .
$ docker run --rm -ti -p8080:8080 test/yourapp
```

Please find the `Dockerfile` in each of the example apps above.

#### Docker + Vaadin Pro

To use paid Vaadin Pro components you'll need an [offline server key (Vaadin License key)](https://vaadin.com/docs/latest/configuration/licenses).
You can then pass the key to the `docker build` via the means of Docker build-args:

1. Add the following to your `Dockerfile`: `ARG offlinekey`; `ENV VAADIN_OFFLINE_KEY=$offlinekey`
2. Build the app with `$ docker build --no-cache -t test/yourapp:latest --build-arg offlinekey='eyJra.....the_very_long_1600_character_offline_key_text_blob' .`

### https/ssl

Vaadin Boot doesn't support https at the moment. The usual setup is to have Nginx unwrap ssl and pass it through to
a Boot app listening for http on localhost. The reason is that you can safely restart Nginx when there's a need to apply
new certificate. There are manuals on the interwebs on:

* how to have Nginx automatically poll in newest Let's Encrypt certificates and apply them automatically;
* how to unwrap https and pass it over to a http port

In short, here are brief steps to setup Nginx+[Let's Encrypt](https://letsencrypt.org/) on Ubuntu machine:

* First, make sure your Vaadin Boot project is listening on localhost only. This will ensure that all requests will go through Nginx.
* Then, remove the default site: `sudo rm /etc/nginx/sites-enabled/default`
* Then write your own `/etc/nginx/sites-enabled/yourapp`, something like this:

```nginx
server {
  location / {
    proxy_pass http://localhost:8080/;
    # proxy_cookie_path / /foo; # use this if you mount your app to `location /foo/`
    proxy_cookie_domain localhost $host;
  }
}
```

Reload nginx configuration (`sudo systemctl reload nginx.service`) and verify that you can access your app
via `http://yourserver`.

Then follow [Let's Encrypt's getting started](https://letsencrypt.org/getting-started/); the [command-line certbot
instructions for Ubuntu](https://certbot.eff.org/instructions?ws=nginx&os=ubuntufocal) worked the best for me.
`sudo certbot --nginx` will download the certificates and will modify your nginx config file to use the certificates
and to automatically redirect from http to https. It will also install itself to cron, to auto-refresh the certificate.

## Testing

It is very easy to test Vaadin-based apps. We will test using [Karibu-Testing](https://github.com/mvysny/karibu-testing/).
The browserless testing technique has numerous advantages over testing in a browser: you don't have to start the browser,
and you don't have to start Jetty to test your app:

```java
public class MainViewTest {
    @NotNull
    private static final Routes routes = new Routes().autoDiscoverViews("com.example");

    @BeforeAll
    public static void setupApp() {
        // initializes your services
        new Bootstrap().contextInitialized(null);
    }

    @AfterAll
    public static void tearDownApp() {
        // stops your services
        new Bootstrap().contextDestroyed(null);
    }

    @BeforeEach
    public void setupVaadin() {
        // Fakes Vaadin so that you can navigate in your app straight from your test code
        MockVaadin.setup(routes);
    }

    @AfterEach
    public void tearDownVaadin() {
        // Removes all Vaadin fake objects
        MockVaadin.tearDown();
    }

    @Test
    public void smoke() {
        UI.getCurrent().navigate(MainView.class);
        _assertOne(MainView.class);
        assertTrue(Bootstrap.initialized);
        _click(_get(Button.class, spec -> spec.withCaption("Click Me")));
    }
}
```

Please see [Karibu-Testing](https://github.com/mvysny/karibu-testing/) documentation for
further details.

## Walkthrough Guides

The "Creating Vaadin App from scratch" video series:

* [Part 1](https://www.youtube.com/watch?v=vl8Dnh6FIYA)
* [Part 2](https://www.youtube.com/watch?v=0g_kfqECDvk)

## Architectural Tips

The ideas here are taken from [My Favorite Vaadin Architecture](https://mvysny.github.io/my-favorite-vaadin-architecture/)
article.

### Accessing SQL database

The easiest way to access a SQL database is to use [jdbi-orm](https://gitlab.com/mvysny/jdbi-orm)
which provides a full-blown CRUD (Create/Update/Delete) bean editing and to-database mapping.
For a working example please take a look at [jdbi-orm-vaadin-crud-demo](https://github.com/mvysny/jdbi-orm-vaadin-crud-demo).

To initialize the library and use an in-memory database for quick prototyping, create a `Bootstrap` class
as described above, then initialize JDBI there:
```java
@WebListener
public class Bootstrap implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        hikariConfig.setMinimumIdle(0);
        JdbiOrm.setDataSource(new HikariDataSource(hikariConfig));

        jdbi().useHandle(handle -> handle.createUpdate("create table if not exists Person (\n" +
                "                id bigint primary key auto_increment,\n" +
                "                name varchar not null,\n" +
                "                age integer not null,\n" +
                "                dateOfBirth date,\n" +
                "                created timestamp,\n" +
                "                modified timestamp,\n" +
                "                alive boolean,\n" +
                "                maritalStatus varchar" +
                ")").execute());

        System.out.println(Person.dao.findAll());
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        JdbiOrm.destroy();
    }
}
```

To create more tables and entities, please see [jdbi-orm](https://gitlab.com/mvysny/jdbi-orm) documentation.

When the database grows bigger, it's much better to use [flyway](https://flywaydb.org/) database
migration tool to update your database schema automatically on every app startup. Please
see [jdbi-orm-vaadin-crud-demo](https://github.com/mvysny/jdbi-orm-vaadin-crud-demo) for an example.
Generally you should run Flyway on app startup from your `Bootstrap` class, to automatically upgrade the database.
Then, place the migration scripts to the `src/main/resources/db/migration` folder.

Note that the tests will also bootstrap the database. This way you don't have to waste
your time mocking/faking data loading code - instead you can test the application as-is since
you'll have everything up-and-running, including services and the database.

### Services

The Entity DAO should only contain code which makes database queries easier - it should not
contain any business code. For that it's best to create a service layer in the SOA (Service Oriented Architecture) fashion,
thus creating a three-tiered application:

* First tier, the web browser
* Second tier, the server-side business logic
* Third tier, the database, entities, DAOs

The simplest way is to create a `Services` class with a bunch of static getters, each returning
the service instance. The advantages are that the IDE autocompletion works perfectly, the lookup is simple and fast,
and are in full control how the services are instantiated - e.g. you can add a special flag for testing and
then create a different set of services for testing if need be. It's really simple to also create stateful services:

```java
public class MyService {
    public String sayHello() {
        return "Hello, " + Person.dao.findFirst();
    }
}

public class MyStatefulService implements Serializable {
    private Person loggedInUser;
    public void login(String username, String password) {
        final Person person = Person.dao.findByName(username);
        person.verifyPassword(password);
        loggedInUser = person;
    }
}

public class Services {
    public static MyService getMyService() {
        return new MyService();
    }

    public static MyStatefulService getMyStatefulService() {
        MyStatefulService service = VaadinSession.getCurrent().getAttribute(MyStatefulService.class);
        if (service == null) {
            service = new MyStatefulService();
            VaadinSession.getCurrent().setAttribute(MyStatefulService.class, service);
        }
        return service;
    }
}
```

### Security/Authentication/Authorization

See [Vaadin Simple Security](https://github.com/mvysny/vaadin-simple-security) for guides
and example projects on how to add security simply to your Vaadin-Boot-based app.

### Localization/Internationalization (l10n/i18n)

Simply use a Java ResourceBundle-based localization and the static `tr()` function
to localize your apps. Please find more at [Vaadin Localization](https://mvysny.github.io/vaadin-localization/).

### Configuration

To configure your app, simply read a JSON file from `/etc/your-app/config.json` via
[Gson](https://github.com/google/gson) (or a YAML file via [SnakeYAML](https://github.com/snakeyaml/snakeyaml))
directly to a Java bean. If the file doesn't exist, you can notify the user and use a default
config file, or you may throw an exception if the configuration file is required.

You can also use a standard Java validation (or your own `validate()` methods) to validate the values in
the beans. You probably already have Hibernate Validator on your classpath since you're probably using
`BeanValidationBinder` (or validation in jdbi-orm): you can reuse Hibernate Validator to validate your config classes as well:

```java
Validation.buildDefaultValidatorFactory().getValidator().validate(yourConfigBean);
```

You can load the configuration either in your Bootstrap `@WebListener`, or in your `main()`
function, before vaadin-boot runs your app.

## Kotlin

The [Kotlin Programming Language](https://kotlinlang.org/) is quickly gaining popularity,
and fixes many of Java's shortcomings. You can definitely use Kotlin with your Vaadin-Boot-based
apps! Please feel free to add the following libraries to your app:

* [vok-orm](https://github.com/mvysny/vok-orm) builds on `jdbi-orm` and adds first-class support for Kotlin
* [karibu-dsl](https://github.com/mvysny/karibu-dsl) offers structured way of building your Vaadin components and routes.
* [kotlinx.serialization](https://kotlinlang.org/docs/serialization.html) to load the configuration JSON file

Many more example projects:

* Simple one-page-one-button example app: [karibu10-helloworld-application](https://github.com/mvysny/karibu10-helloworld-application)
* A simple database-backed one-page task list app: [vaadin-kotlin-pwa](https://github.com/mvysny/vaadin-kotlin-pwa)
* Two-page app demoing grids and database: [Beverage Buddy VoK](https://github.com/mvysny/beverage-buddy-vok)

### Vaadin-on-Kotlin

If you like [Kotlin](https://kotlinlang.org/) and you like the simplicity of the ideas above,
please use the [Vaadin-on-Kotlin](https://www.vaadinonkotlin.eu/) framework which is based on the ideas above.

# Developing Vaadin-Boot

See [CONTRIBUTING](CONTRIBUTING.md)
