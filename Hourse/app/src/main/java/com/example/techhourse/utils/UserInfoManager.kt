package com.example.techhourse.utils

import android.content.Context
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.entity.UserInfoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * 用户信息管理工具类
 * 提供用户注册、登录、密码管理等功能
 */
class UserInfoManager {
    
    companion object {
        
        /**
         * 用户注册
         * @param context 上下文
         * @param phoneNumber 手机号
         * @param password 密码
         * @param securityQuestion 密保问题
         * @param securityAnswer 密保答案
         * @return 注册结果：成功返回用户ID，失败返回-1
         */
        suspend fun registerUser(
            context: Context,
            phoneNumber: String,
            password: String,
            securityQuestion: String,
            securityAnswer: String
        ): Long {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    
                    // 检查手机号是否已存在
                    if (userInfoDao.isPhoneNumberExists(phoneNumber)) {
                        return@withContext -1L // 手机号已存在
                    }
                    
                    // 创建新用户
                    val newUser = UserInfoEntity(
                        phoneNumber = phoneNumber,
                        password = encryptPassword(password), // 加密密码
                        securityQuestion = securityQuestion,
                        securityAnswer = encryptPassword(securityAnswer) // 加密密保答案
                    )
                    
                    // 插入用户信息
                    userInfoDao.insertUser(newUser)
                } catch (e: Exception) {
                    e.printStackTrace()
                    -1L
                }
            }
        }
        
        /**
         * 用户登录
         * @param context 上下文
         * @param phoneNumber 手机号
         * @param password 密码
         * @return 登录成功返回用户信息，失败返回null
         */
        suspend fun loginUser(
            context: Context,
            phoneNumber: String,
            password: String
        ): UserInfoEntity? {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    
                    val encryptedPassword = encryptPassword(password)
                    userInfoDao.getUserByPhoneAndPassword(phoneNumber, encryptedPassword)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        
        /**
         * 通过密保问题重置密码
         * @param context 上下文
         * @param phoneNumber 手机号
         * @param securityQuestion 密保问题
         * @param securityAnswer 密保答案
         * @param newPassword 新密码
         * @return 重置成功返回true，失败返回false
         */
        suspend fun resetPasswordBySecurity(
            context: Context,
            phoneNumber: String,
            securityQuestion: String,
            securityAnswer: String,
            newPassword: String
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    
                    val encryptedAnswer = encryptPassword(securityAnswer)
                    val user = userInfoDao.getUserBySecurityInfo(phoneNumber, securityQuestion, encryptedAnswer)
                    
                    if (user != null) {
                        val encryptedNewPassword = encryptPassword(newPassword)
                        userInfoDao.updatePassword(phoneNumber, encryptedNewPassword)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
        
        /**
         * 修改密码
         * @param context 上下文
         * @param phoneNumber 手机号
         * @param oldPassword 旧密码
         * @param newPassword 新密码
         * @return 修改成功返回true，失败返回false
         */
        suspend fun changePassword(
            context: Context,
            phoneNumber: String,
            oldPassword: String,
            newPassword: String
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    
                    // 验证旧密码
                    val encryptedOldPassword = encryptPassword(oldPassword)
                    val user = userInfoDao.getUserByPhoneAndPassword(phoneNumber, encryptedOldPassword)
                    
                    if (user != null) {
                        val encryptedNewPassword = encryptPassword(newPassword)
                        userInfoDao.updatePassword(phoneNumber, encryptedNewPassword)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
        
        /**
         * 更新密保信息
         * @param context 上下文
         * @param phoneNumber 手机号
         * @param password 当前密码（用于验证）
         * @param newSecurityQuestion 新密保问题
         * @param newSecurityAnswer 新密保答案
         * @return 更新成功返回true，失败返回false
         */
        suspend fun updateSecurityInfo(
            context: Context,
            phoneNumber: String,
            password: String,
            newSecurityQuestion: String,
            newSecurityAnswer: String
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    
                    // 验证密码
                    val encryptedPassword = encryptPassword(password)
                    val user = userInfoDao.getUserByPhoneAndPassword(phoneNumber, encryptedPassword)
                    
                    if (user != null) {
                        val encryptedAnswer = encryptPassword(newSecurityAnswer)
                        userInfoDao.updateSecurityInfo(phoneNumber, newSecurityQuestion, encryptedAnswer)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
        
        /**
         * 获取用户信息
         * @param context 上下文
         * @param phoneNumber 手机号
         * @return 用户信息，不存在返回null
         */
        suspend fun getUserInfo(
            context: Context,
            phoneNumber: String
        ): UserInfoEntity? {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    userInfoDao.getUserByPhoneNumber(phoneNumber)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        
        /**
         * 检查手机号是否已注册
         * @param context 上下文
         * @param phoneNumber 手机号
         * @return 已注册返回true，未注册返回false
         */
        suspend fun isPhoneNumberRegistered(
            context: Context,
            phoneNumber: String
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    userInfoDao.isPhoneNumberExists(phoneNumber)
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
        
        /**
         * 密码加密（使用SHA-256）
         * 实际项目中建议使用更安全的加密方式，如bcrypt
         */
        private fun encryptPassword(password: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(password.toByteArray())
                hashBytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                password // 加密失败时返回原密码（不推荐）
            }
        }
        
        /**
         * 验证手机号格式
         */
        fun isValidPhoneNumber(phoneNumber: String): Boolean {
            val phoneRegex = "^1[3-9]\\d{9}$".toRegex()
            return phoneNumber.matches(phoneRegex)
        }
        
        /**
         * 验证密码强度
         * @param password 密码
         * @return 密码强度描述
         */
        fun validatePasswordStrength(password: String): String {
            return when {
                password.length < 6 -> "密码长度至少6位"
                password.length < 8 -> "密码强度：弱"
                password.matches(".*[a-zA-Z].*".toRegex()) && 
                password.matches(".*\\d.*".toRegex()) -> "密码强度：强"
                else -> "密码强度：中等"
            }
        }
    }
}