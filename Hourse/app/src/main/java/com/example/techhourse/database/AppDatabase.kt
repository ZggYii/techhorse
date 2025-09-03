package com.example.techhourse.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.techhourse.database.dao.PhoneDao
import com.example.techhourse.database.dao.UserBehaviorDao
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.database.entity.UserBehaviorEntity

/**
 * 应用程序数据库
 */
@Database(
    entities = [PhoneEntity::class, UserBehaviorEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun phoneDao(): PhoneDao
    abstract fun userBehaviorDao(): UserBehaviorDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horse_racing_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}