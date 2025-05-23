package com.daniela.pillbox.receivers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.os.Vibrator
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daniela.pillbox.R

/**
 * Broadcast receiver for handling alarm notifications
 */
class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        println("🔥 AlarmReceiver triggered!")

        val medicationName = intent.getStringExtra("medicationName") ?: "Medication"
        val dosage = intent.getStringExtra("dosage") ?: "1"
        val dosageUnit = intent.getStringExtra("dosageUnit") ?: "unit"
        val requestCode = intent.getIntExtra("request_code", -1)

        // 1. Create notification channel
        createHighPriorityChannel(context)

        (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(500)

        // 2. Build notification with high priority
        val notification = NotificationCompat.Builder(context, "HIGH_PRIORITY_CHANNEL_ID")
            .setSmallIcon(R.drawable.pill)
            .setContentTitle("Time for your medication")
            .setContentText("Take $dosage $dosageUnit of $medicationName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setVibrate(longArrayOf(1000, 1000))
            .build()

        // 3. Gives a unique ID for each notification
        NotificationManagerCompat.from(context).notify(
            requestCode, notification
        )
    }

    /**
     * Creates a notification channel for high priority notifications
     */
    private fun createHighPriorityChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "HIGH_PRIORITY_CHANNEL_ID",
                "Important Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Medication reminder alerts"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }

            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}