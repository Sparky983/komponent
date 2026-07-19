package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.Node

internal class Fragment<N : Namespace>(contexts: Contexts, val parent: N) {
    private val element = object : LifecycleHelper<N, N>() {
        override fun emit(content: Element<N, *>): Unit = add(content)

        override fun mount() {
            super.mount()
            children.forEach(Element<N, *>::mount)
        }

        override fun unmount() {
            super.unmount()
            children.forEach(Element<N, *>::unmount)
        }

        override fun removeFromParent() {
            marker.parentNode?.let { parent -> nodes().forEach(parent::removeChild) }
        }

        override fun nodes(): Sequence<Node> = children.asSequence().flatMap { it.nodes() } + marker
    }
    private val marker = document.createTextNode("")
    val children: MutableList<Element<N, *>> = mutableListOf()

    /*
     * We must do an unsafe cast since the compiler cannot prove that `parent.copy0` is `N`
     * since at runtime the scope could be `Html` and the fragment could be typed as being
     * applicable in every namespace (if `N` = `Namespace`) meaning you could add it to a namespace
     * that does not match `scope`.
     *
     * This is safe though since we only ever add it to the parent so they always match.
     */
    @Suppress("UNCHECKED_CAST")
    private val scope: N = 
        when (parent) {
            is Html -> parent.copy0(element as Element<Html, Html>, contexts)
            is Svg -> parent.copy0(element as Element<Svg, Svg>, contexts)
            is MathMl -> parent.copy0(element as Element<MathMl, MathMl>, contexts)
        } as N
    
    @Suppress("UNCHECKED_CAST")
    fun emitSelf() {
        when (parent) {
            is Html -> parent.emit(element as Element<Html, Html>)
            is Svg -> parent.emit(element as Element<Svg, Svg>)
            is MathMl -> parent.emit(element as Element<MathMl, MathMl>)
        }
    }

    /**
     * This operation directly removes the fragment from its parent in the DOM without publishing
     * lifecycle hook events.
     */
    @RequiresOptIn
    annotation class RemoveFromParent

    @RemoveFromParent
    fun removeFromParent() {
        this.element.removeFromParent()
    }

    fun render(children: N.() -> Unit) {
        scope.children()
    }

    private fun child(renderer: N.() -> Unit): Element<N, N> {
        val child = scope.Fragment()
        child.render(renderer)
        return child.element
    }

    fun add(renderer: N.() -> Unit) = add(child(renderer))

    private fun add(content: Element<N, *>) {
        children.add(content)
        val parent = marker.parentNode
        if (parent != null) {
            content.nodes().forEach { parent.insertBefore(it, marker) }
            if (parent.isConnected) {
                content.mount()
            }
        }
    }

    private fun remove(content: Element<N, *>) {
        children.remove(content)
        if (marker.parentNode != null) {
            content.removeFromParent()
            if (marker.isConnected) {
                content.unmount()
            }
        }
    }

    fun clear() {
        while (children.isNotEmpty()) {
            remove(children.last())
        }
    }
    
    fun add(index: Int, renderer: N.() -> Unit) = add(index, child(renderer))

    private fun add(index: Int, content: Element<N, *>) {
        val parent = marker.parentNode
        if (parent != null) {
            val following = children.asSequence()
                .drop(index)
                .flatMap { it.nodes() }
                .firstOrNull() ?: marker
            for (node in content.nodes()) {
                parent.insertBefore(node, following)
            }
            if (parent.isConnected) {
                content.mount()
            }
        }
        children.add(index, content)
    }

    fun removeAt(index: Int) = children[index].also(::remove)

    fun set(index: Int, renderer: N.() -> Unit) = set(index, child(renderer))
    
    private fun set(index: Int, content: Element<N, *>): Element<N, *> {
        val previous = children[index]
        if (marker.parentNode != null) {
            add(index, content)
            remove(previous)
        } else {
            children[index] = content
        }
        return previous
    }
}

internal fun <N : Namespace> N.Fragment(contexts: Contexts = this.contexts): Fragment<N> =
    Fragment(contexts, this)
