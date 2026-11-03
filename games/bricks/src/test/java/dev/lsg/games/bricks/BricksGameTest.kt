package dev.lsg.games.bricks

import dev.lsg.engine.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BricksGameTest {
    @Test fun idleGameBreaksBricksOrLosesBall() {
        val g = BricksGame().also { it.reset(3) }
        val input = InputState()
        repeat(60 * 30) { if (g.status == Status.PLAYING) g.step(GameRunner.STEP, input) }
        assertTrue(g.status == Status.OVER || g.score > 0)
    }

    @Test fun resetRestoresState() {
        val g = BricksGame().also { it.reset(3) }
        repeat(600) { if (g.status == Status.PLAYING) g.step(GameRunner.STEP, InputState()) }
        g.reset(3)
        assertEquals(0, g.score)
        assertEquals(Status.PLAYING, g.status)
    }
}
