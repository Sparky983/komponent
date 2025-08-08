package me.sparky983.komponent

/**
 * Represents a collection of data attributes and values.
 *
 * @since 0.2.0
 */
public class DataAttributes internal constructor(internal val attributes: Array<out DataValue>)

/**
 * Creates a data attribute.
 *
 * @return the data attribute
 * @since 0.2.0
 */
public fun attribute(name: String): DataAttribute = DataAttribute(name)

/**
 * Creates a data attribute list.
 *
 * @return the data attribute list
 * @since 0.2.0
 */
public fun data(vararg attributes: DataValue): DataAttributes {
    return DataAttributes(attributes)
}

/**
 * Represents a typed data attribute.
 *
 * @since 0.2.0
 */
public class DataAttribute internal constructor(internal val name: String) {
    public infix fun with(value: Signal<String?>): DataValue {
        return DataValue(name, value)
    }

    public infix fun with(value: Signal<Boolean>): DataValue {
        return DataValue(name, value { if (it) "" else null })
    }
}

/**
 * Represents a data attribute paired with its value.
 *
 * @since 0.2.0
 */
public class DataValue internal constructor(
    internal val name: String,
    internal val value: Signal<String?>
)
