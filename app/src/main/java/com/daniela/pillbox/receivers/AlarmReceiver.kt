package com.daniela.pillbox.receivers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daniela.pillbox.R
import com.daniela.pillbox.activity.MainActivity

/**
 * Broadcast receiver for handling alarm notifications
 */
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_PREFIX_TEST_ALARM = "com.daniela.pillbox.TEST_ALARM_"
        const val MEDICATION_REMINDER_CHANNEL_ID = "MEDICATION_REMINDER_CHANNEL"
        const val MEDICATION_REMINDER_CHANNEL_NAME = "Medication Reminder"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val receivedAction = intent.action

        if (receivedAction == null || !receivedAction.startsWith(ACTION_PREFIX_TEST_ALARM)) {
            Log.e("AlarmReceiver", "Invalid action received for medication reminder. Aborting.")
            return
        }

        Log.d("AlarmReceiver", "🔥 Test AlarmReceiver triggered with action: $receivedAction")

        val medicationName = intent.getStringExtra("medicationName") ?: "Your Medication"
        val dosage = intent.getStringExtra("dosage") ?: "As prescribed"
        val dosageUnit = intent.getStringExtra("dosageUnit") ?: ""
        val requestCode = intent.getIntExtra("request_code", -1)

        if (requestCode == -1) {
            Log.e(
                "AlarmReceiver",
                "Invalid request code received for medication reminder. Aborting."
            )
            return
        }

        createMedicationReminderChannel(context)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        val notification = NotificationCompat.Builder(context, MEDICATION_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.pill)
            .setContentTitle(context.getString(R.string.time_to_take_your_medication))
            .setContentText("")
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
    private fun createMedicationReminderChannel(context: Context) {
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