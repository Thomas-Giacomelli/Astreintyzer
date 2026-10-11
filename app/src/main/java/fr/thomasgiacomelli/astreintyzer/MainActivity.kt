package fr.thomasgiacomelli.astreintyzer

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import fr.thomasgiacomelli.astreintyzer.ui.dialogs.InfoDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import fr.thomasgiacomelli.astreintyzer.ui.AstreintyzerViewModel
import fr.thomasgiacomelli.astreintyzer.ui.components.TaskCard
import fr.thomasgiacomelli.astreintyzer.ui.dialogs.EndPeriodDialog
import fr.thomasgiacomelli.astreintyzer.ui.dialogs.TaskDialog
import fr.thomasgiacomelli.astreintyzer.ui.screens.SettingsDialog
import fr.thomasgiacomelli.astreintyzer.ui.theme.AstreintyzerTheme
import fr.thomasgiacomelli.astreintyzer.util.AlarmScheduler
import fr.thomasgiacomelli.astreintyzer.util.CallStateManager
import fr.thomasgiacomelli.astreintyzer.util.CsvExporter
import fr.thomasgiacomelli.astreintyzer.util.EmailHelper
import fr.thomasgiacomelli.astreintyzer.util.NotificationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AstreintyzerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NotificationHelper.STARTUP_NOTIFICATION_ID)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur lors de l'annulation de la notification de démarrage", e)
        }

        handleIncomingIntent(intent)

        setContent {
            AstreintyzerTheme {
                AstreintyzerMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val shouldOpenDialog = intent.getBooleanExtra("open_create_task_dialog", false)
        if (shouldOpenDialog) {
            val timestamp = intent.getLongExtra(NotificationHelper.EXTRA_CALL_TIMESTAMP, System.currentTimeMillis())
            val duration = intent.getLongExtra(NotificationHelper.EXTRA_CALL_DURATION, 60L)
            intent.removeExtra("open_create_task_dialog")

            viewModel.setPendingCallTaskData(timestamp, duration)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstreintyzerMainScreen(viewModel: AstreintyzerViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allUsers by viewModel.allUsers.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    val activePeriod by viewModel.activePeriod.collectAsState()
    val tasks by viewModel.tasksForActivePeriod.collectAsState()
    val pendingCallTaskData by viewModel.pendingCallTaskData.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showEndPeriodDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var isDebugMode by remember { mutableStateOf(CallStateManager.isDebugModeEnabled(context)) }

    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }

    var defaultCallTimestamp by remember { mutableLongStateOf(0L) }
    var defaultCallDurationSeconds by remember { mutableLongStateOf(0L) }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    var permissionsGranted by remember {
        mutableStateOf(checkAllPermissionsGranted(context, requiredPermissions))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionsGranted = checkAllPermissionsGranted(context, requiredPermissions)
    }

    LaunchedEffect(pendingCallTaskData) {
        pendingCallTaskData?.let { data ->
            defaultCallTimestamp = data.callTimestamp
            defaultCallDurationSeconds = data.callDurationSeconds
            editingTask = null
            showTaskDialog = true
            viewModel.clearPendingCallTaskData()
        }
    }

    LaunchedEffect(allUsers, activePeriod) {
        if (activePeriod == null && allUsers.isNotEmpty()) {
            viewModel.createDefaultPeriodIfNone(allUsers.first().id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Astreintyzer",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "À propos"
                        )
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Réglages"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (activePeriod != null) {
                FloatingActionButton(
                    onClick = {
                        editingTask = null
                        defaultCallTimestamp = 0L
                        defaultCallDurationSeconds = 0L
                        showTaskDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une tâche"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Technicien d'astreinte :",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = activeUser?.fullName ?: "Aucun utilisateur affecté",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        if (activePeriod != null) {
                            Button(
                                onClick = { showEndPeriodDialog = true }
                            ) {
                                Text("Fin d'astreinte")
                            }
                        }
                    }

                    if (!permissionsGranted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { launcher.launch(requiredPermissions) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Accorder les autorisations téléphone/notification")
                        }
                    }
                }
            }

            if (isDebugMode) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Options de Simulation / Debug",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    AlarmScheduler.scheduleNotification(
                                        context = context,
                                        delayMillis = 30_000L,
                                        callTimestamp = System.currentTimeMillis() - 120_000L,
                                        callDurationSeconds = 120L
                                    )
                                    Toast.makeText(
                                        context,
                                        "Notification simulée dans 30 secondes",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Simuler (30s)")
                            }

                            OutlinedButton(
                                onClick = {
                                    NotificationHelper.showCallEndedNotification(
                                        context = context,
                                        callTimestamp = System.currentTimeMillis() - 180_000L,
                                        callDurationSeconds = 180L
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Simuler (Immédiat)")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tâches de la période (${tasks.size}) :",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune tâche d'astreinte créée pour le moment.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(tasks, key = { task -> task.id }) { task ->
                        TaskCard(
                            task = task,
                            onEditTask = {
                                editingTask = it
                                defaultCallTimestamp = 0L
                                defaultCallDurationSeconds = 0L
                                showTaskDialog = true
                            },
                            onDeleteTask = { viewModel.deleteTask(it) }
                        )
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        TaskDialog(
            initialTask = editingTask,
            defaultCallDurationSeconds = defaultCallDurationSeconds,
            defaultCallTimestamp = defaultCallTimestamp,
            onDismiss = { showTaskDialog = false },
            onSaveTask = { nature, taskDurationMinutes, relocationNeeded, isResolved, callTimestamp, callDurationSeconds ->
                viewModel.saveTask(
                    existingTask = editingTask,
                    nature = nature,
                    taskDurationMinutes = taskDurationMinutes,
                    relocationNeeded = relocationNeeded,
                    isResolved = isResolved,
                    callTimestamp = callTimestamp,
                    callDurationSeconds = callDurationSeconds
                )
                showTaskDialog = false
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            userList = allUsers,
            isDebugMode = isDebugMode,
            onDebugModeChanged = { enabled ->
                CallStateManager.setDebugModeEnabled(context, enabled)
                isDebugMode = enabled
            },
            onSaveUser = { user -> viewModel.saveUser(user) },
            onDeleteUser = { user -> viewModel.deleteUser(user) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showInfoDialog) {
        InfoDialog(onDismiss = { showInfoDialog = false })
    }

    if (showEndPeriodDialog) {
        EndPeriodDialog(
            userList = allUsers,
            currentUser = activeUser,
            onDismiss = { showEndPeriodDialog = false },
            onConfirmEndPeriod = { newUserId ->
                scope.launch {
                    try {
                        val result = viewModel.closeCurrentPeriodAndStartNew(newUserId)
                        showEndPeriodDialog = false
                        if (result != null) {
                            val (closedPeriod, periodTasks) = result
                            val csvUri = CsvExporter.generateCsvAndGetUri(
                                context = context,
                                user = activeUser,
                                period = closedPeriod,
                                tasks = periodTasks
                            )
                            if (csvUri != null) {
                                EmailHelper.sendOnCallRecapEmail(
                                    context = context,
                                    user = activeUser,
                                    period = closedPeriod,
                                    tasks = periodTasks,
                                    csvUri = csvUri
                                )
                            }
                            Toast.makeText(context, "Période d'astreinte clôturée.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erreur lors de la clôture de la période", e)
                        Toast.makeText(context, "Erreur lors de la clôture de la période", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

private fun checkAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
    return permissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
