package me.sparky983.komponent

import org.w3c.dom.Node
import kotlin.reflect.KClass

/**
 * Represents the API for passing around children.
 * 
 * Implementors should be careful to allow for users to be able to "emit" a new
 * element at any time, even after the function has returned.
 * 
 * It is also important to note that implementors may not even choose to use 
 * this type. Sometimes it is necessary to restrict the types of children, and
 * therefore use a stricter receiver type.
 * 
 * @since 0.1.0
 */
public typealias Children = Html.() -> Unit

/**
 * [DslMarker] for [Html] dsl.
 * 
 * @since 0.1.0
 */
@DslMarker
public annotation class HtmlDsl

/**
 * The receiver for [Children].
 * 
 * @since 0.1.0
 */
@HtmlDsl
public sealed class Html(internal val contexts: Contexts) {
    private val onMounts: MutableList<() -> Unit> = mutableListOf()
    private val onUnmounts: MutableList<() -> Unit> = mutableListOf()

    /**
     * Gets context value associated with the type [T] in this scope, or fails
     * with the given message.
     * 
     * @param message the messages given that no context value exists for the
     * given type
     * @return the context value
     * @param T the type of the context value
     * @since 0.1.0
     */
    public inline fun <reified T : Any> context(
        message: String =
            "Context ${T::class.simpleName} was not provided in this scope"
    ): T = context(T::class) ?: throw IllegalStateException(message)

    @PublishedApi
    internal fun <T : Any> context(type: KClass<T>): T? = contexts.context(type)

    /**
     * Adds a function to be run when the receiving element is mounted.
     * 
     * @param handler the handler
     * @since 0.1.0
     */
    public fun onMount(handler: () -> Unit) {
        onMounts.add(handler)
    }

    /**
     * Adds a function to be run when the receiving element is unmounted.
     *
     * @param handler the handler
     * @since 0.1.0
     */
    public fun onUnmount(handler: () -> Unit) {
        onUnmounts.add(handler)
    }

    internal open fun onMount() {
        onMounts.forEach { it() }
    }

    internal open fun onUnmount() {
        onUnmounts.forEach { it() }
    }

    /**
     * Signals to the parent that a child was created.
     */
    internal abstract fun emit(child: Html)

    /**
     * Removes this element from its parent.
     */
    internal abstract fun removeFromParent()

    /**
     * Returns a shallow sequence of all actual dom nodes that this element 
     * represents.
     * 
     * A [Fragment] represents multiple elements.
     */
    internal abstract fun nodes(): Sequence<Node>
}

/**
 * An element represented by a single [Node].
 */
internal class Tag internal constructor(
    private val element: Node,
    contexts: Contexts
) : Html(contexts) {
    override fun emit(child: Html) {
        onMount(child::onMount)
        onUnmount(child::onUnmount)
        if (element.isConnected) {
            child.onMount()
        }

        child.nodes().forEach(element::appendChild)
    }

    override fun removeFromParent() {
        element.parentNode?.removeChild(element)
    }

    override fun nodes() = sequenceOf(element)
}