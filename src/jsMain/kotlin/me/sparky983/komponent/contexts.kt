package me.sparky983.komponent

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Provides the given context value for the context type to all children.
 * 
 * @param value the context value
 * @param children the children
 * @param N the namespace of the component
 * @param T the context type
 */
public inline fun <N : Namespace, reified T : Any> N.Provide(
    value: T, 
    noinline children: N.() -> Unit
) {
    Provide(typeOf<T>(), value, children)
}

@PublishedApi
internal fun <N : Namespace, T : Any> N.Provide(
    type: KType,
    value: T,
    children: N.() -> Unit
) {
    val provide = Fragment(Provider(type, value, parent = contexts))
    provide.render(children)
    provide.emitSelf()
}

/**
 * Represents a contexts scope.
 */
internal sealed interface Contexts {
    fun <T : Any> context(type: KType): T?

    /**
     * Represents a context scope with no values.
     */
    object Empty : Contexts {
        override fun <T : Any> context(type: KType): T? = null
    }
}

/**
 * Essentially a linked list of context providers forming a full context scope.
 * 
 * @param type the context type this "node" is providing
 * @param value the context value
 * @param parent the inherited contexts
 * @param T the type of the context
 */
private class Provider<T : Any>(
    private val type: KType,
    private val value: T,
    private val parent: Contexts
) : Contexts {
    override fun <T : Any> context(type: KType): T? {
        if (type == this.type) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        return parent.context(type)
    }
}
