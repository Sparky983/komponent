package me.sparky983.komponent

import kotlinx.browser.document

/**
 * A text element with the given content.
 *
 * @since 0.1.0
 */
public fun Html.text(content: String) {
    emit(Tag(document.createTextNode(content), contexts))
}

/**
 * A dynamic text component that updates with the given content.
 * 
 * The given content signal subscription is canceled when the element is 
 * unmounted.
 * 
 * @since 0.1.0
 */
public fun Html.text(content: Signal<String>) {
    val node = document.createTextNode("")
    val subscription = content.subscribe {
        node.data = it
    }
    val tag = Tag(node, contexts)
    tag.onMount {
        subscription.canceled = false
    }
    tag.onUnmount {
        subscription.canceled = true
    }
    emit(tag)
}