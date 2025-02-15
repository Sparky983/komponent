# Getting Started

## Installation

::: code-group

````kt [Gradle (Kotlin DSL)]
repositories {
    mavenCentral()
    maven("https://repo.sparky983.me/snapshots")
}

dependencies {
    kotlinCompilerPluginClasspath("me.sparky983.komponent:compiler:0.1.0-SNAPSHOT")
}

kotlin {
    sourceSets {
        jsMain.dependencies {
            implementation("me.sparky983.komponent:komponent:0.1.0-SNAPSHOT")
        }
    }
}
````

:::