package com.example.techhourse.utils

import android.content.Context
import android.util.Log
import com.example.techhourse.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 数据库调试辅助类
 * 用于查看和调试数据库中的用户数据
 */
class DatabaseDebugHelper {
    
    companion object {
        private const val TAG = "DatabaseDebug"
        
        /**
         * 打印数据库中所有用户信息
         * @param context 上下文
         */
        suspend fun printAllUsers(context: Context) {
            withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    
                    val allUsers = userInfoDao.getAllUsers()
                    val userCount = userInfoDao.getUserCount()
                    
                    Log.d(TAG, "=== 数据库用户信息调试 ===")
                    Log.d(TAG, "数据库名称: horse_racing_database")
                    Log.d(TAG, "用户总数: $userCount")
                    Log.d(TAG, "表名: user_info")
                    
                    if (allUsers.isEmpty()) {
                        Log.d(TAG, "❌ 数据库中没有用户数据！")
                        Log.d(TAG, "可能原因:")
                        Log.d(TAG, "1. 用户还没有成功注册")
                        Log.d(TAG, "2. 注册过程中出现了错误")
                        Log.d(TAG, "3. 数据库迁移问题")
                    } else {
                        Log.d(TAG, "✅ 找到 ${allUsers.size} 个用户:")
                        allUsers.forEachIndexed { index, user ->
                            Log.d(TAG, "--- 用户 ${index + 1} ---")
                            Log.d(TAG, "ID: ${user.id}")
                            Log.d(TAG, "手机号: ${user.phoneNumber}")
                            Log.d(TAG, "密码: ${if (user.password.isNotEmpty()) "已设置(${user.password.length}位)" else "未设置"}")
                            Log.d(TAG, "密保问题: ${user.securityQuestion}")
                            Log.d(TAG, "密保答案: ${if (user.securityAnswer.isNotEmpty()) "已设置" else "未设置"}")
                            Log.d(TAG, "是否当前用户: ${if (user.isCurrentUser == 1) "是" else "否"}")
                            Log.d(TAG, "创建时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(user.createTime))}")
                            Log.d(TAG, "更新时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(user.updateTime))}")
                        }
                    }
                    
                    Log.d(TAG, "=== 调试信息结束 ===")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "查询数据库时出错: ${e.message}", e)
                }
            }
        }
        
        /**
         * 检查特定手机号是否存在
         * @param context 上下文
         * @param phoneNumber 手机号
         */
        suspend fun checkPhoneNumber(context: Context, phoneNumber: String) {
            withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val userInfoDao = database.userInfoDao()
                    
                    val exists = userInfoDao.isPhoneNumberExists(phoneNumber)
                    val user = userInfoDao.getUserByPhoneNumber(phoneNumber)
                    
                    Log.d(TAG, "=== 手机号检查: $phoneNumber ===")
                    Log.d(TAG, "是否存在: $exists")
                    if (user != null) {
                        Log.d(TAG, "用户信息: ID=${user.id}, 密码长度=${user.password.length}")
                    } else {
                        Log.d(TAG, "未找到该手机号对应的用户")
                    }
                    Log.d(TAG, "=== 检查结束 ===")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "检查手机号时出错: ${e.message}", e)
                }
            }
        }
        
        /**
         * 获取数据库路径信息
         * @param context 上下文
         */
        fun getDatabasePath(context: Context): String {
            return context.getDatabasePath("horse_racing_database").absolutePath
        }
    }
}