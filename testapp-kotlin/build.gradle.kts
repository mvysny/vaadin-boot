plugins {
    alias(libs.plugins.vaadin)
    application
    kotlin("jvm") version "1.9.23"
}

dependencies {
    implementation(project(":vaadin-boot"))
    implementation(libs.slf4j.simple)
    implementation(libs.vaadin.core) {
        if (vaadin.productionMode) {
            exclude(module = "vaadin-dev-server")
        }
    }

    implementation(libs.javalin) {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }

    testImplementation(libs.kaributesting)
    testImplementation(libs.junit.jupiter.engine)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("com.example.Main")
}
