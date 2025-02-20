# Reactivity

Reactivity is a concept that allows UI to react to changes in our application's
state.

## Explaining Reactivity

Reactive values are values of `Signal<T>`. A `Signal<T>` simply represents a
value that may change over time.

You can create one using the `signal(T)` function:

```kt
val count: MutableSignal<Int> = signal(initialValue = 1)

count.value++

count.subscribe { println(it) } // "1"

count.value++ // "2"
```

Subscribing a `Signal<T>` will call the block, first with the initial value, and
again for each subsequent update.

To create a `Signal<T>` that is never updated, you can use the `just(T)` 
function:

```kt
val name = just("Foo")
```

## Derived Signal

It is common to have reactive values that are "derived" from other values. This
means that when the original value changes, the derived value also updates.

You can simply attach a trailing lambda to any signal to transform it into a 
derived signal.

```kt
val count = signal(1)
val doubleCount = count { it * 2 }

assert(doubleCount.value == 2)

count.value = 4
assert(doubleCount.value == 8)
```

## Compiler

The Komponent Compiler allows you to call any function that accepts a 
`Signal<T>` with just a regular value:

```kt
fun log(signal: Signal<String>) {
    signal.subscribe { println(it) }
}

log("Hello, world!") // "Hello, world!"
```

## Reactive Components

Komponent tags accept reactive values for all attributes. This means that you
can always use a static (in this context, static means any regular old value) or
reactive a value.

```kt
fun Html.Counter() {
    val count = signal(1)
    val value = count { "Value: $count" }
    
    button(onClick = { count.value++ }) {
        text(value)
    }
}
```

You can also accept a `Signal` value to allow any value to be optionally 
reactive.

```kt
fun Html.Title(title: Signal<String>) {
    h1 {
        text(title)
    }
}
```

This function can now be called statically or reactively thanks to the compiler.

```kt
fun Html.MyBlog() {
    Title("My Blog")
    
    val title = signal("My Blog")
    
    Title(title)
}
```

## Reactive Flow Components

A single `Signal` only represents a single value. On their own, they don't 
support other kinds of values such as lists, other components or anything that
requires conditional rendering.

### When

The `When` component is used to show a component when a condition holds. The 
`When` is best used for conditions that may change over time.

```kt
fun Html.Details() {
    val expanded = signal(false)
    
    button(onClick = { expanded.value = !expanded.value }) {
        text("Click to expand")
    }
    
    When(expanded) {
        text("Hidden content")
    }
}
```

`When` also supports fallback rendering to handle the `else` case.

```kt
fun Html.Details() {
    When(expanded, fallback = { text("Content is hidden") }) { // [!code ++]
        text("Hidden content") // [!code ++]
    } // [!code ++]
}
```

The `When` component only exists because the logic to replace the internal logic
to reactively remove and re-add the children, and it is necessary for unchanging 
values as an ordinary `if` statement will still work.

### Dynamic

Sometimes, you want to rerender an entire component when a value changes, for 
this you can use the `Dynamic` component. It takes a `Signal` and renders the
children every time the signal is updated.

```kt
fun Html.Tabs(tab: MutableSignal<Tab>) {
    button(onClick = { tab.value = Tab.FIRST }) { text("first") }
    button(onClick = { tab.value = Tab.SECOND }) { text("Second") } 

    Dynamic(tab) {
        when (it) {
            FIRST -> {}
            SECOND -> {}
        }
    }
}
```

### Lists

Reactive lists use a different kind of signal known as a `ListSignal` since 
signals cannot track mutations to the value, only mutations of the value itself.

To create a list signal, use `flowList(List<E>)` or `flowListOf(*E)`. 

```kt
data class User(val name: String, val age: Int)

val users = flowListOf(User(name = "Foo", age = 17))

button(onClick = { users.add(User(name = "Bar", age = 17)) }) {
    text("Add new user")
}
```

A `ListSignal` can be modified like any other list. However, it can also be used
as a prop to the `For` component which renders a dynamic list of elements.

```kt
For(each = list) { user -> // [!code ++]
    text("Name: ${user.name} Age: ${user.age}") // [!code ++]
} // [!code ++]
```

Similarly to with the `When` component, it may not be necessary for non-reactive 
values as a plain old `for` will suffice.

```kt
val users = listOf(User(name = "Foo", age = 17))

for (user in users) {
    text("Name: ${user.name} Age: ${user.age}")
}
```