package me.sparky983.komponent

/**
 * Represents a value that can be observed changing over time.
 * 
 * This interface defines the read-only aspect of a signal, but a writable 
 * interface exists through [MutableSignal].
 * 
 * @param T the type of the value
 * @see just
 * @see MutableSignal
 * @since 0.1.0
 */
public interface Signal<out T> {
    /**
     * The current value of the signal.
     * 
     * @since 0.1.0
     */
    public val value: T

    /**
     * Creates a new subscription, emitting values by calling the subscriber
     * when updates are received.
     *
     * The latest value is instantly replayed.
     *
     * Once the subscriber has been subscribed, it will receive events 
     * indefinitely until it has been [canceled][Subscription.canceled].
     *
     * @param subscriber the subscriber
     * @return the subscription
     * @since 0.1.0
     */
    public fun subscribe(subscriber: (T) -> Unit): Subscription

    /**
     * Maps this signal with the given mapping function.
     * 
     * @param mapper the mapping function
     * @return the new signal
     * @param R the resulting type of the mapping function
     * @since 0.1.0
     */
    @JsName("map")
    public operator fun <R> invoke(mapper: (T) -> R): Signal<R>
}

/**
 * An extension of [Signal] that allows for programmatic mutation of the
 * underlying value.
 * 
 * @param T the type of the value
 * @see Signal
 * @see signal
 * @since 0.1.0
 */
public interface MutableSignal<T> : Signal<T> {
    public override var value: T
}

/**
 * An object that is able to manage and dispose of subscriptions.
 * 
 * @since 0.1.0
 */
public interface Subscription {
    /**
     * Whether the subscription is receiving values.
     * 
     * @since 0.1.0
     */
    public var canceled: Boolean
}

/**
 * Creates an immutable signal that only holds the given value.
 * 
 * @param value the value
 * @return the signal
 * @param T the type of the signal
 * @since 0.1.0
 */
public fun <T> just(value: T): Signal<T> {
    return object : Signal<T> {
        override val value: T = value

        override fun subscribe(subscriber: (T) -> Unit): Subscription {
            subscriber(value)
            return object : Subscription {
                override var canceled: Boolean
                    get() = true
                    set(_) {}
            }
        }

        override fun <M> invoke(mapper: (T) -> M): Signal<M> {
            return just(mapper(value))
        }
    }
}

/**
 * Creates a mutable signal with the given initial value.
 * 
 * @param initialValue the value to initialize the signal with
 * @return the signal
 * @param T the type of the signal
 * @since 0.1.0
 */
public fun <T> signal(initialValue: T): MutableSignal<T> {
    return object : MutableSignal<T> {
        private val subscriptions = hashMapOf<Subscription, (T) -> Unit>()

        override var value: T = initialValue
            set(value) {
                field = value
                for (subscriber in subscriptions.values) {
                    subscriber(value)
                }
            }

        override fun <M> invoke(mapper: (T) -> M): Signal<M> {
            val mapped = signal(mapper(value))
            subscribe { mapped.value = mapper(it) }
            return mapped
        }

        override fun subscribe(subscriber: (T) -> Unit): Subscription {
            subscriber(value)
            val subscription = object : Subscription {
                override var canceled: Boolean = true
                    set(value) {
                        if (value) {
                            subscriptions.remove(this)
                        } else {
                            if (field) {
                                subscriptions[this] = subscriber
                            }
                        }
                        field = value
                    }
            }
            subscription.canceled = false
            return subscription
        }
    }
}

public class ListSignal<E> internal constructor(
    private val list: MutableList<E>
) : AbstractMutableList<E>() {
    private val mirrors: MutableMap<Subscription, Mirror> = hashMapOf()

    private inner class Mirror(val fragment: Fragment, val mapper: (E) -> Html)

    internal fun mirrorInto(fragment: Fragment, mapper: (E) -> Html): Subscription {
        val subscription = object : Subscription {
            override var canceled: Boolean = true
                set(value) {
                    if (value) {
                        mirrors.remove(this)
                    } else {
                        if (field) {
                            mirrors[this] = Mirror(fragment, mapper)
                        }
                    }
                    field = value
                }
        }
        subscription.canceled = false
        return subscription
    }

    private fun update(performer: Fragment.((E) -> Html) -> Unit) {
        for (mirror in mirrors.values) {
            mirror.fragment.performer(mirror.mapper)
        }
    }

    override fun add(index: Int, element: E) {
        list.add(index, element)
        update { this.add(index, it(element)) }
    }

    override fun removeAt(index: Int): E {
        return list.removeAt(index).also {
            update { this.removeAt(index) }
        }
    }

    override fun set(index: Int, element: E): E {
        return list.set(index, element).also {
            update { this.set(index, it(element)) }
        }
    }

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun get(index: Int): E = list[index]

    override val size: Int
        get() = list.size
}

public fun <E> flowList(list: List<E>): ListSignal<E> {
    return ListSignal(list.toMutableList())
}

public fun <E> flowListOf(): ListSignal<E> {
    return ListSignal(mutableListOf())
}

public fun <E> flowListOf(vararg element: E): ListSignal<E> {
    return ListSignal(mutableListOf(*element))
}
