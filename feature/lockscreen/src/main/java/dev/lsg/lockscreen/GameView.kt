package dev.lsg.lockscreen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import dev.lsg.engine.Canvas2D
import dev.lsg.engine.GameRunner
import dev.lsg.engine.GestureTracker
import dev.lsg.engine.Palette
import dev.lsg.engine.Screen
import dev.lsg.engine.Status
import kotlin.math.min

/** Hosts a [GameRunner]: drives it from Choreographer, letterboxes the virtual screen, forwards touches. */
class GameView(context: Context, private val runner: GameRunner) : View(context) {

    private val rectPaint = Paint().apply { isAntiAlias = false }
    private val textPaint = Paint().apply {
        isAntiAlias = false
        color = Palette.FG
        typeface = android.graphics.Typeface.MONOSPACE
        textSize = 14f
    }
    private val gestures = GestureTracker(runner.input)
    private var target: Canvas? = null

    private val adapter = object : Canvas2D {
        override fun clear(color: Int) {
            rectPaint.color = color
            target?.drawRect(0f, 0f, Screen.W, Screen.H, rectPaint)
        }

        override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
            rectPaint.color = color
            target?.drawRect(x, y, x + w, y + h, rectPaint)
        }
    }

    private var scale = 1f
    private var offX = 0f
    private var offY = 0f
    private var lastFrameNs = 0L
    private var running = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            if (lastFrameNs != 0L) runner.advance((frameTimeNanos - lastFrameNs) / 1_000_000_000f)
            lastFrameNs = frameTimeNanos
            invalidate()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun resume() {
        if (running) return
        running = true
        lastFrameNs = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun pause() {
        running = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        scale = min(w / Screen.W, h / Screen.H)
        offX = (w - Screen.W * scale) / 2f
        offY = (h - Screen.H * scale) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(0xFF000000.toInt())
        canvas.save()
        canvas.translate(offX, offY)
        canvas.scale(scale, scale)
        canvas.clipRect(0f, 0f, Screen.W, Screen.H)
        target = canvas
        runner.render(adapter)
        canvas.drawText("SCORE ${runner.game.score}", 4f, 14f, textPaint)
        if (runner.game.status == Status.OVER) {
            canvas.drawText("GAME OVER", 40f, Screen.H / 2, textPaint)
            canvas.drawText("tap to retry", 34f, Screen.H / 2 + 18f, textPaint)
        }
        target = null
        canvas.restore()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = (e.x - offX) / scale
        val y = (e.y - offY) / scale
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> gestures.down(x, y)
            MotionEvent.ACTION_MOVE -> gestures.move(x, y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> gestures.up(x, y)
        }
        return true
    }
}
