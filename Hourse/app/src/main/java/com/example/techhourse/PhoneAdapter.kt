package com.example.techhourse

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.utils.RoomUserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 手机列表适配器
 */
class PhoneAdapter(private var phoneList: List<PhoneEntity>) : RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder>() {
    
    private var favoritePhoneIds: Set<Long> = emptySet()
    
    // 随机手机图片资源数组
    private val phoneImages = arrayOf(
        R.mipmap.icon_iphone,
        R.mipmap.icon_iphone,
        R.mipmap.icon_iphone,
        R.mipmap.icon_iphone
    )
    
    class PhoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val phoneImage: ImageView = itemView.findViewById(R.id.iv_phone_image)
        val phoneName: TextView = itemView.findViewById(R.id.tv_phone_name)
        val phonePrice: TextView = itemView.findViewById(R.id.tv_phone_price)
        val phoneDesc: TextView = itemView.findViewById(R.id.tv_phone_desc)
        val favoriteIndicator: ImageView = itemView.findViewById(R.id.iv_favorite_indicator)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phone_card, parent, false)
        return PhoneViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
        val phone = phoneList[position]
        
        // 使用数据库中的图片资源ID，如果为0则使用默认图片
        val imageResourceId = if (phone.imageResourceId != 0) {
            phone.imageResourceId
        } else {
            R.mipmap.icon_iphone // 默认图片
        }
        holder.phoneImage.setImageResource(imageResourceId)
        
        // 设置手机名称（品牌名 + 型号）
        val phoneName = phone.phoneModel
        holder.phoneName.text = phoneName
        
        // 设置价格
        holder.phonePrice.text = if (phone.price.isNotEmpty()) {
            if (phone.price.startsWith("¥") || phone.price.startsWith("$")) {
                phone.price
            } else {
                "¥${phone.price}"
            }
        } else {
            "价格待定"
        }
        
        // 设置内存配置
        holder.phoneDesc.text = if (phone.memoryConfig.isNotEmpty()) {
            phone.memoryConfig
        } else {
            "配置信息"
        }
        
        // 设置收藏状态显示
        if (favoritePhoneIds.contains(phone.id.toLong())) {
            holder.favoriteIndicator.visibility = View.VISIBLE
        } else {
            holder.favoriteIndicator.visibility = View.GONE
        }
        
        // 设置点击事件，跳转到手机详情页面
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent: Intent = Intent(context, PhoneDetailActivity::class.java).apply {
                putExtra(PhoneDetailActivity.EXTRA_PHONE_ID, phone.id.toInt())
                putExtra(PhoneDetailActivity.EXTRA_PHONE_MODEL, phone.phoneModel)
                putExtra(PhoneDetailActivity.EXTRA_BRAND_NAME, phone.brandName)
                putExtra(PhoneDetailActivity.EXTRA_PRICE, phone.price)
                putExtra(PhoneDetailActivity.EXTRA_MEMORY_CONFIG, phone.memoryConfig)
                putExtra(PhoneDetailActivity.EXTRA_MARKET_NAME, phone.marketName)
                putExtra(PhoneDetailActivity.EXTRA_IMAGE_RESOURCE_ID, phone.imageResourceId)
                // 将数据库中的其他字段映射到PhoneDetailActivity期望的字段
                putExtra(PhoneDetailActivity.EXTRA_FRONT_CAMERA, phone.frontCamera) // 使用camera字段作为前置摄像头
                putExtra(PhoneDetailActivity.EXTRA_REAR_CAMERA, phone.rearCamera) // 使用camera字段作为后置摄像头
                putExtra(PhoneDetailActivity.EXTRA_RESOLUTION, phone.resolution) // 使用display字段作为分辨率
                putExtra(PhoneDetailActivity.EXTRA_SCREEN_SIZE, phone.screenSize) // 使用display字段作为屏幕尺寸
                putExtra(PhoneDetailActivity.EXTRA_SELLING_POINT, phone.sellingPoint) // 使用description作为卖点
            }
            context.startActivity(intent)
        }
    }
    
    override fun getItemCount(): Int = phoneList.size
    
    /**
     * 更新手机列表数据
     */
    fun updatePhones(newPhoneList: List<PhoneEntity>) {
        phoneList = newPhoneList
        notifyDataSetChanged()
    }
    
    /**
     * 更新收藏状态
     */
    fun updateFavoriteStatus(favoriteIds: Set<Long>) {
        favoritePhoneIds = favoriteIds
        notifyDataSetChanged()
    }
}