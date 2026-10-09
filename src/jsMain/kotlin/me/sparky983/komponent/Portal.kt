package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.Node

/**
 * A component that renders its children to a [target] DOM element.
 *
 * Typically used to implement modals, toasts or other UI that needs to break
 * out of the UI tree.
 * 
 * @param target to render the children to
 * @param children the children
 * @throws IllegalStateException if [target] is not connected to the document
 * when the portal is mounted
 * @since 0.3.0
 */
public fun Html.Portal(target: Node = document.body!!, children: Children) {
    val contents = Fragment().also(children)
    val targetNode = Tag(target, contexts)
    onMount {
        check(target.isConnected) {
            "Portal target is not connected to the document"
        }
        targetNode.emit(contents)
    }
    onUnmount {
        contents.removeFromParent()
        targetNode.onUnmount()
    }
}