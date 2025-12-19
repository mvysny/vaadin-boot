import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    `maven-publish`
    signing
    kotlin("jvm") version "2.2.21" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

defaultTasks("clean", "build")

allprojects {
    group = "com.github.mvysny.vaadin-boot"
    version = "13.6"

    repositories {
        mavenCentral()
    }
}

subprojects {

    apply {
        plugin("maven-publish")
        plugin("java")
        plugin("org.gradle.signing")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            // to see the exceptions of failed tests in CI console.
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // creates a reusable function which configures proper deployment to Maven Central
    ext["configureMavenCentral"] = { artifactId: String ->

        java {
            withJavadocJar()
            withSourcesJar()
        }

        tasks.withType<Javadoc> {
            isFailOnError = false
        }

        tasks.withType<JavaCompile> {
            options.isDeprecation = true
        }

        publishing {
            publications {
                create("mavenJava", MavenPublication::class.java).apply {
                    groupId = project.group.toString()
                    this.artifactId = artifactId
                    version = project.version.toString()
                    pom {
                        description = "Vaadin Boot: Simple way to run your Vaadin app in embedded Jetty"
                        name = artifactId
                        url = "https://github.com/mvysny/vaadin-boot"
                        licenses {
                            license {
                                name = "The MIT License (MIT)"
                                url = "https://opensource.org/licenses/MIT"
                                distribution = "repo"
                            }
                        }
                        developers {
                            developer {
                                id = "mavi"
                                name = "Martin Vysny"
                                email = "martin@vysny.me"
                            }
                        }
                        scm {
                            url = "https://github.com/mvysny/vaadin-boot"
                        }
                    }

                    from(components["java"])
                }
            }
        }

        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}

nexusPublishing {
    repositories {
        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
