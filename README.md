# Vaadin Boot

[![Maven Central](https://img.shields.io/maven-central/v/com.github.mvysny.vaadin-boot/vaadin-boot?label=vaadin-boot)](https://central.sonatype.com/artifact/com.github.mvysny.vaadin-boot/vaadin-boot)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mvysny.vaadin-boot/vaadin-boot-tomcat?label=vaadin-boot-tomcat)](https://central.sonatype.com/artifact/com.github.mvysny.vaadin-boot/vaadin-boot-tomcat)

Boots your Vaadin app in embedded Jetty from your `main()` method quickly and easily, **without Spring**.

Tired of complexity of running your app as WAR from your IDE? Tired of using Spring Boot just to start your app in a simple way?
Tired of constant debugging issues? Then this project is for you. From now on, you
can use the free [Intellij Community](https://www.jetbrains.com/idea/download) to develop your projects.

Vaadin Boot is for developers that [prefer to create their own solution, DIY](https://mvysny.github.io/frameworkless-diy/)
exactly to fit their needs. You don't like to start by including a pre-fabricated application framework,
complex and abstract enough to handle hundreds of use-cases.
You don't have hundreds of use-cases: you only have one. You only use what you need.
There lies maximum simplicity which you own, understand and can rely on.

First Principles: you only need a servlet container to run Vaadin apps.

## Using In Your Apps

Vaadin Boot is published in Maven Central; simply add a dependency on it:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.mvysny.vaadin-boot:vaadin-boot:13.6")
}
```
Or Maven:
```xml
<project>
	<dependencies>
		<dependency>
			<groupId>com.github.mvysny.vaadin-boot</groupId>
			<artifactId>vaadin-boot</artifactId>
			<version>13.6</version>
		</dependency>
    </dependencies>
</project>
```

Compatibility chart:

| Vaadin-Boot version | Min Java | Servlet Spec     | Supported Vaadin | Jetty | Tomcat |
|---------------------|----------|------------------|------------------|-------|--------|
| 10.7+               | Java 11+ | javax.servlet    | Vaadin 8-23      | 10.x  | N/A    |
| 11.x (deprecated)   | Java 17+ | jakarta.servlet  | Vaadin 24+       | 11.x  | N/A    |
| 12.x (deprecated)   | Java 17+ | jakarta.servlet  | Vaadin 24+       | 12.x  | N/A    |
| 13.x                | Java 17+ | jakarta.servlet  | Vaadin 24+       | 12.x  | 11.x   |
| 13.6+               | Java 21+ | jakarta.servlet  | Vaadin 24+       | 12.1+ | 11.0+  |

See the [Vaadin-Boot Git Tags](https://github.com/mvysny/vaadin-boot/tags) for the list of
released Vaadin-Boot versions.

Then, add the following `Main` class to your project:
```java
public class Main {
    public static void main(String[] args) throws Exception {
        new VaadinBoot().run();
    }
}
```

Then add an empty file named `src/main/resources/webapp/ROOT` to your project -
Vaadin Boot will then serve static files from this folder.

By default, VaadinBoot listens on all interfaces; call `localhostOnly()` to
only listen on localhost.

### Tomcat

To run your ap with Tomcat, make sure to use Vaadin-Boot 13.0 or higher; then
depend on `vaadin-boot-tomcat` instead on `vaadin-boot`.

**Important**: you need to define your own servlet when running Tomcat, otherwise
you'll get HTTP 404. Simply define this class in your project:

```java
@WebServlet(urlPatterns = "/*")
class MyServlet extends VaadinServlet {}
```

### Jetty vs Tomcat

Both are excellent choices, battle-tested in production. If you have no opinion on this,
just go with Jetty.

### Missing `/src/main/webapp`?

Since we're not packaging to WAR, the `src/main/webapp` folder is ignored. We need to package the `webapp` folder
in a way so that it's served from the jar itself. Indeed, the directory is located at `src/main/resources/webapp`.
Make sure to add an empty file named `src/main/resources/webapp/ROOT` - that will allow Vaadin Boot
to quickly figure out the precise location of your webapp directory in the class loader.

Packaging `webapp` folder to jar file indeed slows down the retrieval of static resources a bit.
However, this is not really a big performance problem since the browsers will cache the static resources;
this also simplifies and hardens Vaadin Boot itself since we don't have to figure out
the webapp folder location based on the app zip file structure or your dev env file system structure.

`webapp` location also doesn't depend on a CWD (current working directory) which is important for Intellij IDEA: in multi-module projects,
IDEA is dumb to insist to setting CWD to the root of the project, not to the module.

### Command-line Args

At the moment it's "Do it yourself". Good start
is to use [Apache commons-cli](https://commons.apache.org/proper/commons-cli/) or
[picocli](https://picocli.info).

However, certain settings can be configured via env variables / jvm args, please see
the *Configuration* section below.

### Scaffolding with AI agents

New Vaadin-Boot apps can be scaffolded quickly by AI agents (such as Claude Code) using
the `vaadin-boot-app` skill from [mvysny/claude-skills](https://codeberg.org/mvysny/claude-skills).
Once the skill is installed, ask your agent to create a new Vaadin-Boot app and it will set up the
Gradle project, `Main` class, `webapp/ROOT` marker and a starter view for you.

### Example Apps

Very basic example apps using Vaadin Boot:

* Vaadin 24, Gradle: [vaadin-boot-example-gradle](https://github.com/mvysny/vaadin-boot-example-gradle)
* Vaadin 24, Maven: [vaadin-boot-example-maven](https://github.com/mvysny/vaadin-boot-example-maven)
* Vaadin 24, Maven, Tomcat: [vaadin-boot-example-maven-tomcat](https://github.com/mvysny/vaadin-boot-example-maven-tomcat)
* Vaadin 23, Gradle: [vaadin-boot-example-gradle](https://github.com/mvysny/vaadin-boot-example-gradle), the v23 branch.
* Vaadin 23, Maven: [vaadin-boot-example-maven](https://github.com/mvysny/vaadin-boot-example-maven), the v23 branch.
* Vaadin 14, Gradle: [vaadin14-boot-example-gradle](https://github.com/mvysny/vaadin14-boot-example-gradle)
* Vaadin 14, Maven: [vaadin14-boot-example-maven](https://github.com/mvysny/vaadin14-boot-example-maven)
* Vaadin 8, Maven: [vaadin8-sampler](https://github.com/mvysny/vaadin8-sampler)

More advanced examples, demoing both security and SQL access:

* Vaadin 24, Gradle: [vaadin-simple-security-example](https://github.com/mvysny/vaadin-simple-security-example)
* All [Karibu-DSL](https://github.com/mvysny/karibu-dsl) example apps run on Vaadin-Boot, too.

## Preparing environment

Please install Java JDK 21 or higher (required by Vaadin-Boot 13.6+; earlier lines have lower requirements, see the compatibility chart above).

Vaadin build requires node.js and npm to build the 'frontend bundle'.
However, that will happen automatically so there's nothing you need to do: Vaadin plugin will automatically download
node.js and npm for you (node.js will be downloaded and run from `$HOME/.vaadin`).

## Running your apps

Grab the sources of your app from the git repository.
To run your app quickly from command-line, without having to run your IDE:

1. Run `./gradlew run` (or `./mvnw -C exec:java` for Maven)
2. Your app will be running on [http://localhost:8080](http://localhost:8080).

To run the app from your IDE (we recommend using [Intellij IDEA](https://www.jetbrains.com/idea/download), the Community edition):

1. Import the project into your IDE
2. Run `./gradle vaadinPrepareFrontend` in the project once (or `./mvnw -C vaadin:prepare-frontend` for Maven), to configure Vaadin paths.
3. Run/Debug the `Main` class as an application (run the `main()` method).
   The app will use npm to download all javascript libraries (may take a long time)
   and will start in development mode.
4. Your app will be running on [http://localhost:8080](http://localhost:8080).

> Gradle tip: Intellij will by default use Gradle to start up your app, which is slower and takes more memory. To
> optimize startup time, head to Intellij *Settings / Build, Execution, Deployment / Build Tools / Gradle* and change
> the "Build and run using" from *Gradle* to *Intellij IDEA*.

When deploying your app to production: see the "Production" chapter below. In short:

1. Build your app in production mode, via `./gradlew clean build -Pvaadin.productionMode` or `./mvnw -C clean package -Pproduction`.
2. The build of your app should produce a zip file; unzip the file and launch the run script.

## Develop with pleasure

We recommend to develop Vaadin Boot apps using an IDE instead of just a plain text editor.
The IDE has huge advantages of providing auto-completion, documentation, access to library sources,
debugging and hot-redeployment etc.
We recommend [Intellij IDEA Community Edition](https://www.jetbrains.com/idea/download): download, install, then import this project into the IDEA.

Open the app in your IDE, and debug the `Main` class as an application (run the `main()` method in debugging mode).
Then, open your browser and hit [http://localhost:8080](http://localhost:8080).

This will activate two things:

* Contrary to what Vaadin says ("Java live reload unavailable"), Vaadin will automatically
  detect changes in your CSS/JavaScript files, will rebuild the JavaScript bundle and will
  reload the page to apply the new values.
* When you do changes in your java files and recompile (Ctrl+F9 in Intellij),
  Java will update classes in your running app. Just press F5 in your browser to reload the page and
  to see your changes. See below on tips to vastly improve the basic hot-redeployment support.

There are lots of pre-existing Vaadin components; you can check out the
[Beverage Buddy](https://github.com/mvysny/beverage-buddy-vok/) example app for more
examples of component usage. You should also read the [full Vaadin documentation](https://vaadin.com/docs/flow/Overview.html).

The browser is a very powerful IDE which can help you debug CSS- and layout-related issue. Take your time and read slowly through the following tutorials, to get acquinted with the browser
developer tools:

* [Chrome Developer Tools tutorial](https://developers.google.com/web/tools/chrome-devtools/)
* [Firefox Developer Tools tutorial](https://developer.mozilla.org/en-US/docs/Tools)

### Advanced HotSwapping

The default Java hot-redeployment is limited to Java method in-body code changes only.
The easiest way to improve is to use [JetBrainsRuntime](https://github.com/JetBrains/JetBrainsRuntime) (or JBR) which
is a Java version modified for better hot-redeployment (to be precise, contains DCEVM patches).
The easiest way to obtain and use JBR is:

* Open your Project Settings in IDEA, then locate the SDK "Edit" button and press it
* Click the upper `+` button, then "Download JDK".
* Select JDK version 21, then the "JetBrains Runtime". Both the basic version and the JCEF version
  work; we recommend the basic version since JCEF version is bigger and therefore takes longer to download, and JCEF is not used by Vaadin apps.
* When running your app from Intellij, make sure to:
  * Run via Intellij instead of via Gradle, see the "Build and run using" tip above
  * Add the following JVM flags: `-XX:+AllowEnhancedClassRedefinition -dcevm`

This will give you pretty awesome hot-redeployment capabilities, but Vaadin will still complain that
"Java live reload unavailable", and won't refresh the browser automatically. To achieve that, the
recommended way is to
follow the [Live Reload](https://vaadin.com/docs/latest/configuration/live-reload/hotswap-agent) documentation and add
the `hotswap-agent.jar` as directed. Alternatively:

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
logger (use it by adding this dependency to your project: `implementation("org.slf4j:slf4j-simple:2.0.6")`.

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

We recommend using [Javalin](https://javalin.io) for simplicity reasons. Use Javalin 5.x — Javalin 6 doesn't support Jetty 12 (which Vaadin-Boot 13+ uses).

Add Javalin to your build script:
```groovy
    implementation("io.javalin:javalin:5.6.5") {
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
        ctx.baseResource = EmptyResource()
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
        assertEquals("Hello!", URI("http://localhost:30123/rest").toURL().readText())
    }
}

// Jetty 12 removed EmptyResource.INSTANCE; supply a minimal stand-in so WebAppContext is happy.
class EmptyResource : Resource() {
    override fun getPath(): Path? = null
    override fun isDirectory(): Boolean = true
    override fun isReadable(): Boolean = true
    override fun getURI(): URI? = null
    override fun getName(): String = "EmptyResource"
    override fun getFileName(): String? = null
    override fun resolve(subUriPath: String?): Resource? = null
}
```

A working version of this test ships with the repo at [`testapp-kotlin/src/test/kotlin/com/example/MyJavalinServletTest.kt`](testapp-kotlin/src/test/kotlin/com/example/MyJavalinServletTest.kt), including imports.

### Adding More Servlets

The simplest way is to add the `@WebServlet` annotation to your servlet - it will be auto-discovered
by Jetty. Please see the Javalin example above for more details.

### Overriding Vaadin Servlet

Simply introduce a class into your project which extends `VaadinServlet`, then add any necessary annotations,
for example:

```java
@WebServlet(name = "myservlet", urlPatterns = {"/*"}, initParams = @WebInitParam(name = "foo", value = "bar"))
class MyServlet extends VaadinServlet {}
```

By default, Vaadin's `ServletDeployer` will auto-register `VaadinServlet` but it will skip
this kind of registration if there's already another servlet inheriting from `VaadinServlet`.

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
    id 'com.vaadin' version '25.1.1' // latest: https://plugins.gradle.org/plugin/com.vaadin
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

We'll use two Maven plugins: the [appassembler-maven-plugin](http://www.mojohaus.org/appassembler/appassembler-maven-plugin/)
to prepare run scripts and the app; and the assembly plugin to create a zip file out of the app.

To configure the assembly plugin, create the `src/main/assembly/zip.xml` file with the following contents:
```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd">
    <id>zip</id>
    <formats>
        <format>zip</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <fileSets>
        <fileSet>
            <directory>target/appassembler</directory>
            <outputDirectory>/</outputDirectory>
            <fileMode>0755</fileMode>
        </fileSet>
    </fileSets>
</assembly>
```

Then configure the plugins in your `pom.xml`:
```xml
<plugins>
    <!-- creates an executable app -->
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>appassembler-maven-plugin</artifactId>
        <version>2.1.0</version>
        <configuration>
            <programs>
                <program>
                    <mainClass>com.vaadin.starter.skeleton.Main</mainClass>
                    <name>app</name>
                </program>
            </programs>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>assemble</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    <!-- creates the "zip" distribution -->
    <plugin>
        <artifactId>maven-assembly-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
            <descriptors>
                <descriptor>src/main/assembly/zip.xml</descriptor>
            </descriptors>
        </configuration>
        <executions>
            <execution>
                <id>make-assembly</id> <!-- this is used for inheritance merges -->
                <phase>package</phase> <!-- append to the packaging phase. -->
                <goals>
                    <goal>single</goal> <!-- goals == mojos -->
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
```

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
* There are JavaScript files in `yourapp.jar/META-INF/VAADIN/webapp/VAADIN/build/` (this applies to Vaadin 23+; for Vaadin 14 the file structure is a bit different)
    * Read more at [Vaadin: The missing guide](https://mvysny.github.io/Vaadin-the-missing-guide/), the "production" mode.

[Jetty is perfectly capable](https://www.eclipse.org/jetty/) of running in production as documented
on the Jetty web page.

### Always-on production mode

If the dev mode isn't working in your dev env, you can enable the 'always-on' production mode:

```java
public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("vaadin.productionMode", "true");
        new VaadinBoot().run();
    }
}
```

Make sure to build your app in production mode first, before starting it.

### Configuration

All configuration options are exposed via a Java API on the `VaadinBoot` class, e.g.
```java
new VaadinBoot().localhostOnly().setPort(8081).run();
```

On top of that, the following Vaadin Boot properties are configurable via environment variables and also Java system properties:

| Vaadin Boot config property | Env variable                | Java system property        | Example Value |
|-----------------------------|-----------------------------|-----------------------------|---------------|
| port                        | SERVER_PORT                 | server.port                 | 18080         |
| listen interface            | SERVER_ADDRESS              | server.address              | localhost     |
| context root                | SERVER_SERVLET_CONTEXT_PATH | server.servlet.context-path | /admin        |

> Note: Vaadin Boot 13.1 and older honored `SERVER_SERVLET_CONTEXT-PATH` instead of `SERVER_SERVLET_CONTEXT_PATH`.

You can not pass the Java system properties to your app run scripts directly, since they will be treated as
program parameters. Instead, pass them via the `JAVA_OPTS` env variable (only works with script created by Gradle):

* Linux: `JAVA_OPTS=-Dserver.port=8082 ./my-app`

#### Vaadin Configuration

The easiest way to pass in [Vaadin Configuration Properties](https://vaadin.com/docs/latest/flow/configuration/properties)
is to set them as Java system properties from your `main()` function, for example:
```java
public static void main(@NotNull String[] args) throws Exception {
    System.setProperty("vaadin.heartbeatInterval", "10");
    new VaadinBoot().run();
}
```
Alternatively you can create a Servlet for your app and pass the parameters
via `@WebInitParam`.

#### Session timeout configuration

Call `VaadinSession.getCurrent().getSession().setMaxInactiveInterval(10 * 60)`
to set the servlet container session timeout to 10 minutes. The best place for this
is from [Vaadin's session initialization listener](https://mvysny.github.io/vaadin-sessioninitlistener/).

Beware: I've seen Jetty not closing the sessions regardless of the setting.
Also, if the HTTP session is invalidated and closed,
the `VaadinSession.addSessionDestroyListener()` aren't called (this is a known Vaadin limitation).
You may need to set Vaadin's `closeIdleSessions` to true; see [Vaadin Session Timeout](https://mvysny.github.io/vaadin-session-timeout/)
for more details.

### Docker

Packaging your apps as docker images is incredibly easy. We use [Docker Multi-stage builds](https://docs.docker.com/build/building/multi-stage/):

* We initialize the build environment and build the app in one docker image;
* We copy the result app to a new image and throw away the build environment completely, to not clutter our production image.

Example `Dockerfile` for a Gradle-based app (requires Docker BuildKit, which is default on modern Docker):
```dockerfile
# The "Build" stage. Copies the entire project into the container, into the /app/ folder, and builds it.
FROM --platform=$BUILDPLATFORM eclipse-temurin:21 AS builder
COPY . /app/
WORKDIR /app/
# Cache mounts reuse Gradle and Vaadin (node/npm) caches across builds — major rebuild speedup.
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/root/.vaadin \
    ./gradlew clean build -Pvaadin.productionMode --no-daemon --info --stacktrace -x test
WORKDIR /app/build/distributions/
RUN tar xvf *.tar
# At this point the unpacked app (run script + jars) sits in /app/build/distributions/<rootProject-name>-<version>/

# The "Run" stage. Start with a clean image, and copy over just the app itself, omitting gradle, npm and any intermediate build files.
FROM eclipse-temurin:21
COPY --from=builder /app/build/distributions/*/ /app/
WORKDIR /app/bin
EXPOSE 8080
# Exec form so that SIGTERM from `docker stop` reaches the JVM and graceful shutdown runs.
ENTRYPOINT ["./app"]
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

To configure your app, simply read a config file from the filesystem, e.g. `/etc/your-app/config.toml`.
We recommend to use [TOML](https://toml.io) file via the [tomlj](https://github.com/tomlj/tomlj) parser,
but any of the following will do as well:

* JSON file from `/etc/your-app/config.json` via [Gson](https://github.com/google/gson)
* YAML file via [SnakeYAML](https://github.com/snakeyaml/snakeyaml)

You read the config file directly to a Java bean. If the file doesn't exist, you can notify the user and use a default
config file, or you may throw an exception if the configuration file is required.

You can also use a standard Java validation (or your own `validate()` methods) to validate the values in
the beans. You probably already have Hibernate Validator on your classpath since you're probably using
`BeanValidationBinder` (or validation in jdbi-orm): you can reuse Hibernate Validator to validate your config classes as well:

```java
Validation.buildDefaultValidatorFactory().getValidator().validate(yourConfigBean);
```

You can load the configuration either in your Bootstrap `@WebListener`, or in your `main()`
function, before vaadin-boot runs your app. If you use the latter way, you can configure Vaadin Boot
itself - the port it's running, the context root, etc. Vaadin Boot will never introduce
config loading itself - every app has different needs, and one unifying solution would lead to terrible complexity.

## Kotlin

The [Kotlin Programming Language](https://kotlinlang.org/) is quickly gaining popularity,
and fixes many of Java's shortcomings. You can definitely use Kotlin with your Vaadin-Boot-based
apps! Please feel free to add the following libraries to your app:

* [vok-orm](https://github.com/mvysny/vok-orm) builds on `jdbi-orm` and adds first-class support for Kotlin
* [karibu-dsl](https://github.com/mvysny/karibu-dsl) offers structured way of building your Vaadin components and routes.
* [kotlinx.serialization](https://kotlinlang.org/docs/serialization.html) to load the configuration JSON file

Many more example projects:

* Simple one-page-one-button example app: [karibu-helloworld-application](https://github.com/mvysny/karibu-helloworld-application)
* A simple database-backed one-page task list app: [vaadin-kotlin-pwa](https://github.com/mvysny/vaadin-kotlin-pwa)
* Two-page app demoing grids and database: [Beverage Buddy VoK](https://github.com/mvysny/beverage-buddy-vok)

### Vaadin-on-Kotlin

If you like [Kotlin](https://kotlinlang.org/) and you like the simplicity of the ideas above,
please use the [Vaadin-on-Kotlin](https://www.vaadinonkotlin.eu/) framework which is based on the ideas above.

## Advanced Stuff

### Running as a systemd service in Linux

Running your Vaadin Boot app as a service in Linux under systemd is very easy. Make sure to build the app in production mode first.
Then, create a new user (for example `myappuser`) which will run the app, and unpack the app into that user home folder.
Then, create a file named `/etc/systemd/system/myapp.service` (replace `myapp` with your app name) with the following contents:

```
[Unit]
Description=MyApp
After=network.target
StartLimitIntervalSec=0

[Service]
Type=simple
Restart=always
RestartSec=1
User=myappuser
ExecStart=/home/myappuser/app/bin/app
# Environment=JAVA_HOME=/home/myappuser/jdks/temurin-21

[Install]
WantedBy=multi-user.target
```

* `ExecStart` points to the shell script running your app. For example, when building `vaadin-boot-example-gradle`,
  the shell script is revealed when you unzip `build/distributions/vaadin-boot-example-gradle.zip`.
* Optionally uncomment the `Environment` line and specify a different JVM for the app.

Done. Run:

* `sudo systemctl start myapp` to start the app
* `sudo systemctl status myapp` to see the app's status and log
* `sudo systemctl enable myapp` to make the app survive machine restart
* `sudo journalctl -u myapp` to see the app's log
* `sudo systemctl stop myapp` to stop the app

# Developing Vaadin-Boot

See [CONTRIBUTING](CONTRIBUTING.md)
