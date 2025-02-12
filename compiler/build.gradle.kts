import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.1.10"
    id("com.vanniktech.maven.publish") version "0.30.0" 
}

group = "me.sparky983.komponent"
version = "0.1.0"
description = "The Komponent Compiler"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.1.10")
}

mavenPublishing {
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true,
    ))
    signAllPublications()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        description = project.description 
            ?: throw IllegalStateException("Add a project description")
        url = "https://github.com/Sparky983/komponent/"
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

tasks {
    withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions.freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}