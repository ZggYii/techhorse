package com.example.techhourse.utils

import android.content.Context
import com.example.techhourse.database.DatabaseInitializer
import com.example.techhourse.database.dao.PhoneDao
import com.example.techhourse.database.dao.UserBehaviorDao
import com.example.techhourse.database.entity.UserBehaviorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 测试数据生成器
 * 用于生成示例用户行为数据，便于测试系统提示词功能
 */
class TestDataGenerator {
    
    companion object {
        /**
         * 插入示例用户行为数据
         * @param context 上下文
         * @param userBehaviorDao 用户行为数据访问对象
         * @param phoneDao 手机数据访问对象
         */
        suspend fun insertSampleUserBehavior(context: Context, userBehaviorDao: UserBehaviorDao, phoneDao: PhoneDao) {
            withContext(Dispatchers.IO) {
                try {
                    // 首先确保手机库数据已初始化
                    DatabaseInitializer.initializePhoneData(context, phoneDao)
                    
                    // 检查是否已有用户行为数据
                    val existingCount = userBehaviorDao.getUserBehaviorCount()
                    if (existingCount > 0) {
                        println("用户行为数据已存在，跳过插入示例数据")
                        return@withContext
                    }
                    
                    // 创建示例用户行为数据
                    val sampleBehavior = UserBehaviorEntity(
                        screenUsageTime = "游戏类App: 4.5小时, 社交类App: 2.3小时, 视频类App: 1.8小时",
                        batteryCapacity = "4500mAh (当前电量: 65%)",
                        memoryUsage = "6.2GB / 8GB",
                        phoneUsagePeriod = "主要使用时段: 晚上19:00-23:00, 周末全天",
                        galleryStorageRatio = "图库占用: 45.6% (主要为游戏截图和社交照片)",
                        dailyGameTime = "日均游戏时间: 4.2小时 (主要为王者荣耀、和平精英)",
                        nightPhotography = "夜间拍照: 经常 (每周3-4次夜景拍摄)",
                        recordTime = System.currentTimeMillis()
                    )
                    
                    // 插入数据
                    val insertedId = userBehaviorDao.insertUserBehavior(sampleBehavior)
                    println("成功插入示例用户行为数据，ID: $insertedId")
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("插入示例用户行为数据失败: ${e.message}")
                }
            }
        }
        
        /**
         * 插入多个不同类型的示例数据
         * @param context 上下文
         * @param userBehaviorDao 用户行为数据访问对象
         * @param phoneDao 手机数据访问对象
         */
        suspend fun insertMultipleSampleData(context: Context, userBehaviorDao: UserBehaviorDao, phoneDao: PhoneDao) {
            withContext(Dispatchers.IO) {
                try {
                    // 首先确保手机库数据已初始化
                    DatabaseInitializer.initializePhoneData(context, phoneDao)
                    
                    // 清空现有数据
                    userBehaviorDao.deleteAllUserBehaviors()
                    
                    // 示例1: 重度游戏用户
                    val gamerBehavior = UserBehaviorEntity(
                        screenUsageTime = "游戏类App: 6.8小时, 社交类App: 1.2小时, 视频类App: 0.8小时",
                        batteryCapacity = "5000mAh (当前电量: 45%)",
                        memoryUsage = "10.5GB / 12GB",
                        phoneUsagePeriod = "主要使用时段: 晚上20:00-02:00",
                        galleryStorageRatio = "图库占用: 78.3% (大量游戏截图和录屏)",
                        dailyGameTime = "日均游戏时间: 6.5小时 (原神、王者荣耀、和平精英)",
                        nightPhotography = "夜间拍照: 很少",
                        recordTime = System.currentTimeMillis() - 86400000 // 1天前
                    )
                    
                    // 示例2: 摄影爱好者
                    val photographerBehavior = UserBehaviorEntity(
                        screenUsageTime = "社交类App: 3.5小时, 视频类App: 2.8小时, 游戏类App: 0.5小时",
                        batteryCapacity = "4200mAh (当前电量: 85%)",
                        memoryUsage = "4.8GB / 8GB",
                        phoneUsagePeriod = "主要使用时段: 白天09:00-18:00, 周末外出拍摄",
                        galleryStorageRatio = "图库占用: 92.1% (大量高清照片和视频)",
                        dailyGameTime = "日均游戏时间: 0.3小时 (偶尔休闲游戏)",
                        nightPhotography = "夜间拍照: 经常 (每周5-6次，专业夜景拍摄)",
                        recordTime = System.currentTimeMillis() - 43200000 // 12小时前
                    )
                    
                    // 示例3: 商务用户
                    val businessBehavior = UserBehaviorEntity(
                        screenUsageTime = "办公类App: 4.2小时, 社交类App: 2.1小时, 视频类App: 1.0小时",
                        batteryCapacity = "3800mAh (当前电量: 92%)",
                        memoryUsage = "3.2GB / 6GB",
                        phoneUsagePeriod = "主要使用时段: 工作日09:00-18:00",
                        galleryStorageRatio = "图库占用: 25.4% (主要为工作文档截图)",
                        dailyGameTime = "日均游戏时间: 0.1小时 (基本不玩游戏)",
                        nightPhotography = "夜间拍照: 偶尔 (商务聚餐时拍照)",
                        recordTime = System.currentTimeMillis()
                    )
                    
                    // 插入所有示例数据
                    val behaviors = listOf(gamerBehavior, photographerBehavior, businessBehavior)
                    val insertedIds = userBehaviorDao.insertUserBehaviors(behaviors)
                    println("成功插入 ${insertedIds.size} 条示例用户行为数据")
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("插入多个示例数据失败: ${e.message}")
                }
            }
        }
    }
}