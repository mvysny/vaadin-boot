plugins {
    `java-library`
}

dependencies {
    implementation(libs.slf4j.api)
    api(libs.jetbrains.annotations)

    // opens url in a browser
    implementation(libs.vaadin.open)

    // both Vaadin and vaadin-open depends on commons-io, we can use it too.
    implementation(libs.commons.io)

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.junit)
    testImplementation(libs.vaadin.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("common")
