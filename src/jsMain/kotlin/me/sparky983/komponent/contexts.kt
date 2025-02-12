package me.sparky983.komponent

import kotlin.reflect.KClass
import kotlin.reflect.cast

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
    Provide(T::class, value, children)
}

@PublishedApi
internal fun <T : Any> Html.Provide(
    type: KClass<T>,
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
    fun <T : Any> context(type: KClass<T>): T?

    /**
     * Represents a context scope with no values.
     */
    object Empty : Contexts {
        override fun <T : Any> context(type: KClass<T>): T? = null
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
    private val type: KClass<T>,
    private val value: T,
    private val parent: Contexts
) : Contexts {
    override fun <T : Any> context(type: KClass<T>): T? {
        if (type == this.type) {
            return type.cast(value)
        }
        return parent.context(type)
    }
}