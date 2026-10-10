package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import kotlin.test.Test
import kotlin.test.assertEquals

class FragmentTest {
    private fun container(): HTMLDivElement {
        val container = document.createElement("div") as HTMLDivElement
        document.body!!.appendChild(container)
        return container
    }

    @Test
    fun `Test out-of-declared-order emissions`() {
        val container = container()
        lateinit var emit: () -> Unit
        var i = 2
        mount(container) {
            li { text("First") }
            Fragment {
                emit = {
                    li { text("${i++}") }
                }
            }
            li { text("Last") }
        }
        emit()
        emit()
        emit()

        assertEquals(5, container.children.length)
        assertEquals("First", container.children.item(0)?.textContent)
        assertEquals("2", container.children.item(1)?.textContent)
        assertEquals("3", container.children.item(2)?.textContent)
        assertEquals("4", container.children.item(3)?.textContent)
        assertEquals("Last", container.children.item(4)?.textContent)
    }
}
