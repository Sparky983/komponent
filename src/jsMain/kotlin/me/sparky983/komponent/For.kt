package me.sparky983.komponent

/**
 * A reactive list component that updates as [each] updates.
 * 
 * @param children a function that renders each child
 * @since 0.1.0
 */
public fun <E> For(each: ListSignal<E>, children: (E) -> Element): Element {
    val fragment = Fragment()

    each.forEach {
        fragment.add(children(it))
    }

    each.mirrorInto(fragment, children)

    return fragment
}