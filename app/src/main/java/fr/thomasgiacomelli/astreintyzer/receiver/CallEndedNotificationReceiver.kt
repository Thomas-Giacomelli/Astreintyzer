package fr.thomasgiacomelli.astreintyzer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.thomasgiacomelli.astreintyzer.util.AlarmScheduler
import fr.thomasgiacomelli.astreintyzer.util.NotificationHelper

class CallEndedNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val timestamp = intent.getLongExtra(AlarmScheduler.EXTRA_CALL_TIMESTAMP, System.currentTimeMillis())
            val duration = intent.getLongExtra(AlarmScheduler.EXTRA_CALL_DURATION, 60L)
            NotificationHelper.showCallEndedNotification(context, timestamp, duration)
        } catch (e: Exception) {
            Log.e("CallEndedNotificationReceiver", "Erreur lors de la réception de l'alarme de fin d'appel", e)
        }
    }
}
