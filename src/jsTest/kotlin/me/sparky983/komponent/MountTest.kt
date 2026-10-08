package me.sparky983.komponent

import kotlinx.browser.document
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MountTest {
    @Test
    fun `Test mount to connected node fires onMount once`() {
        var onMount = 0

        mount(document.body!!) {
            onMount { onMount++ }
        }

        assertEquals(1, onMount)
    }

    @Test
    fun `Test mount to disconnected node fails`() {
        assertFailsWith<IllegalStateException> {
            mount(document.createElement("div")) {}
        }
    }
}