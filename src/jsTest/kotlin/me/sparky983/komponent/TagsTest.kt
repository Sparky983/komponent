package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TagsTest {
    @Test
    fun `Test unmounted tag attribute change`() {
        val test = signal("1")
        val show = signal(true)
        lateinit var div: HTMLElement
        mount(document.body!!) {
            When(show) {
                div = div(
                    className = test, 
                    data = { "test" with test }
                ) {}
            }
        }
        show.value = false
        test.value = "2"
        show.value = true

        assertEquals("2", div.className)
        assertEquals("2", div.dataset["test"])
    }

    @Test
    fun `Test scoped namespaces and colliding names`() {
        lateinit var div: HTMLElement
        mount(document.body!!) {
            div = div {
                a {}
                svg {
                    a {
                        circle(cx = signal("5")) {}
                        title { text("SVG title") }
                    }
                }
                math {
                    mi { text("x") }
                }
            }
        }

        assertEquals("http://www.w3.org/1999/xhtml", (div.childNodes[0] as? org.w3c.dom.Element)?.namespaceURI)
        assertEquals("http://www.w3.org/2000/svg", (div.childNodes[1] as? org.w3c.dom.Element)?.namespaceURI)
        assertEquals("http://www.w3.org/2000/svg", (div.childNodes[1]?.childNodes?.get(0) as? org.w3c.dom.Element)?.namespaceURI)
        assertEquals("http://www.w3.org/1998/Math/MathML", (div.childNodes[2] as? org.w3c.dom.Element)?.namespaceURI)
        assertEquals("http://www.w3.org/1998/Math/MathML", (div.childNodes[2]?.childNodes?.get(0) as? org.w3c.dom.Element)?.namespaceURI)
    }

    @Test
    fun `Test generated event handler`() {
        var clicked = false
        lateinit var button: HTMLElement
        mount(document.body!!) {
            button = button(onClick = { clicked = true }) {}
        }

        button.dispatchEvent(Event("click"))
        assertTrue(clicked)
    }

    @Test
    fun `Test mount lifecycle hooks`() {
        val visible = signal(true)
        var mounted = false
        var unmounted = false

        mount(document.body!!) {
            When(visible) {
                svg {
                    onMount { mounted = true }
                    onUnmount { unmounted = true }
                }
            }
        }

        assertTrue(mounted)
        visible.value = false
        assertTrue(unmounted)
    }
}
