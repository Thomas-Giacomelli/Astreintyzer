package fr.thomasgiacomelli.astreintyzer.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.thomasgiacomelli.astreintyzer.receiver.CallEndedNotificationReceiver

object AlarmScheduler {
    private const val REQUEST_CODE_NOTIFICATION = 1001

    const val EXTRA_CALL_TIMESTAMP = "extra_call_timestamp"
    const val EXTRA_CALL_DURATION = "extra_call_duration"

    fun scheduleNotification(
        context: Context,
        delayMillis: Long = 1_000L,
        callTimestamp: Long = System.currentTimeMillis(),
        callDurationSeconds: Long = 60L
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CallEndedNotificationReceiver::class.java).apply {
            putExtra(EXTRA_CALL_TIMESTAMP, callTimestamp)
            putExtra(EXTRA_CALL_DURATION, callDurationSeconds)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_NOTIFICATION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + delayMillis

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "setAndAllowWhileIdle a échoué, tentative avec set", e)
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e("AlarmScheduler", "Impossible de programmer l'alarme", ex)
            }
        }
    }
}
