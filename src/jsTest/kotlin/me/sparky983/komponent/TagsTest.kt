package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import kotlin.test.Test
import kotlin.test.assertEquals

class TagsTest {
    @Test
    fun `Test unmounted tag attribute change`() {
        val test = signal("1")
        val show = signal(true)
        lateinit var div: HTMLElement
        mount(document.body!!) {
            When(show) {
                div = div(className = test, data = { "test" with test }) {}
            }
        }
        show.value = false
        test.value = "2"
        show.value = true

        assertEquals("2", div.className)
        assertEquals("2", div.dataset["test"])
    }
}