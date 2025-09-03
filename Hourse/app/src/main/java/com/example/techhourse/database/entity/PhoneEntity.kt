package com.example.techhourse.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 手机库表实体类
 */
@Entity(tableName = "phone_library")
data class PhoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val phoneModel: String,        // 手机型号
    val brandName: String,         // 品牌名
    val marketName: String,        // 市场名
    val memoryConfig: String,      // 内存配置
    val frontCamera: String,       // 前摄
    val rearCamera: String,        // 后摄
    val resolution: String,        // 分辨率
    val screenSize: String,        // 屏幕尺寸
    val sellingPoint: String,      // 卖点
    val price: String              // 价格
)