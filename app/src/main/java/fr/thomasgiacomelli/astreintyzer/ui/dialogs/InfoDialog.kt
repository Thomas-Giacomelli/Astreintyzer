package fr.thomasgiacomelli.astreintyzer.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun InfoDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/Thomas-Giacomelli/Astreintyzer"
    val email = "giacomelli.thomas@gmail.com"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "À propos d'Astreintyzer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Astreintyzer est une application de gestion et de suivi de vos périodes d'astreinte via surveillance des appels téléphoniques.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Auteur :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Thomas Giacomelli",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Contact :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )

                Text(
                    text = "GitHub :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = githubUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
