package me.sparky983.komponent

/**
 * Mounts the children to the given dom node.
 * 
 * @param to where the children should be mounted to
 * @param children the children
 * @throws IllegalStateException if [to] is not connected to the document
 * @since 0.1.0
 */
public fun mount(to: org.w3c.dom.Node, children: Html.() -> Unit) {
    check(to.isConnected) { "Mount target is not connected to the document" }
    val fragment = Fragment(Contexts.Empty).also(children)
    fragment.nodes().forEach(to::appendChild)
    fragment.onMount()
}