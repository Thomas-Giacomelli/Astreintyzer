package fr.thomasgiacomelli.astreintyzer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.thomasgiacomelli.astreintyzer.data.entity.OnCallPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OnCallPeriodDao {
    @Query("SELECT * FROM on_call_periods WHERE isClosed = 0 ORDER BY id DESC LIMIT 1")
    fun getActivePeriod(): Flow<OnCallPeriodEntity?>

    @Query("SELECT * FROM on_call_periods WHERE isClosed = 0 ORDER BY id DESC LIMIT 1")
    suspend fun getActivePeriodSync(): OnCallPeriodEntity?

    @Query("SELECT * FROM on_call_periods WHERE id = :id")
    suspend fun getPeriodById(id: Long): OnCallPeriodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: OnCallPeriodEntity): Long

    @Update
    suspend fun updatePeriod(period: OnCallPeriodEntity)
}
