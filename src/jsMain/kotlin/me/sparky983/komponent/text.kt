package me.sparky983.komponent

import kotlinx.browser.document

/**
 * A text element with the given content.
 *
 * @param content the text content
 * @since 0.1.0
 */
public fun Namespace.text(content: String) {
    element.emit(Tag<Namespace, Nothing>(document.createTextNode(content)))
}

/**
 * A dynamic text component that updates with the given content.
 * 
 * The given content signal subscription is canceled when the element is 
 * unmounted.
 * 
 * @param content the text content
 * @since 0.1.0
 */
public fun Namespace.text(content: Signal<String>) {
    val node = document.createTextNode("")
    val subscription = content.subscribe {
        node.data = it
    }
    val tag = Tag<Namespace, Nothing>(node)
    tag.onMount {
        subscription.canceled = false
    }
    tag.onUnmount {
        subscription.canceled = true
    }
    element.emit(tag)
}
