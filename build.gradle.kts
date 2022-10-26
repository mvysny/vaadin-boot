import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java")
}

defaultTasks("clean", "build")

// don't use Jetty 11+ - uses jakarta.Servlet instead of javax.Servlet which makes it incompatible with Vaadin
// Jetty 10+ requires Java 11+
val jettyVersion = "10.0.11"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("org.jetbrains:annotations:22.0.0")

    // Embedded Jetty dependencies
    implementation("org.eclipse.jetty:jetty-webapp:${jettyVersion}")
    implementation("org.eclipse.jetty.websocket:websocket-javax-server:${jettyVersion}")

    testImplementation("org.slf4j:slf4j-simple:2.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("com.vaadin:vaadin-core:23.2.6") {
        exclude(module = "javax.annotation-api")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // to see the exception stacktraces of failed tests in CI
        exceptionFormat = TestExceptionFormat.FULL
    }
}
