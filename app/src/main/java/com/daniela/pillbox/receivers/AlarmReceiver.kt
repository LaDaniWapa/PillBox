package com.daniela.pillbox.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "ACTION_SHOW_ALARM") {
            val medicationId = intent.getStringExtra("medicationId")
            // Show notification or perform other actions here

            val fullscreenIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
                putExtra("medicationId", medicationId)
            }

            context?.startActivity(fullscreenIntent)
        }
    }
}