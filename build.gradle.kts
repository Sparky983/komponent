import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.NpmVersions
import java.net.URI

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("org.jetbrains.dokka") version "2.1.0"
    kotlin("plugin.power-assert") version "2.3.0"
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    js {
        browser {
            testTask {
                useKarma {
                    useChrome()
                }
            }
            commonWebpackConfig {
                sourceMaps = true
            }
        }
        compilerOptions {
            target = "es2015"
        }
    }
    sourceSets {
        jsTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
powerAssert {
    functions = listOf("kotlin.assert", "kotlin.test.assertTrue", "kotlin.test.assertEquals", "kotlin.test.assertNull")
}

dokka {
    dokkaPublications.html {
        moduleName = project.name
        moduleVersion = project.version.toString()
        outputDirectory = layout.buildDirectory.dir("docs/html")
    }
    dokkaSourceSets.configureEach {
        sourceLink {
            remoteUrl = URI("https://github.com/sparky983/komponent/tree/main")
            remoteLineSuffix = "#L"
        }
    }
}

mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaGenerate")))
    signAllPublications()
    publishToMavenCentral()
}

tasks.register<Task>("generate") {
    description = "Generate node API"

    doLast {
        generate(file("src/jsMain/kotlin/me/sparky983/komponent/generated"))
    }
}