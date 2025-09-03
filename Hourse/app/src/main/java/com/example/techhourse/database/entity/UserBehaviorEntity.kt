package com.example.techhourse.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户行为表实体类
 */
@Entity(tableName = "user_behavior")
data class UserBehaviorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val screenUsageTime: String,           // 屏幕使用时长（区分游戏/社交/视频类App）
    val batteryCapacity: String,           // 电池容量
    val memoryUsage: String,               // 使用内存 / 总内存
    val phoneUsagePeriod: String,          // 手机使用时段
    val galleryStorageRatio: String,       // 图库存储使用占比
    val dailyGameTime: String,             // 日均游戏时间
    val nightPhotography: String,          // 夜间拍照
    val recordTime: Long = System.currentTimeMillis()  // 记录时间戳
)