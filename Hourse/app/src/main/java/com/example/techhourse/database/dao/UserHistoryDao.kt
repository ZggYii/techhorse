package com.example.techhourse.database.dao

import androidx.room.*
import com.example.techhourse.database.entity.UserHistoryEntity

@Dao
interface UserHistoryDao {
    
    /**
     * 根据用户ID获取该用户的所有历史记录，按查看时间倒序排列
     */
    @Query("SELECT * FROM user_history WHERE userId = :userId ORDER BY viewTime DESC")
    suspend fun getHistoryByUserId(userId: Long): List<UserHistoryEntity>
    
    /**
     * 根据用户ID获取该用户的历史记录，限制数量
     */
    @Query("SELECT * FROM user_history WHERE userId = :userId ORDER BY viewTime DESC LIMIT :limit")
    suspend fun getHistoryByUserIdWithLimit(userId: Long, limit: Int): List<UserHistoryEntity>
    
    /**
     * 检查用户是否已经查看过某个手机
     */
    @Query("SELECT COUNT(*) FROM user_history WHERE userId = :userId AND phoneId = :phoneId")
    suspend fun checkIfUserViewedPhone(userId: Long, phoneId: Long): Int
    
    /**
     * 插入或更新历史记录
     * 如果用户已经查看过该手机，则更新查看时间
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(history: UserHistoryEntity)
    
    /**
     * 删除用户的某条历史记录
     */
    @Query("DELETE FROM user_history WHERE userId = :userId AND phoneId = :phoneId")
    suspend fun deleteUserHistory(userId: Long, phoneId: Long)
    
    /**
     * 删除用户的所有历史记录
     */
    @Query("DELETE FROM user_history WHERE userId = :userId")
    suspend fun deleteAllUserHistory(userId: Long)
    
    /**
     * 获取用户历史记录总数
     */
    @Query("SELECT COUNT(*) FROM user_history WHERE userId = :userId")
    suspend fun getUserHistoryCount(userId: Long): Int
    
    /**
     * 删除用户最旧的历史记录（当历史记录过多时使用）
     */
    @Query("DELETE FROM user_history WHERE userId = :userId AND id IN (SELECT id FROM user_history WHERE userId = :userId ORDER BY viewTime ASC LIMIT :count)")
    suspend fun deleteOldestHistory(userId: Long, count: Int)
}