package fr.thomasgiacomelli.astreintyzer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.thomasgiacomelli.astreintyzer.data.AstreintyzerRepository
import fr.thomasgiacomelli.astreintyzer.data.entity.OnCallPeriodEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AstreintyzerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AstreintyzerRepository(application)

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePeriod: StateFlow<OnCallPeriodEntity?> = repository.activePeriod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForActivePeriod: StateFlow<List<TaskEntity>> = activePeriod
        .flatMapLatest { period ->
            if (period != null) {
                repository.getTasksForPeriod(period.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeUser = MutableStateFlow<UserEntity?>(null)
    val activeUser: StateFlow<UserEntity?> = _activeUser.asStateFlow()

    data class PendingCallTaskData(
        val callTimestamp: Long,
        val callDurationSeconds: Long
    )

    private val _pendingCallTaskData = MutableStateFlow<PendingCallTaskData?>(null)
    val pendingCallTaskData: StateFlow<PendingCallTaskData?> = _pendingCallTaskData.asStateFlow()

    init {
        viewModelScope.launch {
            repository.activePeriod.collect { period ->
                if (period != null) {
                    val user = repository.getUserById(period.userId)
                    _activeUser.value = user
                } else {
                    _activeUser.value = null
                }
            }
        }
    }

    fun setPendingCallTaskData(timestamp: Long, durationSeconds: Long) {
        _pendingCallTaskData.value = PendingCallTaskData(timestamp, durationSeconds)
    }

    fun clearPendingCallTaskData() {
        _pendingCallTaskData.value = null
    }

    fun saveUser(user: UserEntity) {
        viewModelScope.launch {
            if (user.id == 0L) {
                repository.insertUser(user)
            } else {
                repository.updateUser(user)
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun createDefaultPeriodIfNone(assignedUserId: Long) {
        viewModelScope.launch {
            val currentActive = repository.getActivePeriodSync()
            if (currentActive == null && assignedUserId > 0L) {
                val calendar = Calendar.getInstance()
                val startDate = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 7)
                val endDate = calendar.timeInMillis

                val newPeriod = OnCallPeriodEntity(
                    userId = assignedUserId,
                    startDate = startDate,
                    endDate = endDate,
                    isClosed = false
                )
                repository.insertPeriod(newPeriod)
            }
        }
    }

    fun saveTask(
        existingTask: TaskEntity?,
        nature: String,
        taskDurationMinutes: Int,
        relocationNeeded: Boolean,
        isResolved: Boolean,
        callTimestamp: Long,
        callDurationSeconds: Long
    ) {
        viewModelScope.launch {
            val period = repository.getActivePeriodSync()
            if (period != null) {
                if (existingTask != null) {
                    val updated = existingTask.copy(
                        nature = nature,
                        taskDurationMinutes = taskDurationMinutes,
                        relocationNeeded = relocationNeeded,
                        isResolved = isResolved
                    )
                    repository.updateTask(updated)
                } else {
                    val newTask = TaskEntity(
                        periodId = period.id,
                        callTimestamp = callTimestamp,
                        callDurationSeconds = callDurationSeconds,
                        taskDurationMinutes = taskDurationMinutes,
                        nature = nature,
                        relocationNeeded = relocationNeeded,
                        isResolved = isResolved
                    )
                    repository.insertTask(newTask)
                }
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    suspend fun closeCurrentPeriodAndStartNew(newUserId: Long): Pair<OnCallPeriodEntity, List<TaskEntity>>? {
        val currentPeriod = repository.getActivePeriodSync() ?: return null
        val tasks = repository.getTasksForPeriodSync(currentPeriod.id)

        val closedPeriod = currentPeriod.copy(isClosed = true)
        repository.updatePeriod(closedPeriod)

        val calendar = Calendar.getInstance()
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 7)
        val endDate = calendar.timeInMillis

        val newPeriod = OnCallPeriodEntity(
            userId = newUserId,
            startDate = startDate,
            endDate = endDate,
            isClosed = false
        )
        repository.insertPeriod(newPeriod)

        return Pair(closedPeriod, tasks)
    }
}
