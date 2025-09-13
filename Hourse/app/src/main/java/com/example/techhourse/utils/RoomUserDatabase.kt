package com.example.techhourse.utils

import android.content.Context
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.entity.UserInfoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Room数据库用户管理类
 */
class RoomUserDatabase(context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: RoomUserDatabase? = null
        
        fun getInstance(context: Context): RoomUserDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RoomUserDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val userInfoDao = database.userInfoDao()
    
    /**
     * 注册新用户
     */
    suspend fun registerUser(
        phoneNumber: String, 
        password: String, 
        securityQuestion: String, 
        securityAnswer: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 检查手机号是否已存在
            val existingUser = userInfoDao.getUserByPhoneNumber(phoneNumber)
            if (existingUser != null) {
                return@withContext false
            }
            
            // 创建新用户
            val newUser = UserInfoEntity(
                phoneNumber = phoneNumber,
                password = password,
                securityQuestion = securityQuestion,
                securityAnswer = securityAnswer,
                isCurrentUser = 0
            )
            
            userInfoDao.insertUser(newUser)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 用户登录
     */
    suspend fun loginUser(phoneNumber: String, password: String): Int? = withContext(Dispatchers.IO) {
        try {
            val user = userInfoDao.getUserByPhoneAndPassword(phoneNumber, password)
            if (user != null) {
                // 清除所有用户的当前状态
                userInfoDao.clearAllCurrentUser()
                // 设置当前用户
                userInfoDao.setCurrentUser(user.id)
                user.id
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 验证密保答案
     */
    suspend fun verifySecurityAnswer(phoneNumber: String, answer: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = userInfoDao.getUserByPhoneNumber(phoneNumber)
            user?.securityAnswer == answer
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取密保问题
     */
    suspend fun getSecurityQuestion(phoneNumber: String): String? = withContext(Dispatchers.IO) {
        try {
            userInfoDao.getUserByPhoneNumber(phoneNumber)?.securityQuestion
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 更新密码
     */
    suspend fun updatePassword(phoneNumber: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            userInfoDao.updatePassword(phoneNumber, newPassword)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 验证用户密码
     */
    suspend fun verifyPassword(phoneNumber: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val user = userInfoDao.getUserByPhoneAndPassword(phoneNumber, password)
            user != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取当前登录用户
     */
    suspend fun getCurrentUser(): UserInfoEntity? = withContext(Dispatchers.IO) {
        try {
            userInfoDao.getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 根据ID获取用户
     */
    suspend fun getUserById(userId: Int): UserInfoEntity? = withContext(Dispatchers.IO) {
        try {
            userInfoDao.getUserById(userId)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 用户登出
     */
    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        try {
            userInfoDao.clearAllCurrentUser()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取当前用户ID
     */
    suspend fun getCurrentUserId(): Int = withContext(Dispatchers.IO) {
        try {
            getCurrentUser()?.id ?: -1
        } catch (e: Exception) {
            -1
        }
    }
}