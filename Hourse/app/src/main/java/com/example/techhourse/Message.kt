package com.example.techhourse

import java.text.SimpleDateFormat
import java.util.*

/**
 * 聊天消息数据模型
 */
data class Message(
    val content: String,           // 消息内容
    val senderType: SenderType,    // 发送者类型
    val timestamp: Long = System.currentTimeMillis(),  // 时间戳
    var status: MessageStatus = MessageStatus.NORMAL   // 消息状态
) {
    /**
     * 获取格式化的时间字符串
     * @return 格式化后的时间
     */
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * 检查是否为加载状态
     */
    fun isLoading(): Boolean = status == MessageStatus.LOADING

    /**
     * 检查是否为超时状态
     */
    fun isTimeout(): Boolean = status == MessageStatus.TIMEOUT

    /**
     * 设置为加载状态
     */
    fun setLoading() {
        status = MessageStatus.LOADING
    }

    /**
     * 设置为超时状态
     */
    fun setTimeout() {
        status = MessageStatus.TIMEOUT
    }

    /**
     * 设置为正常状态
     */
    fun setNormal() {
        status = MessageStatus.NORMAL
    }
}
/**
 * 发送者类型枚举
 */
enum class SenderType {
    USER,  // 用户消息
    AI     // AI回复
}

/**
 * 消息状态枚举
 */
enum class MessageStatus {
    NORMAL,    // 正常状态
    LOADING,   // 加载中
    TIMEOUT    // 超时
}