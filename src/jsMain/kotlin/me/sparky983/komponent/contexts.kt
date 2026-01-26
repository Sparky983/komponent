package me.sparky983.komponent

import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Provides the given context value for the context type to all children.
 * 
 * @param value the context value
 * @param children the children
 * @param T the context type
 */
public inline fun <reified T : Any> Html.Provide(
    value: T, 
    noinline children: Children
) {
    Provide(typeOf<T>(), value, children)
}

@PublishedApi
internal fun <T : Any> Html.Provide(
    type: KType,
    value: T,
    children: Children
) {
    Fragment(Provider(type, value, parent = contexts)).also {
        it.children()
        emit(it)
    }
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