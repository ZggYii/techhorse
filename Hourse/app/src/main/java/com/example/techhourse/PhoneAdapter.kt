package com.example.techhourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.techhourse.database.entity.PhoneEntity

/**
 * 手机列表适配器
 */
class PhoneAdapter(private var phoneList: List<PhoneEntity>) : RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder>() {
    
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
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phone_card, parent, false)
        return PhoneViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
        val phone = phoneList[position]
        
        // 设置随机图片
        val randomImageIndex = (phone.id % phoneImages.size)
        holder.phoneImage.setImageResource(phoneImages[randomImageIndex])
        
        // 设置手机名称（品牌名 + 型号）
        val phoneName = if (phone.brandName.isNotEmpty()) {
            "${phone.brandName} ${phone.phoneModel}"
        } else {
            phone.phoneModel
        }
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
    }
    
    override fun getItemCount(): Int = phoneList.size
    
    /**
     * 更新手机列表数据
     */
    fun updatePhones(newPhoneList: List<PhoneEntity>) {
        phoneList = newPhoneList
        notifyDataSetChanged()
    }
}