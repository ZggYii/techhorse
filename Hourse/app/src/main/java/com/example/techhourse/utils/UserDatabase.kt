package com.example.techhourse.utils

import android.content.Context
import android.content.SharedPreferences

data class User(
    val id: String,
    val phoneNumber: String,
    val password: String,
    val securityQuestion: String,
    val securityAnswer: String
)

class UserDatabase private constructor(context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null
        
        fun getInstance(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("user_database", Context.MODE_PRIVATE)
    
    private var currentUserId: String? = null
    
    /**
     * 注册新用户
     * @return true if registration successful, false if phone number already exists
     */
    fun registerUser(phoneNumber: String, password: String, securityQuestion: String, securityAnswer: String): Boolean {
        // 检查手机号是否已存在
        if (isPhoneNumberExists(phoneNumber)) {
            return false
        }
        
        // 生成用户ID
        val userId = "user_${System.currentTimeMillis()}"
        
        // 保存用户信息
        val editor = sharedPreferences.edit()
        editor.putString("${userId}_phone", phoneNumber)
        editor.putString("${userId}_password", password)
        editor.putString("${userId}_security_question", securityQuestion)
        editor.putString("${userId}_security_answer", securityAnswer)
        
        // 保存手机号到用户ID的映射
        editor.putString("phone_${phoneNumber}", userId)
        
        editor.apply()
        return true
    }
    
    /**
     * 用户登录验证
     * @return user ID if login successful, null if failed
     */
    fun loginUser(phoneNumber: String, password: String): String? {
        val userId = getUserIdByPhone(phoneNumber) ?: return null
        val storedPassword = sharedPreferences.getString("${userId}_password", null)
        
        return if (storedPassword == password) {
            currentUserId = userId
            userId
        } else {
            null
        }
    }
    
    /**
     * 验证密保问题答案
     * @return true if answer is correct, false otherwise
     */
    fun verifySecurityAnswer(phoneNumber: String, answer: String): Boolean {
        val userId = getUserIdByPhone(phoneNumber) ?: return false
        val storedAnswer = sharedPreferences.getString("${userId}_security_answer", null)
        return storedAnswer == answer
    }
    
    /**
     * 获取用户的密保问题
     */
    fun getSecurityQuestion(phoneNumber: String): String? {
        val userId = getUserIdByPhone(phoneNumber) ?: return null
        return sharedPreferences.getString("${userId}_security_question", null)
    }
    
    /**
     * 更新用户密码
     */
    fun updatePassword(phoneNumber: String, newPassword: String): Boolean {
        val userId = getUserIdByPhone(phoneNumber) ?: return false
        val editor = sharedPreferences.edit()
        editor.putString("${userId}_password", newPassword)
        editor.apply()
        return true
    }
    
    /**
     * 设置当前用户ID
     */
    fun setCurrentUserId(userId: String) {
        sharedPreferences.edit().putString("current_user_id", userId).apply()
    }
    
    /**
     * 根据用户ID获取用户信息
     */
    fun getUserById(userId: String): User? {
        val phoneNumber = sharedPreferences.getString("${userId}_phone", null) ?: return null
        val password = sharedPreferences.getString("${userId}_password", null) ?: return null
        val securityQuestion = sharedPreferences.getString("${userId}_security_question", null) ?: return null
        val securityAnswer = sharedPreferences.getString("${userId}_security_answer", null) ?: return null
        
        return User(userId, phoneNumber, password, securityQuestion, securityAnswer)
    }
    
    /**
     * 清除当前用户
     */
    fun clearCurrentUser() {
        sharedPreferences.edit().remove("current_user_id").apply()
    }
    
    /**
     * 检查手机号是否已存在
     */
    private fun isPhoneNumberExists(phoneNumber: String): Boolean {
        return sharedPreferences.contains("phone_${phoneNumber}")
    }
    
    /**
     * 根据手机号获取用户ID
     */
    private fun getUserIdByPhone(phoneNumber: String): String? {
        return sharedPreferences.getString("phone_${phoneNumber}", null)
    }
    
    /**
     * 获取当前登录用户ID
     */
    fun getCurrentUserId(): String? {
        return sharedPreferences.getString("current_user_id", null)
    }
    
    /**
     * 用户登出
     */
    fun logout() {
        currentUserId = null
        clearCurrentUser()
    }
}