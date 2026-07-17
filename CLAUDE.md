# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

Vaadin Boot is a small library that boots a Vaadin app in an embedded servlet container (Jetty or Tomcat) straight from `main()`, without Spring. It is published to Maven Central as `com.github.mvysny.vaadin-boot:vaadin-boot` (Jetty) and `:vaadin-boot-tomcat` (Tomcat).

Target runtime is Java 21+, Vaadin 24+, `jakarta.servlet`. See the compatibility chart in `README.md` for older lines (v10–v13 on Java 11/17).

## Ideas & their graduation

Loose ideas live one-per-file in `ideas/` (well-named, no index file) and are deleted once implemented or rejected, backporting any lasting nugget first. See the `ideas-folder` skill for the full procedure. This project's durable places for graduated nuggets:

- a contract or gotcha → the relevant **javadoc**.
- a build/release/architecture note → this **`CLAUDE.md`** (or `CONTRIBUTING.md` for the release flow).
- a user-facing note → **`README.md`**.

There is no changelog/ADR file; a "why we rejected X" note rides as a regression-guard comment in the relevant javadoc/code, per the *No history* rule.

## Build & test commands

All builds go through the Gradle wrapper. `defaultTasks` is `clean build`, so a bare `./gradlew` does a full clean build.

- `./gradlew build` — compile + unit tests for every subproject.
- `./gradlew test` — only JUnit 5 tests.
- `./gradlew :vaadin-boot:test --tests JettyWebServerTest` — run a single test class (replace module/class).
- `./gradlew clean build -Pvaadin.productionMode` — production build; the Vaadin Gradle plugin bundles JS in prod mode and packages the `META-INF/VAADIN/config/flow-build-info.json` token with `"productionMode":true` (Gradle does not add the `flow-server-production-mode.jar` marker — that is a Maven-only mechanism). CI uses this flag. Note: `flow-gradle-plugin` 25.2.1 only packages that token into the first-configured Vaadin subproject in this multi-module build, so each testapp re-packages its own cached token — see the `copyProductionToken` task in each testapp's `build.gradle.kts`. This is a workaround for the upstream bug https://github.com/vaadin/flow/issues/24841 and should be removed once that is fixed.
- `./gradlew :testapp:run` — run a test app locally (port 8080). Works for `testapp`, `testapp-tomcat`, `testapp-kotlin`, `testapp-kotlin-tomcat`.
- `./test/system.rb` (runnable from any directory) — **system tests**. Builds each testapp in production mode, unzips the distribution, starts it, asserts `http://localhost:8080` returns a Vaadin index, asserts REST endpoints (for the kotlin apps), then verifies the app shuts down cleanly on both Enter and Ctrl+C. Requires Ruby 3.4. This is the release gate.

Publishing (maintainer only): `./gradlew clean build publish closeAndReleaseStagingRepositories`. Full release flow — version bump, tag, publish — is in `CONTRIBUTING.md`.

## Architecture

Three production modules plus four test apps. The split exists so Jetty and Tomcat are swappable without dragging in both containers.

- **`common/`** — container-agnostic API. Key types:
  - `VaadinBootBase<THIS>` — the fluent configuration/lifecycle object. Owns port, host, context root, browser-on-start behavior. Reads `SERVER_PORT` / `server.port`, `SERVER_ADDRESS` / `server.address`, `SERVER_SERVLET_CONTEXT_PATH` / `server.servlet.context-path` via `Env`.
  - `WebServer` — the interface each container implements. Lifecycle contract is documented on the interface: `configure` → `start` → `await` → `stop`, called from a single thread. Implementations must serve `classpath://webapp` static content, support WebSockets, and auto-discover `@WebServlet`/`@WebListener` from at least the main app jar. No `web.xml`, no JSP.
  - `Env`, `Util` — env/system-property lookup and small helpers.
- **`vaadin-boot/`** — Jetty 12 (ee10) implementation. Exposes `com.github.mvysny.vaadinboot.VaadinBoot` which extends `VaadinBootBase` and wires in `JettyWebServer`. Adds Jetty-specific knobs: `disableClasspathScanning`, `scanTestClasspath`, `useVirtualThreadsIfAvailable` (virtual threads auto-enable on JDK 21+).
- **`vaadin-boot-tomcat/`** — Tomcat 11 implementation. Same `com.github.mvysny.vaadinboot.VaadinBoot` class name but backed by `TomcatWebServer`. `VaadinServlet` is auto-registered like on Jetty: embedded Tomcat scans only the app's own classes (mounted at `WEB-INF/classes`), not `flow-server.jar`, so `TomcatWebServer.registerVaadinServletDeployer` explicitly registers Vaadin's `@WebListener` `ServletContextListeners` (which runs `ServletDeployer` after the `Lookup` SCI). An app still declares its own `@WebServlet extends VaadinServlet` only for a custom name/init-params or extra servlets.
- **`testapp/`, `testapp-tomcat/`, `testapp-kotlin/`, `testapp-kotlin-tomcat/`** — minimal runnable apps used by `test/system.rb`. They depend on `:vaadin-boot` or `:vaadin-boot-tomcat` via project references, apply the Vaadin Gradle plugin, and use the Application plugin to produce a zip distribution. The kotlin variants additionally register a Javalin REST servlet, which the system test hits.

Because both container modules ship a class literally named `com.github.mvysny.vaadinboot.VaadinBoot`, **an app must depend on exactly one of them** — mixing both puts two identically-named classes on the classpath.

The `webapp` directory lives at `src/main/resources/webapp` (not `src/main/webapp`, because we don't build a WAR). An empty marker file `src/main/resources/webapp/ROOT` must exist so the boot code can locate the folder inside the packaged jar. The test modules all contain this marker under `src/test/resources/webapp/ROOT`.

## Versioning & dependencies

- Version is declared in the root `build.gradle.kts` under `allprojects { version = ... }`. `-SNAPSHOT` suffix indicates unreleased.
- All external library versions are centralized in `gradle/libs.versions.toml` (Gradle version catalog). Jetty, Tomcat, Vaadin, SLF4J, JUnit, Karibu-Testing, Javalin are pinned there — update the catalog, not individual `build.gradle.kts` files.
- Root `build.gradle.kts` defines a reusable `configureMavenCentral(artifactId)` extension function that every publishable subproject calls to wire up sources/javadoc jars, POM metadata, and GPG signing.
- After upgrading Tomcat or Jetty, run the full test suite: `./gradlew build` followed by `./test/system.rb`. The system tests are the release gate and must pass before committing a container upgrade.
