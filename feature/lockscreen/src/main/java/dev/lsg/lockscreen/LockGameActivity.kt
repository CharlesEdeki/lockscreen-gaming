package dev.lsg.lockscreen

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import dev.lsg.engine.GameRunner

/** Shown over the keyguard. Picks a random game (never the same one twice in a row) each launch. */
class LockGameActivity : Activity() {

    private var view: GameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val info = LockScreenGames.catalog.pickRandom(excludeId = prefs.getString(KEY_LAST, null))
        prefs.edit().putString(KEY_LAST, info.id).apply()

        val gameView = GameView(this, GameRunner(info.create()))
        view = gameView

        val unlock = Button(this).apply {
            text = "Unlock"
            setOnClickListener {
                getSystemService(KeyguardManager::class.java)
                    .requestDismissKeyguard(this@LockGameActivity, null)
                finish()
            }
        }
        setContentView(
            FrameLayout(this).apply {
                addView(gameView)
                addView(unlock, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP or Gravity.END,
                ))
            }
        )
    }

    override fun onResume() {
        super.onResume()
        getSystemService(android.app.NotificationManager::class.java).cancel(ScreenWatchService.LAUNCH_ID)
        view?.resume()
    }
    override fun onPause() { view?.pause(); super.onPause() }

    private companion object {
        const val PREFS = "lockscreen_games"
        const val KEY_LAST = "last_game"
    }
}
