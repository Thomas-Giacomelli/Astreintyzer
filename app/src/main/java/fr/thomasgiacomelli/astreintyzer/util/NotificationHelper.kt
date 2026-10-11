package fr.thomasgiacomelli.astreintyzer.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import fr.thomasgiacomelli.astreintyzer.MainActivity
import fr.thomasgiacomelli.astreintyzer.R
import fr.thomasgiacomelli.astreintyzer.receiver.NotificationActionReceiver

object NotificationHelper {
    const val CHANNEL_ID = "astreinte_call_channel_v2"
    const val NOTIFICATION_ID = 2001
    const val STARTUP_NOTIFICATION_ID = 2002

    const val ACTION_YES = "fr.thomasgiacomelli.astreintyzer.ACTION_YES"
    const val ACTION_NO = "fr.thomasgiacomelli.astreintyzer.ACTION_NO"

    const val EXTRA_CALL_TIMESTAMP = "extra_call_timestamp"
    const val EXTRA_CALL_DURATION = "extra_call_duration"

    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Notifications d'astreinte"
                val descriptionText = "Notifications envoyées suite à un appel pour enregistrer une astreinte"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.notif_sound}")
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    setSound(soundUri, audioAttributes)
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Erreur lors de la création du canal de notification", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun showCallEndedNotification(
        context: Context,
        callTimestamp: Long = System.currentTimeMillis(),
        callDurationSeconds: Long = 60L
    ) {
        createNotificationChannel(context)

        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.notif_sound}")

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CALL_TIMESTAMP, callTimestamp)
            putExtra(EXTRA_CALL_DURATION, callDurationSeconds)
            putExtra("open_create_task_dialog", true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val yesIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_YES
            putExtra(EXTRA_CALL_TIMESTAMP, callTimestamp)
            putExtra(EXTRA_CALL_DURATION, callDurationSeconds)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val noIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_NO
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            noIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messageText = "Cet appel déclenche un travail d'astreinte ?"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Astreintyzer")
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Oui", yesPendingIntent)
            .addAction(0, "Non", noPendingIntent)

        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Erreur lors de l'envoi de la notification", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun showStartupReminderNotification(context: Context) {
        createNotificationChannel(context)

        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.notif_sound}")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            10,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Astreintyzer - Surveillance requise"
        val message = "Le téléphone a redémarré. Veuillez ouvrir l'application pour activer la surveillance des appels d'astreinte."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(STARTUP_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Erreur lors de l'envoi de la notification de démarrage", e)
        }
    }

    fun cancelNotification(context: Context) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Erreur lors de l'annulation de la notification", e)
        }
    }
}
