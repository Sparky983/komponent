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

## IDE Setup

1. Enabled K2 in **Settings | Languages & Frameworks | Kotlin | Enable K2 Mode**
and restart
2. Disable the `kotlin.k2.only.bundled.compiler.plugins.enabled` option in the
IntelliJ registry to allow the Kotlin IntelliJ plugin to enable external Kotlin
plugins in the IDE.