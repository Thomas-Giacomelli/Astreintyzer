package fr.thomasgiacomelli.astreintyzer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val prenom: String,
    val email: String
) {
    val fullName: String
        get() = "$prenom $nom".trim()
}
