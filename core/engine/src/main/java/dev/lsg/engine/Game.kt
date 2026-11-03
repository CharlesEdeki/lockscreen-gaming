package dev.lsg.engine

/** All games render into the same fixed virtual portrait screen; the host scales it to fit. */
object Screen {
    const val W = 180f
    const val H = 320f
}

/** Platform-agnostic drawing surface. Games never touch Android types, so they unit-test on the JVM. */
interface Canvas2D {
    fun clear(color: Int)
    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int)
}

enum class Status { PLAYING, OVER }

/** One-shot events (tap/swipe) are cleared by [GameRunner] after every fixed step. */
class InputState {
    var touching = false
    var x = Screen.W / 2
    var y = Screen.H / 2
    var tapped = false
    var swipeX = 0 // -1, 0, 1
    var swipeY = 0 // -1 (up), 0, 1 (down)

    fun consumeEvents() {
        tapped = false; swipeX = 0; swipeY = 0
    }
}

/** A game is a deterministic state machine: same seed + same inputs => same result. */
interface Game {
    val score: Int
    val status: Status
    fun reset(seed: Long)
    fun step(dt: Float, input: InputState)
    fun render(c: Canvas2D)
}

object Palette {
    const val BG = 0xFF0B0F1A.toInt()
    const val FG = 0xFFE8EEF9.toInt()
    const val ACCENT = 0xFF39D98A.toInt()
    const val WARN = 0xFFFF5C7A.toInt()
    const val GOLD = 0xFFFFC857.toInt()
    const val DIM = 0xFF2A3350.toInt()
}
