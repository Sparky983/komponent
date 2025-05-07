package me.sparky983.komponent

/**
 * A reactive list component that updates as [each] updates.
 * 
 * @param children a function that renders each child
 * @since 0.1.0
 */
public fun <E> Html.For(each: ListSignal<E>, children: Html.(E) -> Unit) {
    val fragment = Fragment()

    each.forEach {
        val element = Fragment()
        element.children(it)
        fragment.add(element)
    }

    val subscription = each.mirrorInto(fragment) {
        val element = Fragment()
        element.children(it)
        element
    }

    onMount { subscription.canceled = false }
    onUnmount { subscription.canceled = true }

    emit(fragment)
}