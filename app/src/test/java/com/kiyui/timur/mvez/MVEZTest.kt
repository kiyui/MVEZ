package com.kiyui.timur.mvez

import org.junit.Test
import org.junit.Assert.*

/**
 * Created by timur on 2/18/18.
 */
class MVEZTest {
    private val application = "com.example.search"
    private val action = "fake.intent.call"
    private val query = "I am a fake search"

    @Test
    fun prefixReplace() {
        val mvez = MVEZ("g", application, action)
        val result = mvez.stripShortcut("!g $query")
        assertEquals(query, result)
    }

    @Test
    fun prefixNoSpaceReplace() {
        val mvez = MVEZ("g", application, action)
        val result = mvez.stripShortcut("!g$query")
        assertEquals(query, result)
    }

    @Test
    fun postfixReplace() {
        val mvez = MVEZ("g", application, action)
        val result = mvez.stripShortcut("$query !g")
        assertEquals(query, result)
    }

    @Test
    fun postfixNoSpaceReplace() {
        val mvez = MVEZ("g", application, action)
        val result = mvez.stripShortcut("$query!g")
        assertEquals(query, result)
    }

    @Test
    fun wrapperReplace() {
        val mvez = MVEZ("g", application, action)
        val result = mvez.stripShortcut("!g $query !g")
        assertEquals(query, result)
    }

    @Test
    fun substringReplace() {
        val mvez = MVEZ("g", application, action)
        val result = mvez.stripShortcut("$query !g you should not !g find me !g")
        assertEquals("$query !g you should not !g find me", result)
    }
}