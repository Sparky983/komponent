package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.Node

/**
 * Effectively a non-existent element that is in-place of all its children.
 */
internal class Fragment : Element() {
    /*
     * Fragments work by creating a marker element that represents the last 
     * child. All children are placed relative to either the marker or 
     * previously added children.
     */
    
    private val marker = document.createTextNode("")
    private val children: MutableList<Element> = mutableListOf()

    fun add(element: Element) {
        children.add(element)

        val parent = marker.parentNode
        if (parent != null) {
            for (node in element.nodes()) {
                parent.insertBefore(node, marker)
            }
        }
    }

    fun remove(element: Element) {
        children.remove(element)
        val parent = marker.parentNode
        if (parent != null) {
            for (node in element.nodes()) {
                parent.removeChild(node)
            }
        }
    }

    fun add(index: Int, element: Element) {
        val parent = marker.parentNode
        if (parent != null) {
            val after = children.asSequence()
                .drop(index)
                .flatMap { it.nodes() }
                .elementAtOrElse(0) { marker }
            for (node in element.nodes()) {
                parent.insertBefore(node, after)
            }
        }
        children.add(index, element)
    }

    fun removeAt(index: Int): Element {
        val element = children[index]
        children.remove(element)
        val parent = marker.parentNode
        if (parent != null) {
            for (node in element.nodes()) {
                parent.removeChild(node)
            }
        }
        return element
    }

    fun set(index: Int, element: Element): Element {
        val previous = children[index]
        val parent = marker.parentNode
        if (parent != null) {
            add(index, element)
            remove(previous)
        }
        return previous
    }

    override fun nodes(): Sequence<Node> {
        return children.asSequence().flatMap { it.nodes() } + marker
    }
}
