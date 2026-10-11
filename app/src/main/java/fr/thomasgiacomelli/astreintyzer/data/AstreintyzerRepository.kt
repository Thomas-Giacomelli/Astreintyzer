package fr.thomasgiacomelli.astreintyzer.data

import android.content.Context
import fr.thomasgiacomelli.astreintyzer.data.entity.OnCallPeriodEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class AstreintyzerRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val periodDao = db.onCallPeriodDao()
    private val taskDao = db.taskDao()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val activePeriod: Flow<OnCallPeriodEntity?> = periodDao.getActivePeriod()

    fun getTasksForPeriod(periodId: Long): Flow<List<TaskEntity>> =
        taskDao.getTasksForPeriod(periodId)

    suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    suspend fun getActivePeriodSync(): OnCallPeriodEntity? = periodDao.getActivePeriodSync()
    suspend fun getPeriodById(id: Long): OnCallPeriodEntity? = periodDao.getPeriodById(id)
    suspend fun insertPeriod(period: OnCallPeriodEntity): Long = periodDao.insertPeriod(period)
    suspend fun updatePeriod(period: OnCallPeriodEntity) = periodDao.updatePeriod(period)

    suspend fun getTasksForPeriodSync(periodId: Long): List<TaskEntity> =
        taskDao.getTasksForPeriodSync(periodId)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
}
