plugins {
    id("com.vaadin")
    id("application")
    kotlin("jvm") version "1.8.0"
}

dependencies {
    implementation(project(":vaadin-boot"))
    implementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    implementation("com.vaadin:vaadin-core:${properties["vaadin_version"]}") {
        afterEvaluate {
            if (vaadin.productionMode) {
                exclude(module = "vaadin-dev-server")
            }
        }
    }

    implementation("io.javalin:javalin:5.3.2") {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v23:${properties["kaributesting_version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties["junit_version"]}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("com.example.Main")
}
