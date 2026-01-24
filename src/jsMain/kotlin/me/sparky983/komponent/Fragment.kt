package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.Node

/**
 * Effectively a non-existent element that is in-place of all its children.
 */
internal class Fragment(contexts: Contexts) : Html(contexts) {
    /*
     * Fragments work by creating a marker element that represents the last 
     * child. All children are placed relative to either the marker or 
     * previously added children.
     */

    private val marker = document.createTextNode("")
    val children: MutableList<Html> = mutableListOf()

    fun add(element: Html) {
        children.add(element)

        val parent = marker.parentNode
        if (parent != null) {
            if (parent.isConnected) {
                element.onMount()
            }
            for (node in element.nodes()) {
                parent.insertBefore(node, marker)
            }
        }
    }

    fun remove(element: Html) {
        children.remove(element)
        val parent = marker.parentNode
        if (parent != null) {
            if (marker.isConnected) {
                element.onUnmount()
            }
            element.removeFromParent()
        }
    }

    fun add(index: Int, element: Html) {
        val parent = marker.parentNode
        if (parent != null) {
            if (parent.isConnected) {
                element.onMount()
            }
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

    fun removeAt(index: Int): Html {
        val element = children[index]
        children.remove(element)
        val parent = marker.parentNode
        if (parent != null) {
            if (marker.isConnected) {
                element.onUnmount()
            }
            element.removeFromParent()
        }
        return element
    }


    fun set(index: Int, element: Html): Html {
        val previous = children[index]
        val parent = marker.parentNode
        if (parent != null) {
            add(index, element)
            remove(previous)
        }
        return previous
    }

    override fun emit(child: Html) = add(child)
    
    override fun removeFromParent() {
        marker.parentNode?.let { parent ->
            nodes().forEach(parent::removeChild)
        }
    }

    override fun nodes(): Sequence<Node> {
        return children.asSequence().flatMap { it.nodes() } + marker
    }

    override fun onMount() {
        super.onMount()
        children.forEach(Html::onMount)
    }

    override fun onUnmount() {
        super.onUnmount()
        children.forEach(Html::onUnmount)
    }
}

/**
 * A fragment element.
 */
internal fun Html.Fragment() = Fragment(contexts)