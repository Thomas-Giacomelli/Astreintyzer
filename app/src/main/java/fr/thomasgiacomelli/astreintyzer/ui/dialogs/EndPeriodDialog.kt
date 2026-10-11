package fr.thomasgiacomelli.astreintyzer.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.thomasgiacomelli.astreintyzer.data.entity.UserEntity

@Composable
fun EndPeriodDialog(
    userList: List<UserEntity>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onConfirmEndPeriod: (newUserId: Long) -> Unit
) {
    var selectedNewUserId by remember {
        mutableStateOf(userList.firstOrNull { it.id != currentUser?.id }?.id ?: userList.firstOrNull()?.id ?: 0L)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Mettre fin à l'astreinte",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cette action clôturera la période d'astreinte en cours et génèrera un récapitulatif par e-mail au format CSV.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Sélectionnez le nouvel utilisateur pour la prochaine astreinte :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                if (userList.isEmpty()) {
                    Text(
                        text = "Aucun autre utilisateur disponible. Veuillez d'abord en créer un dans le menu réglages.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        userList.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedNewUserId = user.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedNewUserId == user.id),
                                    onClick = { selectedNewUserId = user.id }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = user.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedNewUserId > 0L) {
                        onConfirmEndPeriod(selectedNewUserId)
                    }
                },
                enabled = selectedNewUserId > 0L
            ) {
                Text("Clôturer & Envoyer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
