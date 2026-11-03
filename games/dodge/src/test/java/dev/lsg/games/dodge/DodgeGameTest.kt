package dev.lsg.games.dodge

import dev.lsg.engine.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DodgeGameTest {
    @Test fun standingStillEventuallyGetsHit() {
        val g = DodgeGame().also { it.reset(9) }
        val i = InputState()
        repeat(60 * 120) { if (g.status == Status.PLAYING) g.step(GameRunner.STEP, i) }
        assertEquals(Status.OVER, g.status)
    }

    @Test fun scoreGrowsWithTime() {
        val g = DodgeGame().also { it.reset(1) }
        repeat(60) { g.step(GameRunner.STEP, InputState()) }
        assertTrue(g.score > 0)
    }
}
