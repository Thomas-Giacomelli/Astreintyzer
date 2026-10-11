package fr.thomasgiacomelli.astreintyzer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.thomasgiacomelli.astreintyzer.data.dao.OnCallPeriodDao
import fr.thomasgiacomelli.astreintyzer.data.dao.TaskDao
import fr.thomasgiacomelli.astreintyzer.data.dao.UserDao
import fr.thomasgiacomelli.astreintyzer.data.entity.OnCallPeriodEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.TaskEntity
import fr.thomasgiacomelli.astreintyzer.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        OnCallPeriodEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun onCallPeriodDao(): OnCallPeriodDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "astreintyzer_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
