package fr.thomasgiacomelli.astreintyzer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "on_call_periods")
data class OnCallPeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val startDate: Long,
    val endDate: Long,
    val isClosed: Boolean = false
)
