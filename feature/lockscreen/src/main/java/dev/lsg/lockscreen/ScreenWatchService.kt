package dev.lsg.lockscreen

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings

/**
 * Foreground service that listens for SCREEN_ON (cannot be declared in the manifest) and launches
 * [LockGameActivity] when the device is locked. Starting an activity from the background works
 * because the app holds the "draw over other apps" permission.
 */
class ScreenWatchService : Service() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val locked = getSystemService(KeyguardManager::class.java).isKeyguardLocked
            if (locked && Settings.canDrawOverlays(context)) {
                context.startActivity(
                    Intent(context, LockGameActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Lock screen games", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setContentTitle("Lock screen games are on")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(ID, notification)
        }
        registerReceiver(receiver, IntentFilter(Intent.ACTION_SCREEN_ON))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "lockscreen_games"
        private const val ID = 1

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ScreenWatchService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenWatchService::class.java))
        }
    }
}
