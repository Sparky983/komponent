package me.sparky983.komponent

/**
 * A component that updates [component] dynamically as [signal] receives 
 * updates.
 * 
 * @param signal the value to track
 * @param component the renderer for each received value
 * @param N the namespace of the component
 * @param T the type of the tracked value
 * @since 0.1.0
 */
public fun <N : Namespace, T> N.Dynamic(signal: Signal<T>, component: N.(T) -> Unit) {
    val holder = Fragment()
    val subscription = signal.subscribe {
        holder.clear()
        holder.render {
            val fragment = Fragment()
            fragment.emitSelf()
            fragment.render {
                component(it)
            }
        }
    }

    holder.render {
        onMount { subscription.canceled = false }
        onUnmount { subscription.canceled = true }
    }

    holder.emitSelf()
}
