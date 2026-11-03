package dev.lsg.games.snake

import dev.lsg.engine.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SnakeGameTest {
    @Test fun hittingWallEndsGame() {
        val g = SnakeGame().also { it.reset(1) }
        val input = InputState()
        repeat(60 * 5) { if (g.status == Status.PLAYING) g.step(GameRunner.STEP, input) }
        assertEquals(Status.OVER, g.status)
    }

    @Test fun sameSeedIsDeterministic() {
        fun run(): Int {
            val g = SnakeGame().also { it.reset(42) }
            val i = InputState()
            repeat(120) { g.step(GameRunner.STEP, i) }
            return g.score
        }
        assertEquals(run(), run())
    }
}
