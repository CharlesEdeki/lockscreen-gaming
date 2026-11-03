package dev.lsg.engine

/** Fixed-timestep driver: render rate can vary, simulation always advances in [STEP] slices. */
class GameRunner(
    val game: Game,
    val input: InputState = InputState(),
    private val seedSource: () -> Long = { System.nanoTime() },
) {
    private var accumulator = 0f

    init { game.reset(seedSource()) }

    fun advance(frameSeconds: Float) {
        accumulator += frameSeconds.coerceIn(0f, MAX_FRAME)
        var steps = 0
        while (accumulator >= STEP && steps < MAX_STEPS) {
            if (game.status == Status.OVER) {
                if (input.tapped) game.reset(seedSource())
            } else {
                game.step(STEP, input)
            }
            input.consumeEvents()
            accumulator -= STEP
            steps++
        }
        if (steps == MAX_STEPS) accumulator = 0f // drop backlog instead of spiralling
    }

    fun render(c: Canvas2D) = game.render(c)

    companion object {
        const val STEP = 1f / 60f
        const val MAX_FRAME = 0.1f
        const val MAX_STEPS = 6
    }
}
