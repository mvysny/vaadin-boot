import com.vaadin.gradle.getBooleanProperty

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
    implementation(libs.vaadin.core) {
        // https://github.com/vaadin/flow/issues/18572
        if (vaadin.productionMode.map { v -> getBooleanProperty("vaadin.productionMode") ?: v }.get()) {
            exclude(module = "vaadin-dev")
        }
    }

    testImplementation(libs.kaributesting)
    testImplementation(libs.junit.jupiter.engine)
}

application {
    mainClass = "com.example.Main"
}
