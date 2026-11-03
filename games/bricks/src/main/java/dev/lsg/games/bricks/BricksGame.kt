package dev.lsg.games.bricks

import dev.lsg.engine.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class BricksGame : Game {
    override var score = 0; private set
    override var status = Status.PLAYING; private set

    private var paddleX = Screen.W / 2
    private var bx = 0f; private var by = 0f
    private var vx = 0f; private var vy = 0f
    private var speed = BASE_SPEED
    private val alive = BooleanArray(COLS * ROWS)

    override fun reset(seed: Long) {
        val rng = Random(seed)
        score = 0; speed = BASE_SPEED; status = Status.PLAYING
        paddleX = Screen.W / 2
        alive.fill(true)
        bx = paddleX; by = PADDLE_Y - 10f
        vx = speed * (rng.nextFloat() * 0.8f - 0.4f); vy = -speed
    }

    override fun step(dt: Float, input: InputState) {
        if (input.touching) paddleX = input.x.coerceIn(PADDLE_W / 2, Screen.W - PADDLE_W / 2)

        bx += vx * dt; by += vy * dt
        if (bx < RADIUS) { bx = RADIUS; vx = abs(vx) }
        if (bx > Screen.W - RADIUS) { bx = Screen.W - RADIUS; vx = -abs(vx) }
        if (by < RADIUS) { by = RADIUS; vy = abs(vy) }

        // Paddle: bounce angle depends on where the ball lands.
        if (vy > 0 && by + RADIUS >= PADDLE_Y && by - RADIUS <= PADDLE_Y + PADDLE_H &&
            abs(bx - paddleX) <= PADDLE_W / 2 + RADIUS
        ) {
            val offset = ((bx - paddleX) / (PADDLE_W / 2)).coerceIn(-1f, 1f)
            vx = speed * offset * 0.9f
            vy = -sqrt((speed * speed - vx * vx).coerceAtLeast(speed * speed * 0.25f))
            by = PADDLE_Y - RADIUS
        }

        // Brick hit: a point test on the ball centre keeps this cheap and allocation-free.
        val col = (bx / BRICK_W).toInt()
        val row = ((by - TOP) / BRICK_H).toInt()
        if (by >= TOP && col in 0 until COLS && row in 0 until ROWS && alive[row * COLS + col]) {
            alive[row * COLS + col] = false
            vy = -vy
            score += 10
            if (alive.none { it }) {
                speed *= 1.1f
                alive.fill(true)
                vx = vx.coerceIn(-speed, speed)
                vy = -speed
            }
        }

        if (by > Screen.H + RADIUS) status = Status.OVER
    }

    override fun render(c: Canvas2D) {
        c.clear(Palette.BG)
        val colors = intArrayOf(Palette.WARN, Palette.GOLD, Palette.ACCENT, Palette.FG, Palette.DIM)
        for (r in 0 until ROWS) for (col in 0 until COLS) {
            if (alive[r * COLS + col]) {
                c.rect(col * BRICK_W + 1, TOP + r * BRICK_H + 1, BRICK_W - 2, BRICK_H - 2, colors[r % colors.size])
            }
        }
        c.rect(paddleX - PADDLE_W / 2, PADDLE_Y, PADDLE_W, PADDLE_H, Palette.FG)
        c.rect(bx - RADIUS, by - RADIUS, RADIUS * 2, RADIUS * 2, Palette.GOLD)
    }

    companion object {
        const val COLS = 8
        const val ROWS = 5
        const val BRICK_W = Screen.W / COLS
        const val BRICK_H = 10f
        const val TOP = 30f
        const val PADDLE_W = 38f
        const val PADDLE_H = 6f
        const val PADDLE_Y = 296f
        const val RADIUS = 3f
        const val BASE_SPEED = 140f
        val info = GameInfo("bricks", "Bricks") { BricksGame() }
    }
}
