rootProject.name = "vaadin-boot"
include(
	"vaadin-boot",
	"testapp",
	"testapp-kotlin"
)
pluginManagement { // remove when Vaadin 24 is released
	repositories {
		maven { setUrl("https://maven.vaadin.com/vaadin-prereleases") }
		gradlePluginPortal()
	}
}
