package com.example.techhourse.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.techhourse.database.dao.FavoriteDao
import com.example.techhourse.database.dao.PhoneDao
import com.example.techhourse.database.dao.UserBehaviorDao
import com.example.techhourse.database.dao.UserHistoryDao
import com.example.techhourse.database.dao.UserInfoDao
import com.example.techhourse.database.entity.FavoriteEntity
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.database.entity.UserBehaviorEntity
import com.example.techhourse.database.entity.UserHistoryEntity
import com.example.techhourse.database.entity.UserInfoEntity

/**
 * 应用程序数据库
 */
@Database(
    entities = [PhoneEntity::class, UserBehaviorEntity::class, UserInfoEntity::class, FavoriteEntity::class, UserHistoryEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun phoneDao(): PhoneDao
    abstract fun userBehaviorDao(): UserBehaviorDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userHistoryDao(): UserHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // 数据库迁移：从版本1到版本2，添加imageResourceId字段
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE phone_library ADD COLUMN imageResourceId INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // 数据库迁移：从版本2到版本3，添加用户信息表
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_info (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        phoneNumber TEXT NOT NULL,
                        password TEXT NOT NULL,
                        securityQuestion TEXT NOT NULL,
                        securityAnswer TEXT NOT NULL,
                        createTime INTEGER NOT NULL,
                        updateTime INTEGER NOT NULL
                    )
                """)
            }
        }
        
        // 数据库迁移：从版本3到版本4，添加isCurrentUser字段
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_info ADD COLUMN isCurrentUser INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // 数据库迁移：从版本4到版本5，添加收藏表
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_favorites (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        phoneId INTEGER NOT NULL,
                        createTime INTEGER NOT NULL
                    )
                """)
            }
        }
        
        // 数据库迁移：从版本5到版本6，添加用户历史记录表
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        phoneId INTEGER NOT NULL,
                        viewTime INTEGER NOT NULL,
                        phoneModel TEXT NOT NULL,
                        phoneBrand TEXT NOT NULL,
                        FOREIGN KEY(userId) REFERENCES user_info(id) ON DELETE CASCADE,
                        FOREIGN KEY(phoneId) REFERENCES phone_library(id) ON DELETE CASCADE
                    )
                """)
                // 创建索引
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_history_userId ON user_history(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_history_phoneId ON user_history(phoneId)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_user_history_userId_phoneId ON user_history(userId, phoneId)")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "horse_racing_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
                INSTANCE = instance
                instance
            }
        }
    }
}