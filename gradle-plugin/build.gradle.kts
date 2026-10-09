import com.vanniktech.maven.publish.GradlePublishPlugin

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "1.3.1"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
}

gradlePlugin {
    website = "https://komponent.sparky983.me"
    vcsUrl = "https://github.com/Sparky983/komponent.git"
    plugins {
        create("komponent") {
            id = "me.sparky983.komponent"
            implementationClass = "me.sparky983.komponent.gradle.KomponentPlugin"
            displayName = "Komponent"
            description = "Configures Komponent and the Komponent Compiler."
            tags = listOf("komponent", "html")
        }
    }
}

mavenPublishing {
    configure(GradlePublishPlugin())
    signAllPublications()
    if (version.toString().endsWith("-SNAPSHOT")) {
        publishToMavenCentral()
    }
}

tasks {
    jar {
        manifest {
            attributes("Implementation-Version" to version)
        }
    }

    publishPlugins {
        onlyIf { !version.toString().endsWith("-SNAPSHOT") }
    }
}
