import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("org.jetbrains.dokka") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Dokka("dokkaGenerate")))
    signAllPublications()
    publishToMavenCentral()
}

tasks {
    withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}