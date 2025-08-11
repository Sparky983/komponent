package me.sparky983.komponent

/**
 * A component that updates [component] dynamically as [signal] receives 
 * updates.
 * 
 * @param signal the value to track
 * @param component the renderer for each received value
 * @since 0.1.0
 */
public fun <T> Dynamic(signal: Signal<T>, component: (T) -> Element): Element {
    val holder = Fragment()
    var current: Element? = null
    signal.subscribe {
        current?.let { holder.remove(it) }
        current = component(it)
    }
    return holder
}