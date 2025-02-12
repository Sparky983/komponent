package me.sparky983.komponent

/**
 * Mounts the children to the given dom node.
 * 
 * @param to where the children should be mounted to
 * @param children the children
 * @since 0.1.0
 */
public fun mount(to: org.w3c.dom.Node, children: Html.() -> Unit) {
    val node = Tag(to, Contexts.Empty).apply {
        emit(Fragment().also(children))
    }
    node.onMount()
}