# Client-Side Routing

Komponent supports basic client-side routing.

## Installation

Client-side routing requires an external dependency:

::: code-group

```kt [build.gradle.kts]
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        jsMain.dependencies {
            implementation("me.sparky983.komponent:router:0.1.0")
        }
    }
}
```

:::

## Usage

Client-side routes are simply defined using the `Router` and `Route` components.
The each `Route` component nested inside the `Router` represents a separate, 
client-side page.

```kt
fun Html.MyApp() {
    Router {
        Route("/") {
            // render page at "/"
        }
    }
}
```

or to capture a path variable:

::: info
Currently variables cannot be greedy.
:::

```kt
fun Html.MyApp() {
    Router {
        Route("/project/:id") { ctx -> // [!code ++]
            val id = ctx["id"] // [!code ++]
        } // [!code ++]
    }
}
```

To match against fallbacks, the `Fallback` component can be used:

```kt
fun Html.MyApp() {
    Router {
        Fallback { // [!code ++]
            text("Page not found") // [!code ++]
        } // [!code ++]
    }
}
```
