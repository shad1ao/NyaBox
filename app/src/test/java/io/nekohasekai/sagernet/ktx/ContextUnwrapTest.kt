package io.nekohasekai.sagernet.ktx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContextUnwrapTest {

    private class Node(val name: String, var parent: Node? = null)

    @Test
    fun unwrapSkipsThemeWrappersAndReturnsTheActivity() {
        val activity = Node("MainActivity")
        val wrapped = Node("ContextThemeWrapper", Node("ContextThemeWrapper", activity))
        assertEquals(activity, unwrap(wrapped, Node::parent) { it.takeIf { node -> node.name == "MainActivity" } })
    }

    @Test
    fun unwrapReturnsTheStartNodeWhenItAlreadyMatches() {
        val activity = Node("MainActivity")
        assertEquals(activity, unwrap(activity, Node::parent) { it.takeIf { node -> node.name == "MainActivity" } })
    }

    @Test
    fun unwrapReturnsNullWhenNothingMatches() {
        val wrapped = Node("ContextThemeWrapper", Node("Application"))
        assertNull(unwrap(wrapped, Node::parent) { it.takeIf { node -> node.name == "MainActivity" } })
    }

    @Test
    fun unwrapStopsOnACycle() {
        val outer = Node("ContextThemeWrapper")
        val inner = Node("ContextThemeWrapper", outer)
        outer.parent = inner
        assertNull(unwrap(outer, Node::parent) { null as String? })
    }
}