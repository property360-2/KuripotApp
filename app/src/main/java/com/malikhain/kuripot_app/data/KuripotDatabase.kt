package com.malikhain.kuripot_app.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.malikhain.kuripot_app.data.dao.*
import com.malikhain.kuripot_app.data.entities.*

@Database(
    entities = [
        NoteEntity::class,
        CategoryEntity::class,
        BudgetEntryEntity::class,
        ArchiveEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KuripotDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetEntryDao(): BudgetEntryDao
    abstract fun archiveDao(): ArchiveDao
    abstract fun settingDao(): SettingDao
    
    companion object {
        @Volatile
        private var INSTANCE: KuripotDatabase? = null
        
        fun getDatabase(context: Context): KuripotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KuripotDatabase::class.java,
                    "kuripot_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 