import com.vaadin.gradle.getBooleanProperty

plugins {
    alias(libs.plugins.vaadin)
    application
}

dependencies {
    implementation(project(":vaadin-boot-tomcat"))
    implementation(libs.slf4j.simple)
    implementation(libs.vaadin.core) {
        if (vaadin.effective.productionMode.get()) {
            exclude(module = "vaadin-dev")
        }
    }

    testImplementation(libs.kaributesting)
    testImplementation(libs.junit)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "com.example.Main"
}
