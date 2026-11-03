# A first-class AI dev & test loop for Vaadin Boot apps

Goal: an agent (or a human) edits a Vaadin Boot app and gets an authoritative, machine-readable
answer to *"is my last change live, and does it work?"* — without rebuilding, relaunching and
guessing from screenshots. Then it verifies: Karibu for logic, a browser for what renders,
`test/system.rb` for boot/shutdown.

## What upstream merged: vaadin/flow#25376 "feat: vaadin-dev dev loop"

Merged 2026-09-02, ships with **Vaadin 25.3** (we are on 25.3.5). Pieces:

- **`flow-devloop-daemon`** — a zero-dependency daemon JVM that *owns the app process*:
  change detection over every reactor module the app depends on, in-process `javac`, a
  transaction engine, a loopback RPC for the CLI. The same jar is a `-javaagent` that hands
  out `Instrumentation`; HotswapAgent + a JBR (`-XX:+AllowEnhancedClassRedefinition`) are
  attached too.
- **In-app connector** in `vaadin-dev-server` (`com.vaadin.base.devserver.devloop`) — a
  `VaadinServiceInitListener` that, only when `-Dvaadin.devloop.daemonPort` / `.token` are set,
  dials the daemon and holds one socket for the app's lifetime (its close = app died). Serves
  `REDEFINE`, `RESOURCES`, `THEME`, `RELOAD`, `FRONTEND`, `INFO`. Suspends Flow's own
  save-triggered watchers so exactly one command decides when a change goes live.
- **`.vaadin/vaadin-dev` CLI + agent skills**, installed by `mvn vaadin:install-dev-cli` into
  `.vaadin/`, `.agents/skills/`, `.claude/skills/`, committed like `mvnw`.
- Verbs: `status`, `start`, `apply`, `restart`, `stop`, `shutdown`. **Exit code is the verdict**:
  `0` live, `1` failed, `4` superseded. "Never over-claim": JPA mappings, proxied beans,
  changed `@JsModule`/`@CssImport`, `application.properties`, deleted classes escalate to a
  restart rather than report `Stable`; app-log errors after the change make it not-green.

Follow-ups worth knowing: #25851 (runtimes: WARs via `jetty:run`, WildFly, TomEE, Payara,
Liberty, Cargo/Tomcat — the daemon picks `main` vs. a Maven goal in `AppRuntime`), #26203
(opt-in `-Dvaadin.dev.compiler=maven`, honours annotation processors), #26299
(`--jvm-args`). Open: #26508, #26782, #26833. Known limit: callers of a re-signed method are
not recompiled (javac compiles only the change-set) — recompile with Maven after an API edit.

## Is it Spring-Boot-only? No. Is it Maven-only? Yes.

- **Spring is soft.** `MainClass` tries `-Dvaadin.dev.mainClass`, then a packaged jar's
  `Start-Class`/`Main-Class`, then `@SpringBootApplication`, then *any*
  `public static void main` in `target/classes`. The Spring checks in the connector are
  name-based (no Spring on the classpath). An entry point is launched as plain
  `java -cp <resolved classpath> <MainClass>` (`MainClassRuntime`) — exactly a Vaadin Boot app.
- **Maven is hard.** `Reactor`/`EffectiveModel` read `pom.xml`s with the JDK XML parser; the
  classpath comes from `mvnw compile dependency:build-classpath`; outputs are assumed at
  `target/classes`; state lives in `target/devloop/`; install is a Maven goal. No Gradle
  support upstream, and I found no issue asking for it.

So: **a Maven-built Vaadin Boot app on Vaadin 25.3+ should work as-is** (unverified — see
`Q_smoke_test`). **A Gradle one — every testapp here, and Gradle is Vaadin Boot's default —
cannot.**

## Frictions specific to Vaadin Boot (on the Maven path)

- `Q_serving_line`: the daemon decides "server is listening" from the app log, regex
  `(?i)(tomcat|jetty|netty|undertow).{0,40}started on port|(?i)started \S+ in \d+([.,]\d+)? second`
  (`AppLog.SERVING`). Vaadin Boot prints `Started in PT1.2S. Running on …`, and neither
  embedded Jetty nor Tomcat logs a matching line — so every `start`/`restart` falls back to
  "registered; the app logged no server port" after the settle window
  (`vaadin.dev.startSettleMillis`, default 15 s). Cheap fix on our side: also print
  `"<Jetty|Tomcat> started on port <n>"` from `VaadinBootBase.start()` (`server.getName()` is
  already there). `test/system.rb` doesn't grep the banner, so nothing breaks. Measure first.
- `Q_servlet_init`: the connector registers from `serviceInit`, i.e. when the `VaadinServlet`
  initialises. If Vaadin Boot's servlet initialises lazily (first request) the app never
  registers until someone opens the page and `start` waits up to `STARTUP_TIMEOUT` (5 min).
  Check: does Flow's `ServletDeployer` set `loadOnStartup`? Does a user `@WebServlet` without
  `loadOnStartup` (testapp-kotlin's `MyServlet`) init lazily under Jetty 12 / Tomcat 11?
- `Q_port`: Vaadin Boot reads `SERVER_PORT` / `-Dserver.port`. The daemon forwards only
  `vaadin.*` and `spring.*` daemon properties to the app, so `-Dserver.port` must go via
  `vaadin-dev start --jvm-args "-Dserver.port=8081"` (or the env var, if it is inherited —
  verify). Documentation, not code.
- Shutdown promise: the app runs with a piped, never-closed stdin, so `System.in.read()` blocks
  rather than returning -1 — fine. `stop` presumably `destroy()`s → SIGTERM → our shutdown
  hook. Verify that `vaadin-dev stop` shuts Jetty/Tomcat down cleanly (no orphaned port).
- `Env.findWebRoot()` needs `webapp/ROOT` on the classpath; under `java -cp target/classes:…`
  it is there. Should be a non-issue; confirm in the smoke test.
- The connector lives in `vaadin-dev-server`, which a Vaadin Boot app gets via `vaadin-dev`
  in dev mode (as our testapps do). An app that only has `vaadin-core` has no connector.

## Gradle: options

- **A. Ask upstream for Gradle.** The daemon's Maven surface is `Reactor`, `EffectiveModel`,
  `Launch`'s classpath resolve and the install goal; a Gradle equivalent needs an init script
  or a tooling-API dump of source sets, output dirs and runtime classpath, plus
  `vaadin { installDevCli }` in `flow-gradle-plugin`. File an issue on vaadin/flow; Vaadin Boot
  is a good motivating user, since Gradle is our default.
- **B. Roll our own loop for Gradle.** Drive the same connector protocol from a Gradle-side
  daemon. Rejected in advance: the protocol and daemon are documented as "internal use only, may
  be renamed or removed", and a loop is a big thing to own — against "you only use what you
  need".
- **C. Interim: document "want `vaadin-dev`? build with Maven"** in the README, and point to a
  Maven example project (mvysny/vaadin-boot-example-maven).

Lean: C now, A as an upstream issue, never B. `Q_gradle`: does anyone actually need Gradle here
badly enough to push A?

## The testing half of the lifecycle

- **Karibu-Testing** (already in the testapps) — fast, browserless, the agent's default proof
  for logic and component wiring.
- **`vaadin-dev apply`** — proof the bytes are live, with a verdict instead of a guess.
- **Browser (Playwright MCP)** — proof it renders; the devloop skill says to open the page
  *before* `apply` so CSS/theme pushes have somewhere to land.
- **`test/system.rb`** — boot, serve, Enter/Ctrl+C shutdown. The release gate.

`Q_maven_testapp`: should this repo gain a Maven-built testapp (consuming the Gradle-built
`vaadin-boot` via `publishToMavenLocal`) so `system.rb` can gate "`vaadin-dev start` → edit →
`apply` exits 0 → `stop` frees the port" on every release? It would be the only Maven corner of
a Gradle build; the alternative is a manual smoke test in `CONTRIBUTING.md` before each
release.

`Q_smoke_test`: before any code, try it by hand on vaadin-boot-example-maven bumped to Vaadin
25.3.5: `mvn vaadin:install-dev-cli`, `.vaadin/vaadin-dev start`, time it (that measures
`Q_serving_line`), edit a view, `apply`, check exit code and browser, `stop`. Also: does
`install-dev-cli` work without `vaadin-maven-plugin` configured beyond `prepare-frontend`?

`Q_skill`: the user-scope `vaadin-boot-app` skill should tell the agent to use `vaadin-dev` for
Maven apps on 25.3+ and to fall back to `./gradlew run` + Karibu + browser for Gradle ones.
That lives outside this repo.

## When this graduates

- How to use `vaadin-dev` with Vaadin Boot (Maven only, 25.3+, `--jvm-args` for the port) →
  `README.md`.
- Why no Gradle loop of our own (option B) → a `D_` in `design/decisions.md`.
- The banner change, if made → javadoc on `VaadinBootBase.start()` saying the
  `"… started on port"` line is read by `vaadin-dev`.
- A new release gate, if made → `test/system.rb` + `AGENTS.md` Commands; else a manual step
  in `CONTRIBUTING.md`.
