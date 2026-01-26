package me.sparky983.komponent

import kotlinx.browser.document
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

class ContextsTest {
    @Test
    fun `Test inner context`() {
        val outer = listOf(1, 2, 3)
        val inner = listOf(3, 2, 1)
        lateinit var actualInner: List<Int>
        lateinit var actualOuter: List<Int>

        mount(document.body!!) {
            Provide<List<Int>>(outer) {
                Provide<List<Int>>(inner) {
                    actualInner = context<List<Int>>()
                }
                actualOuter = context<List<Int>>()
            }
        }

        assertSame(inner, actualInner)
        assertSame(outer, actualOuter)
    }
    
    @Test
    fun `Test persistent context`() {
        lateinit var getContext: () -> String
        
        mount(document.body!!) {
            Provide<String>("context") {
                getContext = { context<String>() }
            }
        }
        
        assertEquals("context", getContext())
    }

    @Test
    fun `Test no context`() {
        lateinit var exception: Throwable
        
        mount(document.body!!) {
            try {
                context<Int>(message = "Some message")
                fail()
            } catch (e: Throwable) {
                exception = e
            }
        }

        assertTrue(exception is IllegalStateException)
        assertEquals("Some message", exception.message)
    }
}