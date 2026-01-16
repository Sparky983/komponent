# Context API

The context API can be used to pass down values to deeply nested components 
without having to explicitly pass them down through each function.

The `Provide` component provides a means of providing values to children, and 
the `context<T>` function gets current value of the specified type.

First, using the `Provide` component simply takes the value, and then any 
children will be able to access the component within their scope.

```kt
fun MyApp() {
    val darkMode = signal(false)
    
    Provide(darkMode) {
        // children go here
    }
}
```

Now, any children nested inside the `Provide` component will have access to the
same value by its type.

```kt
fun MyApp() {
    Provide(darkMode) {
        Toggle() // [!code ++]
    }
}

fun Toggle() { // [!code ++]
    val darkMode = context<MutableSignal<Boolean>>() // [!code ++]
    button(onClick = { darkMode.value = !darkMode.value }) { // [!code ++]
        text("Toggle light/dark mode") // [!code ++]
    } // [!code ++]
} // [!code ++]
```