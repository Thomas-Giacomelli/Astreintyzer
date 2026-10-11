package fr.thomasgiacomelli.astreintyzer.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskDialog(
    initialTask: TaskEntity?,
    defaultCallDurationSeconds: Long = 0L,
    defaultCallTimestamp: Long = 0L,
    onDismiss: () -> Unit,
    onSaveTask: (
        nature: String,
        taskDurationMinutes: Int,
        relocationNeeded: Boolean,
        isResolved: Boolean,
        callTimestamp: Long,
        callDurationSeconds: Long
    ) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE) }

    val initialTimestamp = when {
        initialTask != null -> if (initialTask.callTimestamp > 0) initialTask.callTimestamp else initialTask.createdAt
        defaultCallTimestamp > 0 -> defaultCallTimestamp
        else -> System.currentTimeMillis()
    }

    val defaultCallDuration = when {
        initialTask != null -> initialTask.callDurationSeconds
        defaultCallDurationSeconds > 0 -> defaultCallDurationSeconds
        else -> 300L // 5 minutes par défaut pour une saisie manuelle (300 secondes)
    }

    val defaultMinutes = remember(initialTask, defaultCallDurationSeconds) {
        when {
            initialTask != null -> initialTask.taskDurationMinutes
            defaultCallDurationSeconds > 0 -> ((defaultCallDurationSeconds + 59) / 60).toInt().coerceAtLeast(1)
            else -> 15
        }
    }

    var nature by remember { mutableStateOf(initialTask?.nature ?: "") }
    var durationText by remember { mutableStateOf(defaultMinutes.toString()) }
    var dateTimeText by remember { mutableStateOf(dateFormat.format(Date(initialTimestamp))) }
    var relocationNeeded by remember { mutableStateOf(initialTask?.relocationNeeded ?: false) }
    var isResolved by remember { mutableStateOf(initialTask?.isResolved ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTask == null) "Nouvelle tâche d'astreinte" else "Modifier la tâche",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nature,
                    onValueChange = { nature = it },
                    label = { Text("Nature de la tâche") },
                    placeholder = { Text("Ex: Problème serveur, coupure réseau...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = dateTimeText,
                    onValueChange = { dateTimeText = it },
                    label = { Text("Date et heure (JJ/MM/AAAA HH:MM)") },
                    placeholder = { Text("Ex: 25/10/2025 14:30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { char -> char.isDigit() } },
                    label = { Text("Durée de la tâche (en minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = relocationNeeded,
                        onCheckedChange = { relocationNeeded = it }
                    )
                    Text(
                        text = "Déplacement nécessaire",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isResolved,
                        onCheckedChange = { isResolved = it }
                    )
                    Text(
                        text = "Tâche résolue",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = durationText.toIntOrNull() ?: defaultMinutes
                    val parsedTimestamp = try {
                        dateFormat.parse(dateTimeText)?.time ?: initialTimestamp
                    } catch (_: Exception) {
                        initialTimestamp
                    }
                    onSaveTask(
                        nature,
                        minutes,
                        relocationNeeded,
                        isResolved,
                        parsedTimestamp,
                        defaultCallDuration
                    )
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
