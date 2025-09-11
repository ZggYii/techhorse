package com.example.techhourse.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户信息表实体类
 */
@Entity(tableName = "user_info")
data class UserInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val phoneNumber: String,        // 手机号
    val password: String,           // 密码（实际应用中应该加密存储）
    val securityQuestion: String,   // 密保问题
    val securityAnswer: String,     // 密保答案
    val isCurrentUser: Int = 0,     // 是否为当前登录用户，1为是，0为不是
    val createTime: Long = System.currentTimeMillis(),  // 创建时间戳
    val updateTime: Long = System.currentTimeMillis()   // 更新时间戳
)