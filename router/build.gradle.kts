@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
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
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaGenerate")))
    signAllPublications()
    publishToMavenCentral()
}

