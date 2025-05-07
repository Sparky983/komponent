package me.sparky983.komponent

/**
 * A component that updates [component] dynamically as [signal] receives 
 * updates.
 * 
 * @param signal the value to track
 * @param component the renderer for each received value
 * @since 0.1.0
 */
public fun <T> Html.Dynamic(signal: Signal<T>, component: Html.(T) -> Unit) {
    val holder = Fragment()
    val subscription = signal.subscribe {
        holder.children.forEach { holder.remove(it) }
        val fragment = Fragment()
        holder.emit(fragment)
        fragment.component(it)
    }

    holder.onMount { subscription.canceled = false }
    holder.onUnmount { subscription.canceled = true }

    emit(holder)
}