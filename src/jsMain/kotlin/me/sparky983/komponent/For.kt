package me.sparky983.komponent

/**
 * A reactive list component that updates as [each] updates.
 * 
 * @param each the list to track
 * @param children a function that renders each child
 * @param N the namespace of the component
 * @param E the type of each element
 * @since 0.1.0
 */
public fun <N : Namespace, E> N.For(each: ListSignal<E>, children: N.(E) -> Unit) {
    val fragment = Fragment()

    fragment.render {
        each.forEach { fragment.add { children(it) } }
    }

    val subscription = each.mirrorInto(fragment) { children(it) }

    onMount { subscription.canceled = false }
    onUnmount { subscription.canceled = true }

    fragment.emitSelf()
}
