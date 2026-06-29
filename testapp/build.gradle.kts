plugins {
    alias(libs.plugins.vaadin)
    application
}

dependencies {
    implementation(project(":vaadin-boot")) {
        // we are not using Push, therefore we can exclude the websocket jars, to significantly decrease the app zip file size
        exclude(module = "jetty-ee10-websocket-jakarta-server")
    }
    implementation(libs.slf4j.simple)
    implementation(libs.vaadin.core)
    if (!vaadin.effective.productionMode.get()) {
        implementation(libs.vaadin.dev)
    }

    testImplementation(libs.karibu.testing)
    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// In production mode the app must ship META-INF/VAADIN/config/flow-build-info.json with
// "productionMode": true, otherwise Vaadin Boot boots in dev mode and fails (no dev bundle).
// flow-gradle-plugin 25.2.1 only packages that token into the FIRST Vaadin subproject's jar
// in a multi-module build (its 'vaadinBuildFrontendToken' BuildService uses a build-global
// name), so we package this module's own cached token explicitly onto both the jar (for the
// distribution) and the test runtime classpath (the boot tests run from the exploded build).
// Remove this once https://github.com/vaadin/flow/issues/24841 is fixed upstream.
if (vaadin.effective.productionMode.get()) {
    val copyProductionToken = tasks.register<Copy>("copyProductionToken") {
        dependsOn("vaadinBuildFrontend")
        from(layout.buildDirectory.file("cached-flow-build-info.json")) {
            into("META-INF/VAADIN/config")
            rename { "flow-build-info.json" }
        }
        into(layout.buildDirectory.dir("generated/production-token"))
    }
    val productionTokenDir = layout.buildDirectory.dir("generated/production-token")
    tasks.named<Jar>("jar") {
        dependsOn(copyProductionToken)
        from(productionTokenDir)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    tasks.named<Test>("test") {
        dependsOn(copyProductionToken)
        classpath += files(productionTokenDir)
    }
}

// Lets the boot tests assert the app actually runs in the mode the build selected.
tasks.withType<Test> {
    systemProperty("expectedVaadinProductionMode", vaadin.effective.productionMode.get().toString())
}

application {
    mainClass = "com.example.Main"
}
