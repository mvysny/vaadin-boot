import com.vaadin.gradle.getBooleanProperty

plugins {
    id("com.vaadin")
    application
    kotlin("jvm") version "1.9.22"
}

dependencies {
    implementation(project(":vaadin-boot"))
    implementation(libs.slf4j.simple)
    implementation(libs.vaadin.core) {
        // https://github.com/vaadin/flow/issues/18572
        if (vaadin.productionMode.map { v -> getBooleanProperty("vaadin.productionMode") ?: v }.get()) {
            exclude(module = "vaadin-dev")
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
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass = "com.example.Main"
}
