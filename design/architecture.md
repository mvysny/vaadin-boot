# Architecture

How the pieces compose — what no single symbol can say and what would be expensive to overturn:
wiring and dependency direction, the lifecycle / threading / data-flow story, the flows a newcomer
needs, where to start reading. **Normative: the code conforms.** Change this file first, then the
code. Not here: why (`decisions.md` — cite the `D_`), what upstream does (`research.md` — cite the
`R_`), one symbol's behaviour (its javadoc), the module map (`AGENTS.md`). Only the sections with
content; the worked example in each is the ruler. Cap 12 KB — over it, research or javadoc
content has crept in.

---

## Wiring

- Dependencies point one way: `testapp*` → `vaadin-boot` | `vaadin-boot-tomcat` → `common`, whose entire knowledge of a container is the `WebServer` interface — `common`'s own tests run the whole lifecycle against `DummyWebServer` and its `FailsToStart` / `FailsToStop` variants.
- `VaadinBootBase<THIS>` owns the configuration and the lifecycle and holds exactly one `WebServer`, handed in by the subclass constructor (`VaadinBoot extends VaadinBootBase<VaadinBoot>` calling `super(new JettyWebServer())`). The self type is what keeps the fluent setters returning the container's own class.
- The container module's `VaadinBoot` is where container-specific knobs live, and its `WebServer` downcasts the configuration back to it — `JettyWebServer.configure` casts to `VaadinBoot` to read `isUseVirtualThreadsIfAvailable()` and friends. Adding a knob therefore means touching the pair, never `common`.
- Static content and class discovery hang off one probe: `Env.findWebRoot()` resolves `classpath:/webapp/ROOT`, and `findResourcesJarOrFolder` / `findClassesJarOrFolder` derive the jar or the `classes` directories from that URL. The webapp lookup itself never consults the current working directory; the CWD enters only through `Env.isDevelopmentEnvironment`'s `pom.xml` / `build.gradle` sniff and `findClassesJarOrFolder`'s last-resort `target/classes` branch — the branch that breaks when IDEA launches a submodule with the project root as CWD.
- Neither module references app code: servlets and listeners reach the container through annotation scanning, never registration. The one exception is Vaadin's own `@WebListener`, which `TomcatWebServer` adds by name — see its javadoc.

## Flows

**Boot and shutdown** (`VaadinBootBase.run()`, main thread):

1. `start()` copies `Env.isVaadinProductionMode` — decided once at class-load, from `flow-server-production-mode.jar` or from `"productionMode": true` in `flow-build-info.json` — into the `vaadin.productionMode` system property, unless the caller already set it.
2. `server.configure(this)` builds the container's context: web root, classpath scanning, port, host, context root.
3. `server.start()` blocks until every servlet and listener has initialized. It either returns a running server or throws having already stopped itself, which is why nothing here cleans up after it.
4. `onStarted(server)`, then the banner with the URL and the PID — this pair *is* guarded: a throw from either calls `stop()` before rethrowing.
5. A shutdown hook is registered, the browser opens in dev mode, and `main` blocks on `System.in.read()`.
6. Enter → `stop()`. EOF, i.e. no stdin (`./gradlew run`, Docker without a tty) → `server.await()` until Ctrl+C fires the shutdown hook, which calls the same `stop()`. `stop()` is `synchronized` and idempotent, so the two paths cannot race or double-stop.

**Servlet discovery** — the one place the two containers genuinely differ:

- Jetty scans everything, unless `disableClasspathScanning`: `WebAppContext` with `CONTAINER_JAR_PATTERN` = `.*\.jar|.*/classes/.*` and `setConfigurationDiscovered(true)`, so Vaadin's `@WebListener` inside `flow-server.jar` is found alongside the app's own annotations.
- Tomcat scans only the app: `enableClasspathScanning` mounts the app's classes at `WEB-INF/classes` of a virtual WAR and nothing else, so `registerVaadinServletDeployer` adds Vaadin's listener explicitly. An app declares its own `@WebServlet extends VaadinServlet` only for a custom name, init params or an extra servlet.

## Where to start reading

`common/src/main/java/.../VaadinBootBase.java` — `run()` is the whole lifecycle on one screen — then `WebServer`, whose javadoc is the contract each container implements.
