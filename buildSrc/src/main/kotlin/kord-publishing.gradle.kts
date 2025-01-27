import java.util.*

plugins {
    `maven-publish`
    signing
}

fun MavenPublication.addDokkaIfNeeded() {
    if (tasks.findByName("dokkaHtml") != null) {
        val platform = name.substringAfterLast('-')
        val dokkaJar = tasks.register("${platform}DokkaJar", Jar::class) {
            dependsOn("dokkaHtml")
            archiveClassifier.set("javadoc")
            destinationDirectory.set(buildDir.resolve(platform))
            from(tasks.getByName("dokkaHtml"))
        }
        artifact(dokkaJar)
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            addDokkaIfNeeded()
            groupId = Library.group
            artifactId = "cache-${artifactId}"
            version = Library.version

            pom {
                name.set(Library.name)
                description.set(Library.description)
                url.set(Library.projectUrl)

                organization {
                    name.set("Kord")
                    url.set("https://github.com/kordlib")
                }

                developers {
                    developer {
                        name.set("The Kord Team")
                    }
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/kordlib/kord/issues")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("http://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/kordlib/kord.git")
                    developerConnection.set("scm:git:ssh://git@github.com:kordlib/kord.git")
                    url.set(Library.projectUrl)
                }
            }

        }

        if (System.getenv("CI") == null) {
            repositories {
                mavenLocal()
            }
        } else if (!isJitPack) {
            repositories {
                maven {
                    url = uri(if (Library.isSnapshot) Repo.snapshotsUrl else Repo.releasesUrl)

                    credentials {
                        username = System.getenv("NEXUS_USER")
                        password = System.getenv("NEXUS_PASSWORD")
                    }
                }
            }
        }
    }
}

if (!isJitPack && Library.isRelease) {
    signing {
        val signingKey = findProperty("signingKey")?.toString()
        val signingPassword = findProperty("signingPassword")?.toString()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(String(Base64.getDecoder().decode(signingKey)), signingPassword)
            publishing.publications.withType<MavenPublication> {
                sign(this)
            }
        }
    }
}
