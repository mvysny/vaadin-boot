val vaadin_version: String by extra
val slf4j_version: String by extra
val junit_version: String by extra
val jetty_version: String by extra

plugins {
    `java-library`
}

dependencies {
    implementation("org.slf4j:slf4j-api:$slf4j_version")
    api("org.jetbrains:annotations:24.0.1")

    // Embedded Jetty dependencies.
    // This one is needed to host webapps and perform classpath scanning for annotations
    api("org.eclipse.jetty.ee10:jetty-ee10-annotations:$jetty_version")
    // This one is required to have websocket/push support.
    implementation("org.eclipse.jetty.ee10.websocket:jetty-ee10-websocket-jakarta-server:$jetty_version")

    // opens url in a browser; Vaadin dependency
    implementation("com.vaadin:open:8.5.0")

    testImplementation("org.slf4j:slf4j-simple:$slf4j_version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit_version")
    testImplementation("com.vaadin:vaadin-core:$vaadin_version")
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-boot")
