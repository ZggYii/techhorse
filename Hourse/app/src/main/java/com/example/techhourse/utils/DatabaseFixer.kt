package com.example.techhourse.utils

import android.content.Context
import com.example.techhourse.R
import com.example.techhourse.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 数据库修复工具类
 * 用于修复imageResourceId的错误赋值问题
 */
class DatabaseFixer {
    
    companion object {
        private const val WRONG_RESOURCE_ID = 2131623945 // icon_weibo的资源ID
        private const val CORRECT_RESOURCE_ID = 2131623982 // infinix的正确资源ID
        
        /**
         * 修复数据库中错误的imageResourceId值
         * @param context 上下文
         * @return 修复的记录数量
         */
        suspend fun fixImageResourceIds(context: Context): Int {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val phoneDao = database.phoneDao()
                    
                    // 查找所有imageResourceId为错误值的记录
                    val wrongPhones = phoneDao.getAllPhones().filter { 
                        it.imageResourceId == WRONG_RESOURCE_ID 
                    }
                    
                    // 更新这些记录的imageResourceId
                    var fixedCount = 0
                    wrongPhones.forEach { phone ->
                        // 根据品牌确定正确的资源ID
                        val correctResourceId = when {
                            phone.brandName.contains("Infinix", ignoreCase = true) -> R.mipmap.infinix
                            else -> CORRECT_RESOURCE_ID // 默认使用infinix的ID
                        }
                        
                        // 更新记录
                        val updatedPhone = phone.copy(imageResourceId = correctResourceId)
                        phoneDao.updatePhone(updatedPhone)
                        fixedCount++
                    }
                    
                    fixedCount
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
        }
        
        /**
         * 检查是否存在imageResourceId错误的记录
         * @param context 上下文
         * @return 错误记录的数量
         */
        suspend fun checkWrongImageResourceIds(context: Context): Int {
            return withContext(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val phoneDao = database.phoneDao()
                    
                    phoneDao.getAllPhones().count { 
                        it.imageResourceId == WRONG_RESOURCE_ID 
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
        }
    }
}