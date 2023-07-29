// workaround for https://github.com/gradle/gradle/issues/9830
pluginManagement {
    val vaadin_version: String by settings
    plugins {
        id("com.vaadin") version vaadin_version
    }
}
include(
	"vaadin-boot",
	"testapp",
	"testapp-kotlin"
)

