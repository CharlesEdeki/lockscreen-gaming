package dev.lsg.lockscreen

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
            if (!getSystemService(KeyguardManager::class.java).isKeyguardLocked) return
            val launch = Intent(context, LockGameActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Route 1: direct launch (works where the overlay exemption is honoured).
            if (Settings.canDrawOverlays(context)) {
                try { context.startActivity(launch) } catch (_: Exception) {}
            }

            // Route 2: full-screen-intent notification, the sanctioned way to appear over the keyguard.
            val pending = PendingIntent.getActivity(
                context, 0, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val n = Notification.Builder(context, LAUNCH_CHANNEL)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Play a quick game")
                .setCategory(Notification.CATEGORY_ALARM)
                .setFullScreenIntent(pending, true)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()
            getSystemService(NotificationManager::class.java).notify(LAUNCH_ID, n)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Lock screen games", NotificationManager.IMPORTANCE_MIN)
        )
        nm.createNotificationChannel(
            NotificationChannel(LAUNCH_CHANNEL, "Game launcher", NotificationManager.IMPORTANCE_HIGH)
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
        private const val LAUNCH_CHANNEL = "lockscreen_launch"
        const val LAUNCH_ID = 2

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ScreenWatchService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenWatchService::class.java))
        }
    }
}
