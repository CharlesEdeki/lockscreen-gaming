package dev.lsg.engine

import kotlin.math.abs

/** Turns raw pointer events (in virtual coordinates) into [InputState] taps and swipes. */
class GestureTracker(private val input: InputState, private val swipeThreshold: Float = 14f) {
    private var ox = 0f
    private var oy = 0f
    private var moved = false

    fun down(x: Float, y: Float) {
        input.touching = true; input.x = x; input.y = y
        ox = x; oy = y; moved = false
    }

    fun move(x: Float, y: Float) {
        input.x = x; input.y = y
        val dx = x - ox
        val dy = y - oy
        if (abs(dx) >= swipeThreshold || abs(dy) >= swipeThreshold) {
            moved = true
            if (abs(dx) > abs(dy)) { input.swipeX = if (dx > 0) 1 else -1; input.swipeY = 0 }
            else { input.swipeY = if (dy > 0) 1 else -1; input.swipeX = 0 }
            ox = x; oy = y // allow chained swipes within one drag
        }
    }

    fun up(x: Float, y: Float) {
        input.x = x; input.y = y; input.touching = false
        if (!moved) input.tapped = true
    }
}
