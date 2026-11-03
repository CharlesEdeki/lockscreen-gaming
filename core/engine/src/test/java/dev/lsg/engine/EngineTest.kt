package dev.lsg.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private class CountingGame : Game {
    var steps = 0
    override val score = 0
    override var status = Status.PLAYING
    override fun reset(seed: Long) { steps = 0; status = Status.PLAYING }
    override fun step(dt: Float, input: InputState) { steps++ }
    override fun render(c: Canvas2D) {}
}

class EngineTest {
    @Test fun runnerUsesFixedSteps() {
        val g = CountingGame()
        GameRunner(g, seedSource = { 1L }).advance(1f / 30f)
        assertEquals(2, g.steps)
    }

    @Test fun runnerCapsCatchUp() {
        val g = CountingGame()
        GameRunner(g, seedSource = { 1L }).advance(5f)
        assertTrue(g.steps <= GameRunner.MAX_STEPS)
    }

    @Test fun tapRestartsFinishedGame() {
        val g = CountingGame()
        val r = GameRunner(g, seedSource = { 1L })
        r.advance(GameRunner.STEP); assertEquals(1, g.steps)
        g.status = Status.OVER
        r.input.tapped = true
        r.advance(GameRunner.STEP)
        assertEquals(Status.PLAYING, g.status)
        assertEquals(0, g.steps)
    }

    @Test fun gestureDetectsSwipeAndTap() {
        val i = InputState(); val t = GestureTracker(i)
        t.down(50f, 50f); t.move(80f, 52f); t.up(80f, 52f)
        assertEquals(1, i.swipeX); assertEquals(false, i.tapped)
        i.consumeEvents()
        t.down(50f, 50f); t.up(51f, 50f)
        assertTrue(i.tapped)
    }

    @Test fun catalogAvoidsImmediateRepeat() {
        val c = GameCatalog(listOf("a", "b", "c").map { id -> GameInfo(id, id) { CountingGame() } })
        val rnd = Random(7)
        repeat(200) { assertNotEquals("b", c.pickRandom(rnd, excludeId = "b").id) }
    }
}
