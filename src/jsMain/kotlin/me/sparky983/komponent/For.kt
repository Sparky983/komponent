package me.sparky983.komponent

/**
 * A reactive list component that updates as [each] updates.
 * 
 * @param children a function that renders each child
 * @since 0.1.0
 */
public fun <E> Html.For(each: ListSignal<E>, children: Html.(E) -> Unit) {
    val fragment = Fragment()

    for (element in each) {
        fragment.children(element)
    }

    val subscription = each.mirrorInto(fragment) {
        val fragment = Fragment()
        fragment.children(it)
        fragment
    }

    onMount { subscription.canceled = false }
    onUnmount { subscription.canceled = true }

    emit(fragment)
}