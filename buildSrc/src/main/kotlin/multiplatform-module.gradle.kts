import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    org.jetbrains.kotlin.multiplatform
    org.jetbrains.kotlinx.`binary-compatibility-validator`
    org.jetbrains.dokka
}

kotlin {
    jvm {
        compilations.all {
            compilerOptions.options.jvmTarget.set(Jvm.target)
        }
    }

    js(IR) {
        nodejs()
        browser()
    }

    linuxX64()
    linuxArm64()
    mingwX64()
//    macosX64()
//    macosArm64()

    sourceSets {
        val nativeMain by creating {
            dependsOn(commonMain.get())
        }
        targets.map { it.name }
            .filter { it.endsWith("64") }
            .map { getByName("${it}Main") }
            .forEach { it.dependsOn(nativeMain) }
    }
}

tasks {
    getByName<KotlinJvmTest>("jvmTest") {
        useJUnitPlatform()
    }

    dokkaHtml {
        configure {
            dokkaSourceSets {
                val map = asMap

                if (map.containsKey("jsMain")) {
                    named("jsMain") {
                        displayName.set("JS")
                    }
                }

                if (map.containsKey("jvmMain")) {
                    named("jvmMain") {
                        displayName.set("JVM")
                    }
                }

                if (map.containsKey("commonMain")) {
                    named("jvmMain") {
                        displayName.set("Common")
                    }
                }
            }
        }
    }
}
