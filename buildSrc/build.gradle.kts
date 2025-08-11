plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.2.20-Beta2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}
