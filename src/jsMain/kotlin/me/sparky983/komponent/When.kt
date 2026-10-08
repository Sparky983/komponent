package me.sparky983.komponent

/**
 * A dynamic conditional component. Renders [children] while the condition is 
 * `true`, otherwise the [fallback] condition.
 * 
 * Each time [children] or [fallback] is shown, it is rendered again as new
 * elements, with their own life cycle.
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

    var visibility: Boolean? = null

    val subscription = condition.subscribe { update ->
        if (update != visibility) {
            visibility = update
            holder.children.toList().forEach(holder::remove)
            val render = if (update) children else fallback
            if (render != null) {
                val branch = Fragment()
                branch.render()
                holder.add(branch)
            }
        }
    }

    holder.onMount { subscription.canceled = false }
    holder.onUnmount { subscription.canceled = true }

    emit(holder)
}