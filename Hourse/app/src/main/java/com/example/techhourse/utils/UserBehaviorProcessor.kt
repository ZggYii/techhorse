package com.example.techhourse.utils

import com.example.techhourse.database.dao.UserBehaviorDao
import com.example.techhourse.database.entity.UserBehaviorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 用户行为数据处理工具类
 * 负责处理PhoneUsageInfoManager返回的数据并存储到数据库
 */
class UserBehaviorProcessor {
    
    companion object {
        /**
         * 处理并保存用户行为数据
         * @param usageInfoMap PhoneUsageInfoManager.getAllUsageInfo()返回的Map
         * @param userBehaviorDao 用户行为数据访问对象
         */
        suspend fun processAndSaveUserBehavior(
            usageInfoMap: Map<String, String>,
            userBehaviorDao: UserBehaviorDao
        ) {
            withContext(Dispatchers.IO) {
                try {
                    // 处理Map中的数据，过滤包含"系统无法"的值
                    val processedData = processUsageData(usageInfoMap)
                    
                    // 创建用户行为实体
                    val userBehavior = UserBehaviorEntity(
                        screenUsageTime = processedData["屏幕使用时长"] ?: "None",
                        batteryCapacity = processedData["电池容量"] ?: "None",
                        memoryUsage = processedData["使用内存"] ?: "None",
                        phoneUsagePeriod = processedData["手机使用时段"] ?: "None",
                        galleryStorageRatio = processedData["图库存储使用占比"] ?: "None",
                        dailyGameTime = processedData["日均游戏时间"] ?: "None",
                        nightPhotography = processedData["夜间拍照"] ?: "None"
                    )
                    
                    // 保存到数据库
                    userBehaviorDao.insertUserBehavior(userBehavior)
                    
                    println("用户行为数据已成功保存到数据库")
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("保存用户行为数据失败: ${e.message}")
                }
            }
        }
        
        /**
         * 处理使用数据，过滤包含"系统无法"的值
         * @param usageInfoMap 原始数据Map
         * @return 处理后的数据Map
         */
        private fun processUsageData(usageInfoMap: Map<String, String>): Map<String, String> {
            val processedMap = mutableMapOf<String, String>()
            
            usageInfoMap.forEach { (key, value) ->
                // 如果value包含"系统无法"，则设置为"None"
                val processedValue = if (value.contains("系统无法")) {
                    "None"
                } else {
                    value
                }
                processedMap[key] = processedValue
            }
            
            return processedMap
        }
        
        /**
         * 获取最新的用户行为数据
         * @param userBehaviorDao 用户行为数据访问对象
         * @return 最新的用户行为实体，如果没有数据则返回null
         */
        suspend fun getLatestUserBehavior(userBehaviorDao: UserBehaviorDao): UserBehaviorEntity? {
            return withContext(Dispatchers.IO) {
                try {
                    userBehaviorDao.getLatestUserBehavior()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        
        /**
         * 获取用户行为数据统计
         * @param userBehaviorDao 用户行为数据访问对象
         * @return 数据条数
         */
        suspend fun getUserBehaviorCount(userBehaviorDao: UserBehaviorDao): Int {
            return withContext(Dispatchers.IO) {
                try {
                    userBehaviorDao.getUserBehaviorCount()
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
            }
        }
    }
}