plugins {
    `java-library`
}

dependencies {
    implementation("org.slf4j:slf4j-api:${properties["slf4j_version"]}")
    implementation("org.jetbrains:annotations:22.0.0")

    // Embedded Jetty dependencies
    api("org.eclipse.jetty:jetty-webapp:${properties["jetty_version"]}")
    api("org.eclipse.jetty.websocket:websocket-jetty-server:${properties["jetty_version"]}")

    // opens url in a browser; Vaadin dependency
    implementation("com.vaadin:open:8.5.0")

    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties["junit_version"]}")
    testImplementation("com.vaadin:vaadin-core:${properties["vaadin_version"]}") {
        exclude(module = "javax.annotation-api")
    }
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-boot")
