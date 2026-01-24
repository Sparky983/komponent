package me.sparky983.komponent

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Text
import kotlin.test.Test
import kotlin.test.assertEquals

class PortalTest {
    @Test
    fun `Test mounting to target`() {
        val visible = signal(false)
        lateinit var target: HTMLDivElement
        mount(document.body!!) {
            target = div {}
            When(visible) {
                Portal(target = target) {
                    text("hello, world!")
                }
            }
        }
        assertEquals(0, target.childNodes.length)
        visible.value = true
        assertEquals(2, target.childNodes.length)
        assertEquals("hello, world!", (target.childNodes.item(0) as Text).data)
        assertEquals("", (target.childNodes.item(1) as Text).data) // marker
        visible.value = false
        assertEquals(0, target.childNodes.length)
    }
    
    @Test
    fun `Test mount hooks`() {
        val visible = signal(false)
        var onMount = 0
        var onUnmount = 0
        
        mount(document.body!!) {
            When(visible) {
                Portal {
                    onMount { onMount++ }
                    onUnmount { onUnmount++ }
                }
            }
        }
        
        assertEquals(0, onMount)
        assertEquals(0, onUnmount)
        visible.value = true
        assertEquals(1, onMount)
        assertEquals(0, onUnmount)
        visible.value = false
        assertEquals(1, onMount)
        assertEquals(1, onUnmount)
    }
}