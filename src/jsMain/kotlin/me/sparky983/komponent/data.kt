package me.sparky983.komponent

/**
 * DSL for building attributes.
 * 
 * Data attributes can be set using the attributes builder:
 * ```kotlin
 * fun Html.Counter() {
 *     val count = signal(0)
 *     button(
 *         onClick = { count.value++ }, 
 *         data = { "count" with count } // Sets the data-count attribute
 *     ) {
 *         text(count { "Count: $it" })
 *     }
 * }
 * ```
 *
 * @since 0.2.0
 */
public class AttributesBuilder internal constructor() {
    private val _attributes: MutableMap<String, Signal<String?>> = mutableMapOf()
    internal val attributes: Map<String, Signal<String?>> get() = _attributes

    /**
     * Assigns the given value to the attribute.
     *
     * @param value the value
     * @since 0.2.0
     */
    public infix fun String.with(value: Signal<String?>) {
        _attributes[this] = value
    }

    /**
     * Assigns the given boolean value to the attribute.
     *
     * @param value the value
     * @since 0.2.0
     */
    public infix fun String.with(value: Signal<Boolean>) {
        _attributes[this] = value { if (it) "" else null }
    }
}