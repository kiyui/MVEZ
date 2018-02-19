package com.kiyui.timur.mvez

import org.junit.Test
import org.junit.Assert.*

/**
 * Created by timur on 2/18/18.
 */
class MVEZPreferencesTest {
    private val query = "I am an example query woohoo"
    private val gApp = MVEZ("g", "com.example.1", "intent.1")
    private val hApp = MVEZ("h", "com.example.2", "intent.2")
    private val jApp = MVEZ("j", "com.example.3", "intent.3")
    private val mvezPreferences by lazy {
        val internal = MVEZPreferences()
        internal.add(gApp)
        internal.add(hApp)
        internal.add(jApp)
        internal
    }

    @Test
    fun noDuplicateAdd () {
        val testPreferences = MVEZPreferences()
        val app1 = MVEZ("a", "com.example.a", "intent.a")
        val app2 = MVEZ("a", "com.example.a", "intent.a")

        assertTrue(testPreferences.add(app1))
        assertEquals(testPreferences.getLabels(), listOf(app1.toString()))

        assertFalse(testPreferences.add(app2))
        assertEquals(testPreferences.getLabels(), listOf(app1.toString()))
    }

    @Test
    fun canRemoveAdd () {
        val testPreferences = MVEZPreferences()
        val app1 = MVEZ("a", "com.example.a", "intent.a")
        val app2 = MVEZ("b", "com.example.b", "intent.b")

        assertTrue(testPreferences.add(app1))
        assertTrue(testPreferences.add(app2))
        assertEquals(testPreferences.getLabels(), listOf(app1.toString(), app2.toString()))

        assertTrue(testPreferences.remove(0))
        assertEquals(testPreferences.getLabels(), listOf(app2.toString()))

        assertTrue(testPreferences.remove(0))
        assertEquals(testPreferences.getLabels().size, 0)
    }

    @Test
    fun cannotRemoveMissing () {
        val testPreferences = MVEZPreferences()
        testPreferences.add(gApp)
        testPreferences.add(hApp)
        testPreferences.add(jApp)

        assertFalse(testPreferences.remove(3))
        assertFalse(testPreferences.remove(-1))
    }

    @Test
    fun findFirst() {
        val result = mvezPreferences.getActionableMVEZ("!g $query") as MVEZ
        assertTrue(result.equals(gApp))
        assertFalse(result.equals(hApp))
        assertFalse(result.equals(jApp))
    }

    @Test
    fun findMiddle() {
        val result = mvezPreferences.getActionableMVEZ("!h $query") as MVEZ
        assertFalse(result.equals(gApp))
        assertTrue(result.equals(hApp))
        assertFalse(result.equals(jApp))
    }

    @Test
    fun findLast() {
        val result = mvezPreferences.getActionableMVEZ("!j $query") as MVEZ
        assertFalse(result.equals(gApp))
        assertFalse(result.equals(hApp))
        assertTrue(result.equals(jApp))
    }
}