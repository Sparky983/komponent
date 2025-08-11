import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

description = "The Komponent Compiler"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(kotlin("compiler-embeddable"))
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

tasks {
    withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions.freeCompilerArgs = listOf(
            "-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
            "-Xcontext-parameters"
        )
    }
}