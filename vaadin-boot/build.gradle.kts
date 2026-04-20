plugins {
    `java-library`
}

dependencies {
    api(project(":common"))
    implementation(libs.slf4j.api)

    // Embedded Jetty dependencies.
    // This one is needed to host webapps and perform classpath scanning for annotations
    api(libs.jetty.webapp)
    // This one is required to have websocket/push support (Jakarta flavor, used by Vaadin).
    implementation(libs.jetty.websocket)
    // Jetty-flavor WebSocket API, so that embedded apps (e.g. Javalin) can register Jetty-style
    // WebSockets. Also satisfies the SCI in jetty-ee10-websocket-jetty-server that scans for
    // org.eclipse.jetty.websocket.api.WebSocketContainer at Jetty startup.
    implementation(libs.jetty.websocket.jetty)

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.junit)
    testImplementation(libs.vaadin.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-boot")
