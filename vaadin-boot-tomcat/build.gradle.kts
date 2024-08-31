plugins {
    `java-library`
}

dependencies {
    implementation(libs.slf4j.api)
    api(libs.jetbrains.annotations)

    // Embedded Tomcat dependencies.
    api(libs.tomcat.core)
    // This one is required to have websocket/push support.
    implementation(libs.tomcat.websocket)
    // we need to include basic support for jsp otherwise Tomcat would complain
    implementation(libs.tomcat.jasper) {
        // but we can exclude the jsp compiler and all other unnecessary stuff
    	exclude(module = "ecj")
        exclude(module = "tomcat-embed-el")
    }

    // opens url in a browser
    implementation(libs.vaadin.open)

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.junit)
    testImplementation(libs.vaadin.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

@Suppress("UNCHECKED_CAST")
val configureMavenCentral = ext["configureMavenCentral"] as (artifactId: String) -> Unit
configureMavenCentral("vaadin-boot-tomcat")
