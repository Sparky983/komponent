package me.sparky983.komponent

/**
 * A reactive list component that updates as [each] updates.
 * 
 * @param children a function that renders each child
 * @since 0.1.0
 */
public fun <E> Html.For(each: ListSignal<E>, children: Html.(E) -> Unit) {
    val fragment = Fragment()

    fun render(element: E): Html {
        val row = Fragment()
        row.children(element)
        return row
    }

    each.forEach { fragment.add(render(it)) }

    val subscription = each.observe(object : ListObserver<E> {
        override fun added(index: Int, element: E) {
            fragment.add(index, render(element))
        }

        override fun removed(index: Int) {
            fragment.removeAt(index)
        }

        override fun replaced(index: Int, element: E) {
            fragment.set(index, render(element))
        }
    })

    onMount { subscription.canceled = false }
    onUnmount { subscription.canceled = true }

    emit(fragment)
}

/**
 * A reactive list component that updates as [each] updates, additionally
 * providing an index signal for each child.
 * 
 * @param children a function that renders each child, along with its index
 * @since 0.3.0
 */
public fun <E> Html.For(
    each: ListSignal<E>,
    children: Html.(index: Signal<Int>, E) -> Unit
) {
    val fragment = Fragment()
    val indices = mutableListOf<MutableSignal<Int>>()

    fun render(index: MutableSignal<Int>, element: E): Html {
        val row = Fragment()
        row.children(index, element)
        return row
    }

    fun reindex(from: Int) {
        for (i in from until indices.size) {
            indices[i].value = i
        }
    }

    each.forEachIndexed { i, element ->
        val index = signal(i)
        indices.add(index)
        fragment.add(render(index, element))
    }

    val subscription = each.observe(object : ListObserver<E> {
        override fun added(index: Int, element: E) {
            val position = signal(index)
            indices.add(index, position)
            fragment.add(index, render(position, element))
            reindex(index + 1)
        }

        override fun removed(index: Int) {
            indices.removeAt(index)
            fragment.removeAt(index)
            reindex(index)
        }

        override fun replaced(index: Int, element: E) {
            val position = signal(index)
            indices[index] = position
            fragment.set(index, render(position, element))
        }
    })

    onMount { subscription.canceled = false }
    onUnmount { subscription.canceled = true }

    emit(fragment)
}