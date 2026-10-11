package fr.thomasgiacomelli.astreintyzer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.thomasgiacomelli.astreintyzer.util.NotificationHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                Log.i("BootReceiver", "Téléphone redémarré, envoi de la notification de rappel pour activer l'application.")
                NotificationHelper.showStartupReminderNotification(context)
            }
        } catch (e: Exception) {
            Log.e("BootReceiver", "Erreur lors du traitement du boot completed", e)
        }
    }
}
