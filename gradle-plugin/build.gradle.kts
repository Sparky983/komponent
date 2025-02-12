plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "2.1.10"
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
    plugins {
        create("komponent") {
            id = "me.sparky983.komponent.compiler"
            implementationClass = "me.sparky983.komponent.gradle.KomponentPlugin"
        }
    }
}