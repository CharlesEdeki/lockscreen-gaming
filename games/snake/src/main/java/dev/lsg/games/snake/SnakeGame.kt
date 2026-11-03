package dev.lsg.games.snake

import dev.lsg.engine.*
import kotlin.math.max
import kotlin.random.Random

class SnakeGame : Game {
    override var score = 0; private set
    override var status = Status.PLAYING; private set

    private val body = ArrayDeque<Int>() // head is last; cell = y * COLS + x
    private var dx = 1
    private var dy = 0
    private var food = 0
    private var timer = 0f
    private var rng = Random(0)

    override fun reset(seed: Long) {
        rng = Random(seed)
        body.clear()
        val cy = ROWS / 2
        for (x in 3..5) body.addLast(cy * COLS + x)
        dx = 1; dy = 0; score = 0; timer = 0f
        status = Status.PLAYING
        placeFood()
    }

    override fun step(dt: Float, input: InputState) {
        if (input.swipeX != 0 && dx == 0) { dx = input.swipeX; dy = 0 }
        else if (input.swipeY != 0 && dy == 0) { dy = input.swipeY; dx = 0 }

        timer += dt
        val interval = max(0.07f, 0.14f - 0.003f * score)
        if (timer < interval) return
        timer -= interval

        val head = body.last()
        val nx = head % COLS + dx
        val ny = head / COLS + dy
        if (nx !in 0 until COLS || ny !in 0 until ROWS) { status = Status.OVER; return }
        val next = ny * COLS + nx
        val growing = next == food
        // The tail cell frees up this tick unless we are growing.
        if (body.contains(next) && !(next == body.first() && !growing)) { status = Status.OVER; return }

        body.addLast(next)
        if (growing) { score++; placeFood() } else body.removeFirst()
    }

    private fun placeFood() {
        val free = (0 until COLS * ROWS).filter { it !in body }
        if (free.isEmpty()) { status = Status.OVER; return }
        food = free[rng.nextInt(free.size)]
    }

    override fun render(c: Canvas2D) {
        c.clear(Palette.BG)
        c.rect((food % COLS) * CELL + 3f, (food / COLS) * CELL + 3f, CELL - 6f, CELL - 6f, Palette.GOLD)
        for ((i, cell) in body.withIndex()) {
            val color = if (i == body.size - 1) Palette.FG else Palette.ACCENT
            c.rect((cell % COLS) * CELL + 1f, (cell / COLS) * CELL + 1f, CELL - 2f, CELL - 2f, color)
        }
    }

    companion object {
        const val COLS = 12
        const val ROWS = 21
        const val CELL = 15f
        val info = GameInfo("snake", "Snake") { SnakeGame() }
    }
}
