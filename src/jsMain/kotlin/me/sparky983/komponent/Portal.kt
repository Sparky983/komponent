package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.Node

/**
 * A component that renders its children to a [target] HTML DOM node.
 *
 * Typically used to implement modals, toasts or other UI that needs to break
 * out of the UI tree.
 * 
 * @param target to render the children to
 * @param children the children
 * @since 0.3.0
 */
public fun Namespace.Portal(target: Node = document.body!!, children: Html.() -> Unit) {
    val targetNode = Tag<Html, Html>(target)
    val contents = Html(targetNode, contexts).Fragment().apply {
        render(children)
    }
    onMount {
        contents.emitSelf()
    }
    onUnmount {
        targetNode.unmount()
        @OptIn(Fragment.RemoveFromParent::class)
        contents.removeFromParent()
    }
}
