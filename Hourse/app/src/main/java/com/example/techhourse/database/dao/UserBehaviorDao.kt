package com.example.techhourse.database.dao

import androidx.room.*
import com.example.techhourse.database.entity.UserBehaviorEntity

/**
 * 用户行为表数据访问对象
 */
@Dao
interface UserBehaviorDao {
    
    @Query("SELECT * FROM user_behavior ORDER BY recordTime DESC")
    suspend fun getAllUserBehaviors(): List<UserBehaviorEntity>
    
    @Query("SELECT * FROM user_behavior WHERE id = :id")
    suspend fun getUserBehaviorById(id: Int): UserBehaviorEntity?
    
    @Query("SELECT * FROM user_behavior ORDER BY recordTime DESC LIMIT 1")
    suspend fun getLatestUserBehavior(): UserBehaviorEntity?
    
    @Query("SELECT * FROM user_behavior WHERE recordTime >= :startTime AND recordTime <= :endTime ORDER BY recordTime DESC")
    suspend fun getUserBehaviorsByTimeRange(startTime: Long, endTime: Long): List<UserBehaviorEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBehavior(userBehavior: UserBehaviorEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBehaviors(userBehaviors: List<UserBehaviorEntity>): List<Long>
    
    @Update
    suspend fun updateUserBehavior(userBehavior: UserBehaviorEntity)
    
    @Delete
    suspend fun deleteUserBehavior(userBehavior: UserBehaviorEntity)
    
    @Query("DELETE FROM user_behavior")
    suspend fun deleteAllUserBehaviors()
    
    @Query("SELECT COUNT(*) FROM user_behavior")
    suspend fun getUserBehaviorCount(): Int
}