package com.daniela.pillbox.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import com.daniela.pillbox.data.models.Medication
import com.daniela.pillbox.data.models.Schedule
import com.daniela.pillbox.receivers.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {
    // Generate unique request code for each alarm (medicationId + schedule index + time)
    private fun generateRequestCode(medicationId: String, scheduleIndex: Int, time: String): Int {
        return (medicationId + scheduleIndex.toString() + time).hashCode()
    }

    fun scheduleAll(medication: Medication, schedules: List<Schedule>) {
        schedules.forEachIndexed { scheduleIndex, schedule ->
            schedule.times?.forEachIndexed { timeIndex, time ->
                scheduleAlarm(medication, schedule, scheduleIndex, timeIndex, time)
            }
        }
    }

    private fun scheduleAlarm(
        medication: Medication,
        schedule: Schedule,
        scheduleIndex: Int,
        timeIndex: Int,
        timeString: String,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        // Check for Android 12+ requirements
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            println("⚠️ Cannot schedule exact alarms - permission needed")
            // Launch permission request intent
            val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medicationName", medication.name)
            putExtra(
                "dosage",
                schedule.amounts?.getOrNull(timeIndex)?.toString() ?: medication.dosage
            )
            putExtra("dosageUnit", medication.dosageUnit)
            putExtra("medicationId", medication.userId)
            putExtra("scheduleIndex", scheduleIndex)
        }

        val requestCode = generateRequestCode(medication.userId, scheduleIndex, timeString)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeParts = timeString.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts.getOrNull(1)?.toInt() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelAll(medication: Medication, schedules: List<Schedule>) {
        schedules.forEachIndexed { scheduleIndex, schedule ->
            schedule.times?.forEach { time ->
                val requestCode = generateRequestCode(medication.userId, scheduleIndex, time)
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                val intent = Intent(context, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent?.let {
                    alarmManager.cancel(it)
                }
            }
        }
    }
}