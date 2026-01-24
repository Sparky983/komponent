@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("com.vanniktech.maven.publish") version "0.32.0"
    id("org.jetbrains.dokka") version "2.1.0"
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    sourceSets.jsMain.dependencies {
        implementation(project(":"))
    }
    js {
        browser {}
        binaries.executable()
        compilerOptions {
            target = "es2015"
        }
    }
}

mavenPublishing {
    configure(KotlinMultiplatform(
        javadocJar = JavadocJar.Dokka("dokkaHtml"),
        sourcesJar = true
    ))
    signAllPublications()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        description = project.description
        url = "https://github.com/Sparky983/komponent"
        licenses {
            license {
                name = "MIT License"
                url = "https://www.opensource.org/licenses/mit-license"
            }
        }
        developers {
            developer {
                id = "Sparky983"
            }
        }
        scm {
            url = "https://github.com/Sparky983/komponent"
            connection = "scm:git:git://github.com/Sparky983/komponent.git"
            developerConnection = "scm:git:ssh://git@github.com/Sparky983/komponent.git"
        }
    }
}

