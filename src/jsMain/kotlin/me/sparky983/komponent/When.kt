package me.sparky983.komponent

/**
 * A dynamic conditional component. Renders [children] while the condition is 
 * `true`, otherwise the [fallback] condition.
 *  
 * @param condition the conditional
 * @param fallback the component to render when [condition] is `false`
 * @param children the default component to render
 * @since 0.1.0
 */
public fun When(
    condition: Signal<Boolean>,
    fallback: Element? = null,
    vararg children: Element
) {
    val holder = FragmentElement()

    var visibility = false

    condition.subscribe { update ->
        if (update != visibility) {
            visibility = update
            if (update) {
                children.forEach(holder::add)
            } else {
                children.forEach(holder::remove)
                if (fallback != null) {
                    holder.add(fallback)
                }
            }
        }
    }
}