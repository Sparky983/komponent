plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.1.10"
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "me.sparky983.komponent"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
}

gradlePlugin {
    website = "https://github.com/Sparky983/komponent"
    vcsUrl = "https://github.com/Sparky983/komponent.git"
    plugins {
        create("komponent") {
            id = "me.sparky983.komponent.compiler"
            implementationClass = "me.sparky983.komponent.gradle.KomponentPlugin"
            displayName = "Komponent Compiler"
            description = "The Komponent Compiler"
            tags = listOf("komponent")
        }
    }
}