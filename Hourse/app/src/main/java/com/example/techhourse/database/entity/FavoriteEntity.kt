package com.example.techhourse.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 收藏表实体类
 */
@Entity(tableName = "user_favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val userId: Int,                    // 用户ID，关联UserInfoEntity的id
    val phoneId: Int,                   // 手机ID，关联PhoneEntity的id
    val createTime: Long = System.currentTimeMillis()  // 收藏时间戳
)