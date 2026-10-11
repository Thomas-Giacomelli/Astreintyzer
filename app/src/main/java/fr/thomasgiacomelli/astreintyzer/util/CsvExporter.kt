package fr.thomasgiacomelli.astreintyzer.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import fr.thomasgiacomelli.astreintyzer.data.entity.OnCallPeriodEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.UserEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun generateCsvAndGetUri(
        context: Context,
        user: UserEntity?,
        period: OnCallPeriodEntity,
        tasks: List<TaskEntity>
    ): Uri? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
            val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE)

            val csvBuilder = StringBuilder()
            csvBuilder.append("Astreintier;${user?.fullName ?: "Inconnu"}\n")
            csvBuilder.append("Email;${user?.email ?: ""}\n")
            csvBuilder.append("Debut Periode;${dateFormat.format(Date(period.startDate))}\n")
            csvBuilder.append("Fin Periode;${dateFormat.format(Date(period.endDate))}\n\n")

            csvBuilder.append("Date/Heure Appel;Duree Appel (sec);Duree Tache (min);Nature;Deplement;Resolut\n")

            for (task in tasks) {
                val dateStr = if (task.callTimestamp > 0) dateFormat.format(Date(task.callTimestamp)) else dateFormat.format(Date(task.createdAt))
                val natureEscaped = task.nature.replace(";", ",").replace("\n", " ")
                val relocationStr = if (task.relocationNeeded) "Oui" else "Non"
                val resolvedStr = if (task.isResolved) "Oui" else "Non"

                csvBuilder.append("$dateStr;${task.callDurationSeconds};${task.taskDurationMinutes};$natureEscaped;$relocationStr;$resolvedStr\n")
            }

            val fileName = "astreinte_recap_${fileDateFormat.format(Date())}.csv"
            val cacheDir = File(context.cacheDir, "csv_exports").apply { mkdirs() }
            val file = File(cacheDir, fileName)
            file.writeText(csvBuilder.toString(), Charsets.UTF_8)

            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            Log.e("CsvExporter", "Erreur lors de la génération du fichier CSV", e)
            null
        }
    }
}
