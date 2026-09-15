# Vaadin Boot — AGENTS.md

## What this is

Boots your Vaadin app in embedded Jetty or Tomcat from your `main()` method quickly and easily, **without Spring**.

First Principles: you only need a servlet container to run Vaadin apps.

## Promises

- **No Spring, no application framework.** A servlet container is all that stands between `main()` and a running Vaadin app.
- **You only use what you need.** Nothing is dragged in for a use case this app doesn't have; a feature does not earn its place by being useful to somebody else.
- **A plain `main()`, in any IDE.** No WAR, no installed app server, no IDE plugin - you develop, debug
  and hot-swap a regular Java app. Enter and Ctrl+C both shut the app down cleanly, and `test/system.rb` gates that on every release.

## Design docs

| File | Owns | Loaded |
|---|---|---|
| `README.md` | the pitch and the user-facing manual | — |
| `CONTRIBUTING.md` | how to run the tests by hand, and the release flow | — |
| `AGENTS.md` (this) | promises, invariants, the module map, conventions, commands | every turn |
| `design/architecture.md` | how the pieces compose — wiring, dependency direction, the boot and discovery flows; normative | lazy |
| `design/decisions.md` | why this and not that — `D_` entries, FAQ-shaped | lazy |
| javadoc | what one symbol does and why it is shaped so | at the symbol |

Every fact lives in exactly one of these; the others link to it.

## Invariants

- **An app depends on exactly one container module.** Both publish `com.github.mvysny.vaadinboot.VaadinBoot`, so both on the classpath is two identically-named classes. See `D_two_container_artifacts`.
- **Every module that boots has an empty `src/main/resources/webapp/ROOT`** (test modules: `src/test/resources/`) — `Env.findWebRoot()` locates the webapp folder by that marker and throws without it.
- **`common/` names no Jetty or Tomcat type.** The seam is the `WebServer` interface; the composition is in `design/architecture.md`.

## Module map

- `common` — the container-agnostic API: configuration, lifecycle, and the `WebServer` seam.
- `vaadin-boot` — Jetty 12 (ee10) behind `VaadinBoot`.
- `vaadin-boot-tomcat` — Tomcat 11 behind the same `VaadinBoot` class name.
- `testapp`, `testapp-tomcat`, `testapp-kotlin`, `testapp-kotlin-tomcat` — minimal runnable apps; the kotlin ones add a Javalin REST servlet.
- `test/` — the Ruby system tests that build, start and shut down each testapp.

## Conventions

- **Java 21+, Vaadin 24+, `jakarta.servlet`.** Older lines are v10–v13 on Java 11/17; the compatibility chart is in `README.md`.
- **External versions live in `gradle/libs.versions.toml`**, never in a subproject's `build.gradle.kts`; the project version is `allprojects { version = }` in the root one, `-SNAPSHOT` meaning unreleased.
- **Every publishable subproject calls `configureMavenCentral(artifactId)`** — the root's extension function wires the sources/javadoc jars, the POM and GPG signing; artifactId = module name, group `com.github.mvysny.vaadin-boot`.
- **A container upgrade is not committed until `./test/system.rb` is green.** The system tests are the release gate.
- **Tests: JUnit 5**, plus Karibu-Testing in the apps; no mocking framework — fakes implement `WebServer`.

## Commands

- `./gradlew` — `clean build` (the default tasks): compile plus every subproject's tests.
- `./gradlew :vaadin-boot:test --tests JettyWebServerTest` — one test class.
- `./gradlew clean build -Pvaadin.productionMode` — production build; what CI runs on push (`.github/workflows/gradle.yml`).
- `./gradlew :testapp:run` — run one testapp locally on port 8080.
- `./test/system.rb` — the system tests; needs Ruby 3.4, runs from any directory. The release gate; CI runs it after the production build.
- `./design/verify_design_tripwires.sh` — the doc-layer tripwire; runs as part of `./gradlew check`, and in its own CI job.

## Skills this project follows

- **Loose ideas live one-per-file in `ideas/`**, deleted once implemented or rejected, any lasting nugget backported to its home in *Design docs*; the `ideas-folder` skill has the procedure.

## Maintenance of this file

Loaded every turn; cap 34 KB, a module's own `AGENTS.md` 10 KB. Over it, in this order:
delete what has no home — status, history, class lists, what the code already says; trim
each line to its fact plus one clause and send the explanation home — why →
`design/decisions.md`, how across symbols → `design/architecture.md`, how in one symbol →
its javadoc, what upstream does → `design/research.md`; only then a module's own
`AGENTS.md`, peripheral modules first, never the core. Never paraphrase a lazy entry into a
line here. `design/verify_design_tripwires.sh` checks the caps and the cites.
