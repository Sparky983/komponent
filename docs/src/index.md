# Getting Started

Before installing, be sure to [set up a Kotlin/JS project][kotlin-js-setup].

## Plugin Installation

::: code-group

```kt [build.gradle.kts]
plugins {
    id("me.sparky983.komponent") version "0.1.0"
}
```

:::

## IDE Setup

Komponent makes use of a Kotlin compiler plugin in order to improve developer
ergonomics. IntelliJ however, does not support this out of the box for 
user plugins so it must be enabled in settings.

1. Enabled K2 in **Settings | Languages & Frameworks | Kotlin | Enable K2 Mode**
and restart
2. Set the value of the `kotlin.k2.only.bundled.compiler.plugins.enabled` option
in the IntelliJ registry to `false` to allow the Kotlin IntelliJ plugin to 
enable external Kotlin plugins in the IDE. You can access the registry by going 
**Navigate | Search Everywhere | Registry...**.

[kotlin-js-setup]: https://kotlinlang.org/docs/js-project-setup.html
