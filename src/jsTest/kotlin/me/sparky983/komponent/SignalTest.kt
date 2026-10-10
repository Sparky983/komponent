package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import kotlin.test.Test
import kotlin.test.assertEquals

class SignalTest {
    @Test
    fun `Test mapped value`() {
        val source = signal(1)
        val mapped = source { it * 2 }

        assertEquals(2, mapped.value)
        source.value = 2
        assertEquals(4, mapped.value)
    }

    @Test
    fun `Test mapped subscription`() {
        val source = signal(1)
        val mapped = source { it * 2 }
        val received = mutableListOf<Int>()

        val subscription = mapped.subscribe { received.add(it) }
        assertEquals(listOf(2), received)
        source.value = 2
        assertEquals(listOf(2, 4), received)

        subscription.canceled = true
        source.value = 3
        assertEquals(listOf(2, 4), received)

        subscription.canceled = false
        source.value = 4
        assertEquals(listOf(2, 4, 8), received)
    }

    @Test
    fun `Test chained map`() {
        val source = signal(1)
        val mapped = source { it * 2 }.invoke { "value: $it" }
        val received = mutableListOf<String>()

        mapped.subscribe { received.add(it) }
        source.value = 2

        assertEquals("value: 4", mapped.value)
        assertEquals(listOf("value: 2", "value: 4"), received)
    }

    @Test
    fun `Test map without subscribers does not invoke mapper`() {
        val source = signal(1)
        var invocations = 0
        source { invocations++ }

        source.value = 2
        source.value = 3
        assertEquals(0, invocations)
    }

    @Test
    fun `Test chained map without subscribers does not invoke mappers`() {
        val source = signal(1)
        var inner = 0
        var outer = 0
        source { inner++ }.invoke { outer++ }

        source.value = 2
        source.value = 3
        assertEquals(0, inner)
        assertEquals(0, outer)
    }

    @Test
    fun `Test canceled mapped subscription does not invoke mapper`() {
        val source = signal(1)
        var invocations = 0
        val mapped = source { invocations++ }

        val subscription = mapped.subscribe {}
        assertEquals(1, invocations)
        subscription.canceled = true

        source.value = 2
        assertEquals(1, invocations)
    }

    @Test
    fun `Test unmounted text does not invoke mapper`() {
        val source = signal(0)
        val visible = signal(true)
        var invocations = 0
        lateinit var div: HTMLDivElement

        mount(document.body!!) {
            div = div {
                When(visible) {
                    text(source {
                        invocations++
                        it.toString()
                    })
                }
            }
        }
        val mounted = invocations
        source.value = 1
        assertEquals("1", div.textContent)
        assertEquals(mounted + 1, invocations)

        visible.value = false
        val unmounted = invocations
        source.value = 2
        source.value = 3
        assertEquals(unmounted, invocations)
    }

    @Test
    fun `Test mapped subscription replays to later subscriber`() {
        val mapped = signal(1).invoke { it * 2 }
        val received = mutableListOf<Int>()

        mapped.subscribe {}
        mapped.subscribe { received.add(it) }

        assertEquals(listOf(2), received)
    }

    @Test
    fun `Test mapped subscription replays after all subscriptions canceled`() {
        val source = signal(1)
        val mapped = source { it * 2 }
        val received = mutableListOf<Int>()

        mapped.subscribe {}.canceled = true
        source.value = 2
        mapped.subscribe { received.add(it) }

        assertEquals(listOf(4), received)
    }

    @Test
    fun `Test mapper invoked once per update for multiple subscribers`() {
        val source = signal(1)
        var invocations = 0
        val mapped = source {
            invocations++
            it
        }

        mapped.subscribe {}
        mapped.subscribe {}
        mapped.subscribe {}
        val subscribed = invocations
        source.value = 2

        assertEquals(subscribed + 1, invocations)
    }

    @Test
    fun `Test chained map invokes inner mapper once per update`() {
        val source = signal(1)
        var invocations = 0
        val inner = source {
            invocations++
            it
        }

        inner.subscribe {}
        inner.invoke { it * 2 }.subscribe {}
        val subscribed = invocations
        source.value = 2

        assertEquals(subscribed + 1, invocations)
    }

    @Test
    fun `Test just map`() {
        val mapped = just(1).invoke { it * 2 }
        val received = mutableListOf<Int>()

        mapped.subscribe { received.add(it) }

        assertEquals(2, mapped.value)
        assertEquals(listOf(2), received)
    }
}
