package me.sparky983.komponent

/**
 * A dynamic conditional component. Renders [children] while the condition is 
 * `true`, otherwise the [fallback] condition.
 *  
 * @param condition the conditional
 * @param fallback the component to render when [condition] is `false`
 * @param children the default component to render
 * @param N the namespace of the component
 * @since 0.1.0
 */
public fun <N : Namespace> N.When(
    condition: Signal<Boolean>,
    fallback: (N.() -> Unit)? = null,
    children: N.() -> Unit
) {
    val holder = Fragment()

    holder.render {
        val conditional = Fragment()
        conditional.render(children)
    
        val otherwise = if (fallback == null) {
            null
        } else {
            Fragment().also { it.render(fallback) }
        }
    
        var visibility: Boolean? = null
    
        val subscription = condition.subscribe { update ->
            if (update != visibility) {
                visibility = update
                holder.clear()
                if (update) {
                    conditional.emitSelf()
                } else {
                    otherwise?.emitSelf()
                }
            }
        }
        onMount { subscription.canceled = false }
        onUnmount { subscription.canceled = true }
    }

    holder.emitSelf()
}
