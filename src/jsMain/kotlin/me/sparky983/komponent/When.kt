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
public fun Html.When(
    condition: Signal<Boolean>,
    fallback: Children? = null,
    children: Children
) {
    val holder = Fragment()

    val conditional = Fragment()
    conditional.children()

    val otherwise = if (fallback == null) {
        null
    } else {
        Fragment().also { it.fallback() }
    }

    var visibility = false

    val subscription = condition.subscribe { update ->
        if (update != visibility) {
            visibility = update
            if (update) {
                holder.add(conditional)
            } else {
                holder.remove(conditional)
                if (otherwise != null) {
                    holder.add(otherwise)
                }
            }
        }
    }

    holder.onMount { subscription.canceled = false }
    holder.onUnmount { subscription.canceled = true }

    emit(holder)
}