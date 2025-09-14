package com.example.techhourse.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "user_history",
    foreignKeys = [
        ForeignKey(
            entity = UserInfoEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PhoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["phoneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["phoneId"]),
        Index(value = ["userId", "phoneId"], unique = true)
    ]
)
data class UserHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: Long,  // 用户ID，关联UserInfoEntity
    
    val phoneId: Long, // 手机ID，关联PhoneEntity
    
    val viewTime: Long = System.currentTimeMillis(), // 查看时间戳
    
    val phoneModel: String, // 手机型号（冗余字段，便于快速查询）
    
    val phoneBrand: String  // 手机品牌（冗余字段，便于快速查询）
)