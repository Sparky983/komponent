package me.sparky983.komponent

import kotlinx.browser.document

/**
 * A text element with the given content.
 *
 * @since 0.1.0
 */
public fun text(content: String): Element {
    return DomElement(document.createTextNode(content))
}

/**
 * A dynamic text component that updates with the given content.
 * 
 * The given content signal subscription is canceled when the element is 
 * unmounted.
 * 
 * @since 0.1.0
 */
public fun text(content: Signal<String>): Element {
    val node = document.createTextNode("")
    content.subscribe { node.data = it }
    return DomElement(node)
}