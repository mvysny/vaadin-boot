dependencies {
    implementation("org.slf4j:slf4j-api:${properties["slf4j_version"]}")
    implementation("org.jetbrains:annotations:22.0.0")

    // Embedded Jetty dependencies
    implementation("org.eclipse.jetty:jetty-webapp:${properties["jetty_version"]}")
    implementation("org.eclipse.jetty.websocket:websocket-javax-server:${properties["jetty_version"]}")

    testImplementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("com.vaadin:vaadin-core:${properties["vaadin_version"]}") {
        exclude(module = "javax.annotation-api")
    }
}

val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("karibu-dsl")
