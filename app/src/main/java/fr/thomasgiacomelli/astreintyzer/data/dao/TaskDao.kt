package fr.thomasgiacomelli.astreintyzer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE periodId = :periodId ORDER BY createdAt DESC")
    fun getTasksForPeriod(periodId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE periodId = :periodId ORDER BY createdAt DESC")
    suspend fun getTasksForPeriodSync(periodId: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}
