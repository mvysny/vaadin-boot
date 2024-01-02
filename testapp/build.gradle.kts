val vaadin_version: String by extra
val slf4j_version: String by extra
val junit_version: String by extra
val kaributesting_version: String by extra

plugins {
    id("com.vaadin")
    application
}

dependencies {
    implementation(project(":vaadin-boot")) {
        // we are not using Push, therefore we can exclude the websocket jars, to significantly decrease the app zip file size
        exclude(module = "jetty-ee10-websocket-jakarta-server")
    }
    implementation("org.slf4j:slf4j-simple:$slf4j_version")
    implementation("com.vaadin:vaadin-core:$vaadin_version") {
        afterEvaluate {
            if (vaadin.productionMode.get()) {
                exclude(module = "vaadin-dev")
            }
        }
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v23:$kaributesting_version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit_version")
}

application {
    mainClass = "com.example.Main"
}
