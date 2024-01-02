val vaadin_version: String by extra
val slf4j_version: String by extra
val junit_version: String by extra
val kaributesting_version: String by extra

plugins {
    id("com.vaadin")
    application
    kotlin("jvm") version "1.9.22"
}

dependencies {
    implementation(project(":vaadin-boot"))
    implementation("org.slf4j:slf4j-simple:$slf4j_version")
    implementation("com.vaadin:vaadin-core:$vaadin_version") {
        afterEvaluate {
            if (vaadin.productionMode.get()) {
                exclude(module = "vaadin-dev")
            }
        }
    }

    implementation("io.javalin:javalin:5.6.1") {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v23:$kaributesting_version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit_version")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass = "com.example.Main"
}
