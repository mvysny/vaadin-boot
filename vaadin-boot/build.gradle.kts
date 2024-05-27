plugins {
    `java-library`
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.jetbrains.annotations)

    // Embedded Jetty dependencies
    api(libs.jetty.webapp)
    api(libs.jetty.websocket)

    // opens url in a browser; Vaadin dependency
    implementation(libs.vaadin.open)

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.vaadin.core) {
        exclude(module = "javax.annotation-api")
    }
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-boot")
