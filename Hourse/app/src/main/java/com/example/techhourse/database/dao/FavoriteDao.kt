package com.example.techhourse.database.dao

import androidx.room.*
import com.example.techhourse.database.entity.FavoriteEntity

/**
 * 收藏表数据访问对象
 */
@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM user_favorites WHERE userId = :userId ORDER BY createTime DESC")
    suspend fun getFavoritesByUserId(userId: Int): List<FavoriteEntity>
    
    @Query("SELECT * FROM user_favorites WHERE userId = :userId AND phoneId = :phoneId LIMIT 1")
    suspend fun getFavoriteByUserAndPhone(userId: Int, phoneId: Int): FavoriteEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND phoneId = :phoneId)")
    suspend fun isFavorite(userId: Int, phoneId: Int): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM user_favorites WHERE userId = :userId AND phoneId = :phoneId")
    suspend fun deleteFavoriteByUserAndPhone(userId: Int, phoneId: Int)
    
    @Query("DELETE FROM user_favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesByUserId(userId: Int)
    
    @Query("SELECT COUNT(*) FROM user_favorites WHERE userId = :userId")
    suspend fun getFavoriteCountByUserId(userId: Int): Int
    
    @Query("DELETE FROM user_favorites")
    suspend fun deleteAllFavorites()
}