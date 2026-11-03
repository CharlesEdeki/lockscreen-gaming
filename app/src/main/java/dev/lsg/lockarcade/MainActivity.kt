package dev.lsg.lockarcade

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dev.lsg.lockscreen.ScreenWatchService

/** Minimal setup screen: grant overlay permission, then switch the lock screen games on or off. */
class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var toggle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply { textSize = 16f }
        val grant = Button(this).apply {
            text = "1. Allow drawing over other apps"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            }
        }
        val fullScreen = Button(this).apply {
            text = "2. Allow full-screen notifications"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= 34) {
                    startActivity(
                        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:$packageName"))
                    )
                }
            }
        }
        toggle = Button(this).apply { setOnClickListener { onToggle() } }

        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(48, 48, 48, 48)
                addView(TextView(context).apply { text = "Lock Arcade"; textSize = 28f })
                addView(status)
                addView(grant)
                addView(fullScreen)
                addView(toggle)
            }
        )
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun onToggle() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val enable = !prefs.getBoolean(KEY_ENABLED, false)
        if (enable) {
            if (!Settings.canDrawOverlays(this)) return refresh()
            if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            ScreenWatchService.start(this)
        } else {
            ScreenWatchService.stop(this)
        }
        prefs.edit().putBoolean(KEY_ENABLED, enable).apply()
        refresh()
    }

    private fun refresh() {
        val enabled = getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)
        val overlay = Settings.canDrawOverlays(this)
        status.text = "Overlay permission: ${if (overlay) "granted" else "needed"}\n" +
            "Lock screen games: ${if (enabled) "ON" else "OFF"}"
        toggle.text = if (enabled) "3. Turn off" else "3. Turn on"
    }

    private companion object {
        const val PREFS = "app"
        const val KEY_ENABLED = "enabled"
    }
}
