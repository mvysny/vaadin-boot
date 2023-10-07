plugins {
    id("com.vaadin")
    id("application")
}

dependencies {
    implementation(project(":vaadin-boot")) {
        // we are not using Push, therefore we can exclude the websocket jars, to significantly decrease the app zip file size
        exclude(module = "jetty-ee10-websocket-jakarta-server")
    }
    implementation("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    implementation("com.vaadin:vaadin-core:${properties["vaadin_version"]}") {
        afterEvaluate {
            if (vaadin.productionMode) {
                exclude(module = "vaadin-dev")
            }
        }
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v23:${properties["kaributesting_version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${properties["junit_version"]}")
}

application {
    mainClass.set("com.example.Main")
}
