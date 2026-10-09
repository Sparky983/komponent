package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import kotlin.test.Test
import kotlin.test.assertEquals

class ForTest {
    private fun container(): HTMLDivElement {
        val container = document.createElement("div") as HTMLDivElement
        document.body!!.appendChild(container)
        return container
    }

    @Test
    fun `Test initial render`() {
        val list = listSignalOf("a", "b", "c")
        val container = container()

        mount(container) {
            For(list) { text("$it;") }
        }

        assertEquals("a;b;c;", container.textContent)
    }

    @Test
    fun `Test mutations`() {
        val list = listSignalOf("a", "b", "c")
        val container = container()

        mount(container) {
            For(list) { text("$it;") }
        }

        list.add("d")
        assertEquals("a;b;c;d;", container.textContent)
        list.add(1, "e")
        assertEquals("a;e;b;c;d;", container.textContent)
        list.removeAt(2)
        assertEquals("a;e;c;d;", container.textContent)
        list[0] = "f"
        assertEquals("f;e;c;d;", container.textContent)
        list.clear()
        assertEquals("", container.textContent)
    }

    @Test
    fun `Test set before attached`() {
        val list = listSignalOf("a", "b")
        val container = container()

        mount(container) {
            For(list) { text("$it;") }
            list[0] = "c"
        }

        assertEquals("c;b;", container.textContent)
    }

    @Test
    fun `Test rows are not rerendered when shifted`() {
        val list = listSignalOf("a", "b")
        var renders = 0

        mount(container()) {
            For(list) { renders++ }
        }

        assertEquals(2, renders)
        list.add(0, "c")
        list.removeAt(1)
        assertEquals(3, renders)
    }

    @Test
    fun `Test indexed initial render`() {
        val list = listSignalOf("a", "b", "c")
        val container = container()

        mount(container) {
            For(list) { index, element ->
                text(index { "$it:$element;" })
            }
        }

        assertEquals("0:a;1:b;2:c;", container.textContent)
    }

    @Test
    fun `Test indexed mutations`() {
        val list = listSignalOf("a", "b", "c")
        val container = container()

        mount(container) {
            For(list) { index, element ->
                text(index { "$it:$element;" })
            }
        }

        list.add("d")
        assertEquals("0:a;1:b;2:c;3:d;", container.textContent)
        list.add(0, "e")
        assertEquals("0:e;1:a;2:b;3:c;4:d;", container.textContent)
        list.removeAt(2)
        assertEquals("0:e;1:a;2:c;3:d;", container.textContent)
        list[1] = "f"
        assertEquals("0:e;1:f;2:c;3:d;", container.textContent)
        list.clear()
        assertEquals("", container.textContent)
    }

    @Test
    fun `Test indexed rows are not rerendered when shifted`() {
        val list = listSignalOf("a", "b")
        val indices = mutableListOf<Signal<Int>>()

        mount(container()) {
            For(list) { index, _ -> indices.add(index) }
        }

        val (a, b) = indices
        list.add(0, "c")
        assertEquals(3, indices.size)
        assertEquals(1, a.value)
        assertEquals(2, b.value)
        list.removeAt(1)
        assertEquals(3, indices.size)
        assertEquals(1, b.value)
    }

    @Test
    fun `Test indexed only updates shifted rows`() {
        val list = listSignalOf("a", "b", "c")
        val updates = mutableListOf<String>()

        mount(container()) {
            For(list) { index, element ->
                index.subscribe { updates.add("$element=$it") }
            }
        }

        updates.clear()
        list.add(1, "d")
        assertEquals(listOf("d=1", "b=2", "c=3"), updates)
        updates.clear()
        list[0] = "e"
        assertEquals(listOf("e=0"), updates)
    }

    @Test
    fun `Test indexed clear does not update indices`() {
        val list = listSignalOf("a", "b", "c", "d")
        var updates = 0

        mount(container()) {
            For(list) { index, _ ->
                index.subscribe { updates++ }
            }
        }

        updates = 0
        list.clear()
        assertEquals(0, updates)
    }

    @Test
    fun `Test indexed range removal`() {
        val list = listSignalOf("a", "b", "c", "d")
        val container = container()

        mount(container) {
            For(list) { index, element ->
                text(index { "$it:$element;" })
            }
        }

        list.subList(1, 3).clear()
        assertEquals("0:a;1:d;", container.textContent)
    }
}