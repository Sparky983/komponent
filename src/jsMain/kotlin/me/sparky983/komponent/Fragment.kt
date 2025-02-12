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

    override fun emit(child: Html) = add(child)

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