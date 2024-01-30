plugins {
    `java-library`
}

dependencies {
    implementation(libs.slf4j.api)
    api(libs.jetbrains.annotations)

    // Embedded Jetty dependencies.
    // This one is needed to host webapps and perform classpath scanning for annotations
    api(libs.jetty.webapp)
    // This one is required to have websocket/push support.
    implementation(libs.jetty.websocket)

    // opens url in a browser
    implementation(libs.vaadin.open)

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.vaadin.core)
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-boot")
