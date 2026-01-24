# DOM Manipulation

All HTML tag components (`p`, `h1`, `input`) return their underlying DOM 
elements. These elements can be used to imperatively manipulate the DOM.

## Example: Focussing input

The following example will focus an element when it is attached to the DOM.

```kt
fun Html.NameInput() {
    val input = input(placeholder = "Enter your name")
    
    onMount {
        input.focus()
    }
}
```

## Example: Referencing Element During Construction

Often it may be necessary to reference an element inside the function that 
creates it. Kotlin's `lateinit` can be used for this:

```kt
fun Html.NameInput() {
    val name = signal("")

    p {
        text(name { "Your name is $it" })
    }

    lateinit var input: HTMLInputElement
    input = input(
        placeholder = "Enter your name",
        onInput = {
            name.value = input.value
        }
    )
}
```

## Guidelines

It may not always be necessary to directly manipulate the DOM or DOM elements.
Ask yourself whether the behavior you are looking for can be implemented using
built-in Komponent APIs.

Some common use cases may include:
- Two-way data binding
- Detecting clicks outside an element
- External dependencies that require access to a DOM element
- Access element state that is not a part of the DOM such as:
    - input values
    - Media state
    - Scroll
    - Focus 
