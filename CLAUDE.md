# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

Vaadin Boot is a small library that boots a Vaadin app in an embedded servlet container (Jetty or Tomcat) straight from `main()`, without Spring. It is published to Maven Central as `com.github.mvysny.vaadin-boot:vaadin-boot` (Jetty) and `:vaadin-boot-tomcat` (Tomcat).

Target runtime is Java 21+, Vaadin 24+, `jakarta.servlet`. See the compatibility chart in `README.md` for older lines (v10–v13 on Java 11/17).

## Build & test commands

All builds go through the Gradle wrapper. `defaultTasks` is `clean build`, so a bare `./gradlew` does a full clean build.

- `./gradlew build` — compile + unit tests for every subproject.
- `./gradlew test` — only JUnit 5 tests.
- `./gradlew :vaadin-boot:test --tests JettyWebServerTest` — run a single test class (replace module/class).
- `./gradlew clean build -Pvaadin.productionMode` — production build; the Vaadin Gradle plugin bundles JS in prod mode and includes `flow-server-production-mode.jar`. CI uses this flag.
- `./gradlew :testapp:run` — run a test app locally (port 8080). Works for `testapp`, `testapp-tomcat`, `testapp-kotlin`, `testapp-kotlin-tomcat`.
- `cd test && ./system.rb` — **system tests**. Builds each testapp in production mode, unzips the distribution, starts it, asserts `http://localhost:8080` returns a Vaadin index, asserts REST endpoints (for the kotlin apps), then verifies the app shuts down cleanly on both Enter and Ctrl+C. Requires Ruby 3.4. This is the release gate.

Publishing (maintainer only): `./gradlew clean build publish closeAndReleaseStagingRepositories`. Full release flow — version bump, tag, publish — is in `CONTRIBUTING.md`.

## Architecture

Three production modules plus four test apps. The split exists so Jetty and Tomcat are swappable without dragging in both containers.

- **`common/`** — container-agnostic API. Key types:
  - `VaadinBootBase<THIS>` — the fluent configuration/lifecycle object. Owns port, host, context root, browser-on-start behavior. Reads `SERVER_PORT` / `server.port`, `SERVER_ADDRESS` / `server.address`, `SERVER_SERVLET_CONTEXT_PATH` / `server.servlet.context-path` via `Env`.
  - `WebServer` — the interface each container implements. Lifecycle contract is documented on the interface: `configure` → `start` → `await` → `stop`, called from a single thread. Implementations must serve `classpath://webapp` static content, support WebSockets, and auto-discover `@WebServlet`/`@WebListener` from at least the main app jar. No `web.xml`, no JSP.
  - `Env`, `Util` — env/system-property lookup and small helpers.
- **`vaadin-boot/`** — Jetty 12 (ee10) implementation. Exposes `com.github.mvysny.vaadinboot.VaadinBoot` which extends `VaadinBootBase` and wires in `JettyWebServer`. Adds Jetty-specific knobs: `disableClasspathScanning`, `scanTestClasspath`, `useVirtualThreadsIfAvailable` (virtual threads auto-enable on JDK 21+).
- **`vaadin-boot-tomcat/`** — Tomcat 11 implementation. Same `com.github.mvysny.vaadinboot.VaadinBoot` class name but backed by `TomcatWebServer`. Tomcat users must declare their own `@WebServlet(urlPatterns = "/*") class MyServlet extends VaadinServlet {}` — Tomcat won't auto-register `VaadinServlet` the way Jetty's `ServletDeployer` does.
- **`testapp/`, `testapp-tomcat/`, `testapp-kotlin/`, `testapp-kotlin-tomcat/`** — minimal runnable apps used by `test/system.rb`. They depend on `:vaadin-boot` or `:vaadin-boot-tomcat` via project references, apply the Vaadin Gradle plugin, and use the Application plugin to produce a zip distribution. The kotlin variants additionally register a Javalin REST servlet, which the system test hits.

Because both container modules ship a class literally named `com.github.mvysny.vaadinboot.VaadinBoot`, **an app must depend on exactly one of them** — mixing both puts two identically-named classes on the classpath.

The `webapp` directory lives at `src/main/resources/webapp` (not `src/main/webapp`, because we don't build a WAR). An empty marker file `src/main/resources/webapp/ROOT` must exist so the boot code can locate the folder inside the packaged jar. The test modules all contain this marker under `src/test/resources/webapp/ROOT`.

## Versioning & dependencies

- Version is declared in the root `build.gradle.kts` under `allprojects { version = ... }`. `-SNAPSHOT` suffix indicates unreleased.
- All external library versions are centralized in `gradle/libs.versions.toml` (Gradle version catalog). Jetty, Tomcat, Vaadin, SLF4J, JUnit, Karibu-Testing, Javalin are pinned there — update the catalog, not individual `build.gradle.kts` files.
- Root `build.gradle.kts` defines a reusable `configureMavenCentral(artifactId)` extension function that every publishable subproject calls to wire up sources/javadoc jars, POM metadata, and GPG signing.
