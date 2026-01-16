# Your First Component

In Komponent, a component is just a reusable function that creates some UI.  

To declare a component, simply define a function with the `Html` receiver:

```kt
fun Html.MyComponent() {
    // ...
}
```

UI is described using simple functions. The most basic of which is the 
`text(String)` function which appends some text to the document.

```kt
fun Html.MyComponent() {
    text("Hello, world!") // [!code ++]
}
```

You can also append tags to the document: 

::: tip
If you are seeing red lines in your editor, or your code doesn't compile, see
the [installation steps](index.md).
:::

```kt
fun Html.MyComponent() {
    h1(className = "title") { // [!code ++]
        text("<h1>") // [!code ++]
    } // [!code ++]
}
```

In any Komponent, it is convention to always set attributes using explicitly 
named arguments.

### Props

Components are just functions, so they can accept any regular arguments to use
and transform into UI.

```kt
enum class ButtonKind {
    PRIMARY,
    SECONDARY
}

fun Html.Button(kind: ButtonKind) {
    when (kind) {
        PRIMARY -> button(className = "primary") {
            text("Primary")
        }
        SECONDARY -> button(className = "secondary") {
            text("Secondary")
        }
    }
}
```

Props can also be reactive. See [Reactivity](reactivity.md#compiler).

### Children

Sometimes, props include other components. In this case, the `Children` type 
should be used as the last parameter. The `Children` type is actually a 
shorthand for `Html.() -> Unit` which is tedious to write every time so we can
just use the shorthand.

```kt
enum class ButtonKind {
    PRIMARY,
    SECONDARY
}

fun Html.Button(kind: ButtonKind, children: Children) {
    when (kind) {
        PRIMARY -> button(className = "primary") {
            children() // [!code ++]
        }
        SECONDARY -> button(className = "secondary") {
            children() // [!code ++]
        }
    }
}
```

## Mounting the Component

So far, we have created components, but we haven't been able to actually see 
them on a page. To "mount" a component, we must attach it to a DOM element.

```kt
fun main() {
    mount(document.body!!) {
        // Render the body here
        MyApp()
    }
}

fun MyApp() {
    // ...
}
```

## Events

Komponent supports events by passing `on<Event>` arguments to any tag you 
want to listen to.

```kt
fun Html.Button() {
    button(onClick = { println("You clicked!") }) {
        text("Click me")
    }
}
```

### Life Cycle Events

All elements have two life cycle stages that can occur an indefinite amount 
of times: 
- Mounting
- Unmounting

You can listen for them with `onMount` and `onUnmount` respectively. This is 
useful for managing resources.
