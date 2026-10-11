package fr.thomasgiacomelli.astreintyzer.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import fr.thomasgiacomelli.astreintyzer.data.entity.OnCallPeriodEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.UserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmailHelper {

    fun sendOnCallRecapEmail(
        context: Context,
        user: UserEntity?,
        period: OnCallPeriodEntity,
        tasks: List<TaskEntity>,
        csvUri: Uri
    ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
        val startDateStr = dateFormat.format(Date(period.startDate))
        val endDateStr = dateFormat.format(Date(period.endDate))

        val subject = "Récapitulatif d'astreinte - ${user?.fullName ?: "Inconnu"} ($startDateStr - $endDateStr)"

        val body = StringBuilder().apply {
            append("Bonjour ${user?.prenom ?: ""},\n\n")
            append("Voici le récapitulatif de votre période d'astreinte du $startDateStr au $endDateStr.\n\n")
            append("Nombre de tâches enregistrées : ${tasks.size}\n")
            val totalMinutes = tasks.sumOf { it.taskDurationMinutes }
            append("Durée totale des tâches : $totalMinutes minutes\n\n")
            append("Vous trouverez en pièce jointe le fichier CSV détaillé.\n\n")
            append("Cordialement,\nL'application Astreintyzer")
        }.toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(user?.email ?: ""))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, csvUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Envoyer le récapitulatif d'astreinte").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e("EmailHelper", "Aucune application de messagerie trouvée", e)
            Toast.makeText(
                context,
                "Aucune application de messagerie trouvée pour envoyer l'e-mail",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
