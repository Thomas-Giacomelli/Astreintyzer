package fr.thomasgiacomelli.astreintyzer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.thomasgiacomelli.astreintyzer.MainActivity
import fr.thomasgiacomelli.astreintyzer.util.NotificationHelper

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            NotificationHelper.cancelNotification(context)

            when (intent.action) {
                NotificationHelper.ACTION_YES -> {
                    val callTimestamp = intent.getLongExtra(NotificationHelper.EXTRA_CALL_TIMESTAMP, System.currentTimeMillis())
                    val callDuration = intent.getLongExtra(NotificationHelper.EXTRA_CALL_DURATION, 60L)

                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("open_create_task_dialog", true)
                        putExtra(NotificationHelper.EXTRA_CALL_TIMESTAMP, callTimestamp)
                        putExtra(NotificationHelper.EXTRA_CALL_DURATION, callDuration)
                    }
                    context.startActivity(launchIntent)
                }
                NotificationHelper.ACTION_NO -> {
                    // Rien d'autre à faire
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationActionReceiver", "Erreur lors du traitement de l'action de notification", e)
        }
    }
}
