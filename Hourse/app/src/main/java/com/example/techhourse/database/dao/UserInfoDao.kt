package com.example.techhourse.database.dao

import androidx.room.*
import com.example.techhourse.database.entity.UserInfoEntity

/**
 * 用户信息表数据访问对象
 */
@Dao
interface UserInfoDao {
    
    @Query("SELECT * FROM user_info")
    suspend fun getAllUsers(): List<UserInfoEntity>
    
    @Query("SELECT * FROM user_info WHERE id = :id")
    suspend fun getUserById(id: Int): UserInfoEntity?
    
    @Query("SELECT * FROM user_info WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserInfoEntity?
    
    @Query("SELECT * FROM user_info WHERE phoneNumber = :phoneNumber AND password = :password")
    suspend fun getUserByPhoneAndPassword(phoneNumber: String, password: String): UserInfoEntity?
    
    @Query("SELECT * FROM user_info WHERE phoneNumber = :phoneNumber AND securityQuestion = :question AND securityAnswer = :answer")
    suspend fun getUserBySecurityInfo(phoneNumber: String, question: String, answer: String): UserInfoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserInfoEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserInfoEntity>): List<Long>
    
    @Update
    suspend fun updateUser(user: UserInfoEntity)
    
    @Query("UPDATE user_info SET password = :newPassword, updateTime = :updateTime WHERE phoneNumber = :phoneNumber")
    suspend fun updatePassword(phoneNumber: String, newPassword: String, updateTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_info SET isCurrentUser = 0")
    suspend fun clearAllCurrentUser()
    
    @Query("UPDATE user_info SET isCurrentUser = 1 WHERE id = :userId")
    suspend fun setCurrentUser(userId: Int)
    
    @Query("SELECT * FROM user_info WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserInfoEntity?
    
    @Query("UPDATE user_info SET securityQuestion = :question, securityAnswer = :answer, updateTime = :updateTime WHERE phoneNumber = :phoneNumber")
    suspend fun updateSecurityInfo(phoneNumber: String, question: String, answer: String, updateTime: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteUser(user: UserInfoEntity)
    
    @Query("DELETE FROM user_info WHERE phoneNumber = :phoneNumber")
    suspend fun deleteUserByPhoneNumber(phoneNumber: String)
    
    @Query("DELETE FROM user_info")
    suspend fun deleteAllUsers()
    
    @Query("SELECT COUNT(*) FROM user_info")
    suspend fun getUserCount(): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM user_info WHERE phoneNumber = :phoneNumber)")
    suspend fun isPhoneNumberExists(phoneNumber: String): Boolean
}