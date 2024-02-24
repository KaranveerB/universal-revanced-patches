import org.gradle.kotlin.dsl.support.listFilesOrdered

plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
}

group = "com.karanveerb"

repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.smali)
    // TODO: Required because build fails without it. Find a way to remove this dependency.
    implementation(libs.guava)
    // Used in JsonGenerator.
    implementation(libs.gson)

    // A dependency to the Android library unfortunately fails the build, which is why this is required.
    compileOnly(project("dummy"))
}

kotlin {
    jvmToolchain(11)
}

tasks.withType(Jar::class) {
    manifest {
        attributes["Name"] = "Universal Patches"
        attributes["Description"] = "Universal App Patches for ReVanced."
        attributes["Version"] = project.findProperty("version")
        attributes["Timestamp"] = System.currentTimeMillis().toString()
        attributes["Source"] = "git@github.com:KaranveerB/universal-revanced-patches.git"
        attributes["Author"] = "KaranveerB"
        attributes["Contact"] = "52545097+KaranveerB@users.noreply.github.com"
        attributes["Origin"] = "https://github.com/KaranveerB/universal-revanced-patches"
        attributes["License"] = "GNU General Public License v3.0"
    }
}

tasks {
    register<DefaultTask>("generateBundle") {
        description = "Generate DEX files and add them in the JAR file"

        dependsOn(build)

        doLast {
            val win = System.getProperty("os.name").lowercase().contains("win")
            val androidHome = System.getenv("ANDROID_HOME") ?: error("ANDROID_HOME is not set.")
            val buildToolsDir = File(androidHome).resolve("build-tools").listFilesOrdered().last()

            val d8 = buildToolsDir.resolve(if (win) "d8.bat" else "d8").absolutePath

            val artifacts = configurations.archives.get().allArtifacts.files.files.first().absolutePath
            val workingDirectory = layout.buildDirectory.dir("libs").get().asFile

            exec {
                workingDir = workingDirectory
                commandLine = listOf(d8, artifacts)
            }

            exec {
                workingDir = workingDirectory
                commandLine = listOf("zip", "-u", artifacts, "classes.dex")
            }
        }
    }

    register<JavaExec>("generateMeta") {
        description = "Generate metadata for this bundle"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.revanced.meta.IPatchesFileGenerator")
    }


    // Required to run tasks because Gradle semantic-release plugin runs the publish task.
    // Tracking: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    named("publish") {
        dependsOn("generateBundle")
        dependsOn("generateMeta")
    }
}

publishing {
    publications {
        create<MavenPublication>("revanced-patches-publication") {
            from(components["java"])

            pom {
                name = "Universal Patches"
                description = "Universal App Patches for ReVanced."
                url = "https://github.com/KaranveerB"

                licenses {
                    license {
                        name = "GNU General Public License v3.0"
                        url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                    }
                }
                developers {
                    developer {
                        id = "KaranveerB"
                        name = "KaranveerB"
                        email = "52545097+KaranveerB@users.noreply.github.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/KaranveerB/universal-revanced-patches.git"
                    developerConnection = "scm:git:git@github.com:KaranveerB/universal-revanced-patches.git"
                    url = "https://github.com/KaranveerB/universal-revanced-patches"
                }
            }
        }
    }
}
