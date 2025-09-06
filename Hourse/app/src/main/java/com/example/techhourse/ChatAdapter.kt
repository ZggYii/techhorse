package com.example.techhourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 聊天消息适配器
 */
class ChatAdapter(private val messages: MutableList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 1    // 用户消息类型
        private const val VIEW_TYPE_AI = 2      // AI消息类型
    }

    /**
     * 根据消息类型返回不同的视图类型
     */
    override fun getItemViewType(position: Int): Int {
        return when (messages[position].senderType) {
            SenderType.USER -> VIEW_TYPE_USER
            SenderType.AI -> VIEW_TYPE_AI
        }
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_my_message, parent, false)
                UserMessageViewHolder(view)
            }
            VIEW_TYPE_AI -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_other_message, parent, false)
                AiMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    /**
     * 绑定数据到ViewHolder
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is AiMessageViewHolder -> holder.bind(message)
        }
    }

    /**
     * 获取消息数量
     */
    override fun getItemCount(): Int = messages.size

    /**
     * 添加新消息
     */
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    /**
     * 清空所有消息
     */
    fun clearMessages() {
        messages.clear()
        notifyDataSetChanged()
    }
    
    /**
     * 更新指定位置消息的状态
     */
    fun updateMessageStatus(position: Int, status: MessageStatus) {
        if (position >= 0 && position < messages.size) {
            messages[position].status = status
            notifyItemChanged(position)
        }
    }
    
    /**
     * 更新最后一条消息的内容和状态
     */
    fun updateLastMessage(content: String, status: MessageStatus = MessageStatus.NORMAL) {
        if (messages.isNotEmpty()) {
            val lastIndex = messages.size - 1
            val lastMessage = messages[lastIndex]
            messages[lastIndex] = Message(
                content = content,
                senderType = lastMessage.senderType,
                timestamp = lastMessage.timestamp,
                status = status
            )
            notifyItemChanged(lastIndex)
        }
    }

    /**
     * 用户消息ViewHolder
     */
    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tv_message_content)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_message_time)

        fun bind(message: Message) {
            when (message.status) {
                MessageStatus.LOADING -> {
                    tvContent.text = "加载中..."
                    tvTime.text = message.getFormattedTime()
                }
                MessageStatus.TIMEOUT -> {
                    tvContent.text = "响应超时"
                    tvTime.text = message.getFormattedTime()
                }
                MessageStatus.NORMAL -> {
                    tvContent.text = message.content
                    tvTime.text = message.getFormattedTime()
                }
            }
        }
    }

    /**
     * AI消息ViewHolder
     */
    class AiMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tv_message_content)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_message_time)

        fun bind(message: Message) {
            tvContent.text = message.content
            tvTime.text = message.getFormattedTime()
        }
    }
}