package me.sparky983.komponent

import org.w3c.dom.Node

public sealed class Element {
    /**
     * Returns a shallow sequence of all actual dom nodes that this element
     * represents.
     *
     * A [FragmentElement] represents multiple elements.
     */
    internal abstract fun nodes(): Sequence<Node>
}

/**
 * An element represented by a single [Node].
 */
internal class DomElement internal constructor(private val element: Node) : Element() {
    override fun nodes() = sequenceOf(element)
}