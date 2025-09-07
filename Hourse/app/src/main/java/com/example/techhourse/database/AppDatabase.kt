package com.example.techhourse.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.techhourse.database.dao.PhoneDao
import com.example.techhourse.database.dao.UserBehaviorDao
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.database.entity.UserBehaviorEntity

/**
 * 应用程序数据库
 */
@Database(
    entities = [PhoneEntity::class, UserBehaviorEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun phoneDao(): PhoneDao
    abstract fun userBehaviorDao(): UserBehaviorDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // 数据库迁移：从版本1到版本2，添加imageResourceId字段
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE phone_library ADD COLUMN imageResourceId INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horse_racing_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}