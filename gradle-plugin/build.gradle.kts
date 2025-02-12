plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "1.2.1"
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

tasks {
    jar {
        manifest {
            attributes("Implementation-Version" to version)
        }
    }
}