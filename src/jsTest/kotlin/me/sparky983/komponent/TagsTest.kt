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

    @Test
    fun `Test When recreates its children`() {
        val visible = signal(true)
        var renders = 0

        mount(document.body!!) {
            When(visible) {
                renders++
                div {}
            }
        }

        assertEquals(1, renders)
        visible.value = false
        visible.value = true
        assertEquals(2, renders)
    }

    @Test
    fun `Test When creates a new lifecycle when shown again`() {
        val visible = signal(true)
        var nextInstance = 0
        val mounted = mutableListOf<Int>()
        val unmounted = mutableListOf<Int>()

        mount(document.body!!) {
            When(visible) {
                val instance = ++nextInstance
                onMount { mounted += instance }
                onUnmount { unmounted += instance }
                div {}
            }
        }

        mounted.clear()
        visible.value = false
        assertEquals(listOf(1), unmounted)
        visible.value = true
        assertEquals(listOf(2), mounted)
        assertEquals(listOf(1), unmounted)
    }
}
