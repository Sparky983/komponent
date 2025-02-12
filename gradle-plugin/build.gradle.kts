plugins {
    `java-gradle-plugin`
    kotlin("jvm")
}

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
            id = "me.sparky983.komponent"
            implementationClass = "me.sparky983.komponent.gradle.KomponentPlugin"
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