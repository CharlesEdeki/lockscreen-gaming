package dev.lsg.games.dodge

import dev.lsg.engine.*
import kotlin.random.Random

class DodgeGame : Game {
    override val score: Int get() = (survived * 10f).toInt()
    override var status = Status.PLAYING; private set

    private var survived = 0f
    private var playerX = Screen.W / 2
    private var spawnTimer = 0f
    private var rng = Random(0)

    // Struct-of-arrays pool: no per-frame allocation.
    private val hx = FloatArray(MAX)
    private val hy = FloatArray(MAX)
    private val hw = FloatArray(MAX)
    private val hv = FloatArray(MAX)
    private val live = BooleanArray(MAX)

    override fun reset(seed: Long) {
        rng = Random(seed)
        survived = 0f; spawnTimer = 0f; playerX = Screen.W / 2
        live.fill(false); status = Status.PLAYING
    }

    override fun step(dt: Float, input: InputState) {
        survived += dt
        if (input.touching) playerX = input.x.coerceIn(SIZE / 2, Screen.W - SIZE / 2)

        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            spawn()
            spawnTimer = (0.55f - survived * 0.006f).coerceAtLeast(0.18f)
        }

        val left = playerX - SIZE / 2
        for (i in 0 until MAX) {
            if (!live[i]) continue
            hy[i] += hv[i] * dt
            if (hy[i] > Screen.H) { live[i] = false; continue }
            if (hy[i] + SIZE > PLAYER_Y && hy[i] < PLAYER_Y + SIZE && hx[i] + hw[i] > left && hx[i] < left + SIZE) {
                status = Status.OVER; return
            }
        }
    }

    private fun spawn() {
        val i = live.indexOfFirst { !it }
        if (i < 0) return
        val w = 12f + rng.nextFloat() * 22f
        hw[i] = w; hx[i] = rng.nextFloat() * (Screen.W - w); hy[i] = -SIZE
        hv[i] = 90f + survived * 3f + rng.nextFloat() * 40f
        live[i] = true
    }

    override fun render(c: Canvas2D) {
        c.clear(Palette.BG)
        for (i in 0 until MAX) if (live[i]) c.rect(hx[i], hy[i], hw[i], SIZE, Palette.WARN)
        c.rect(playerX - SIZE / 2, PLAYER_Y, SIZE, SIZE, Palette.ACCENT)
    }

    companion object {
        const val MAX = 32
        const val SIZE = 12f
        const val PLAYER_Y = 290f
        val info = GameInfo("dodge", "Dodge") { DodgeGame() }
    }
}
