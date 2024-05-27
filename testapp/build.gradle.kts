plugins {
    alias(libs.plugins.vaadin)
    application
}

dependencies {
    implementation(project(":vaadin-boot"))
    implementation(libs.slf4j.simple)
    implementation(libs.vaadin.core) {
        if (vaadin.productionMode) {
            exclude(module = "vaadin-dev-server")
        }
    }

    testImplementation(libs.kaributesting)
    testImplementation(libs.junit.jupiter.engine)
}

application {
    mainClass.set("com.example.Main")
}
