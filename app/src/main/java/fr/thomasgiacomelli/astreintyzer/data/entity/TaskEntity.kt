package fr.thomasgiacomelli.astreintyzer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodId: Long,
    val callTimestamp: Long = 0L,
    val callDurationSeconds: Long = 0L,
    val taskDurationMinutes: Int = 0,
    val nature: String = "",
    val relocationNeeded: Boolean = false,
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
