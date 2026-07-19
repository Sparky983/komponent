package me.sparky983.komponent

import org.w3c.dom.Node
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * [DslMarker] for the [Namespace] DSL.
 *
 * @since 0.1.0
 */
@DslMarker
public annotation class HtmlDsl

/**
 * An opaque namespace-aware markup-building scope.
 *
 * Components defined on this type may be used in [Html], [Svg] and [MathMl]
 * scopes.
 *
 * @since 0.3.0
 */
@HtmlDsl
public sealed class Namespace(
    internal val contexts: Contexts,
) {
    // A namespace is a scope for an element which keeps track of contexts

    internal abstract val element: Element<*, *>

    /**
     * Gets the context value associated with the type [T] in this scope, or
     * fails with the given message.
     *
     * @param message the message given when no context value exists for the
     * given type
     * @return the context value
     * @param T the type of the context value
     * @since 0.1.0
     */
    public inline fun <reified T : Any> context(
        message: String = "Context ${T::class.simpleName} was not provided in this scope",
    ): T = context(typeOf<T>()) ?: throw IllegalStateException(message)

    @PublishedApi
    internal fun <T : Any> context(type: KType): T? = contexts.context(type)

    /**
     * Adds a function to be run when the receiving element is mounted.
     *
     * @param handler the handler
     * @since 0.1.0
     */
    public fun onMount(handler: () -> Unit): Unit = element.onMount(handler)

    /**
     * Adds a function to be run when the receiving element is unmounted.
     *
     * @param handler the handler
     * @since 0.1.0
     */
    public fun onUnmount(handler: () -> Unit): Unit = element.onUnmount(handler)
}

/**
 * A scope in which HTML elements and HTML-specific components may be created.
 *
 * @since 0.1.0
 */
public class Html internal constructor(
    override val element: Element<*, Html>,
    contexts: Contexts,
) : Namespace(contexts) {
    internal fun copy0(element: Element<Html, Html>, contexts: Contexts = this.contexts) =
        Html(element, contexts)
    
    internal fun emit(element: Element<Html, Html>) = this.element.emit(element)
}

/**
 * A scope in which SVG elements and SVG-specific components may be created.
 *
 * @since 0.3.0
 */
public class Svg internal constructor(
    override val element: Element<*, Svg>,
    contexts: Contexts,
) : Namespace(contexts) {
    internal fun copy0(element: Element<Svg, Svg>, contexts: Contexts = this.contexts) =
        Svg(element, contexts)

    internal fun emit(element: Element<Svg, Svg>) = this.element.emit(element)
}

/**
 * A scope in which MathML elements and MathML-specific components may be
 * created.
 *
 * @since 0.3.0
 */
public class MathMl internal constructor(
    override val element: Element<*, MathMl>,
    contexts: Contexts,
) : Namespace(contexts) {
    internal fun copy0(element: Element<MathMl, MathMl>, contexts: Contexts = this.contexts) =
        MathMl(element, contexts)

    internal fun emit(element: Element<MathMl, MathMl>) = this.element.emit(element)
}

internal interface Element<in N : Namespace, out C : Namespace> {
    fun removeFromParent()
    fun nodes(): Sequence<Node>
    fun emit(content: Element<C, *>)
    fun mount()
    fun unmount()
    fun onMount(handler: () -> Unit)
    fun onUnmount(handler: () -> Unit)
}

// We should probably segregate lifecycle methods, but it's not too important
internal abstract class LifecycleHelper<N : Namespace, C : Namespace> : Element<N, C> {
    private val mountHandlers: MutableList<() -> Unit> = mutableListOf()
    private val unmountHandlers: MutableList<() -> Unit> = mutableListOf()

    override fun onMount(handler: () -> Unit) {
        mountHandlers.add(handler)
    }

    override fun onUnmount(handler: () -> Unit) {
        unmountHandlers.add(handler)
    }

    override fun mount() {
        mountHandlers.forEach { it() }
    }

    override fun unmount() {
        unmountHandlers.forEach { it() }
    }
}

internal class Tag<N : Namespace, C : Namespace>(private val element: Node) : 
    LifecycleHelper<N, C>(), Element<N, C> {
    override fun emit(content: Element<C, *>) {
        onMount(content::mount)
        onUnmount(content::unmount)
        content.nodes().forEach(element::appendChild)
        if (element.isConnected) {
            content.mount()
        }
    }

    override fun removeFromParent() {
        element.parentNode?.removeChild(element)
    }

    override fun nodes(): Sequence<Node> = sequenceOf(element)
}
