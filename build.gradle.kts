plugins {
    kotlin("multiplatform") version "2.1.10"
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
    js {
        browser {}
        compilerOptions {
            target = "es2015"
        }
    }
}

tasks.register<Task>("generate") {
    description = "Generate node API"

    doLast {
        generate(file("src/jsMain/kotlin/me/sparky983/komponent/generated"))
    }
}