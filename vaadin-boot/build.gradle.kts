plugins {
    `java-library`
}

dependencies {
    api(project(":common"))
    implementation(libs.slf4j.api)

    // Embedded Jetty dependencies.
    // This one is needed to host webapps and perform classpath scanning for annotations
    api(libs.jetty.webapp)
    // This one is required to have websocket/push support.
    implementation(libs.jetty.websocket)

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.junit)
    testImplementation(libs.vaadin.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-boot")
