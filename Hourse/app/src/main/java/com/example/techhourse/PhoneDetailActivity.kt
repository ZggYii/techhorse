package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.PhoneCard

/**
 * 手机详情页面Activity
 */
class PhoneDetailActivity : AppCompatActivity() {
    
    // UI组件
    private lateinit var phoneDetailImage: ImageView
    private lateinit var phoneDetailName: TextView
    private lateinit var phoneDetailPrice: TextView
    private lateinit var memoryConfig: TextView
    private lateinit var screenSize: TextView
    private lateinit var resolution: TextView
    private lateinit var frontCamera: TextView
    private lateinit var rearCamera: TextView
    private lateinit var sellingPoint: TextView
    
    companion object {
        const val EXTRA_PHONE_ID = "phone_id"
        const val EXTRA_PHONE_MODEL = "phone_model"
        const val EXTRA_BRAND_NAME = "brand_name"
        const val EXTRA_MARKET_NAME = "market_name"
        const val EXTRA_MEMORY_CONFIG = "memory_config"
        const val EXTRA_FRONT_CAMERA = "front_camera"
        const val EXTRA_REAR_CAMERA = "rear_camera"
        const val EXTRA_RESOLUTION = "resolution"
        const val EXTRA_SCREEN_SIZE = "screen_size"
        const val EXTRA_SELLING_POINT = "selling_point"
        const val EXTRA_PRICE = "price"
        const val EXTRA_IMAGE_RESOURCE_ID = "image_resource_id"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_detail)
        
        // 初始化视图
        initViews()
        
        // 加载手机数据
        loadPhoneData()
    }
    
    private fun initViews() {
        phoneDetailImage = findViewById(R.id.iv_phone_detail_image)
        phoneDetailName = findViewById(R.id.tv_phone_detail_name)
        phoneDetailPrice = findViewById(R.id.tv_phone_detail_price)
        memoryConfig = findViewById(R.id.tv_memory_config)
        screenSize = findViewById(R.id.tv_screen_size)
        resolution = findViewById(R.id.tv_resolution)
        frontCamera = findViewById(R.id.tv_front_camera)
        rearCamera = findViewById(R.id.tv_rear_camera)
        sellingPoint = findViewById(R.id.tv_selling_point)
    }
    
    private fun loadPhoneData() {
        // 从Intent中获取传递的手机数据
        val phoneId = intent.getIntExtra(EXTRA_PHONE_ID, -1)
        val phoneModel = intent.getStringExtra(EXTRA_PHONE_MODEL) ?: ""
        val brandNameStr = intent.getStringExtra(EXTRA_BRAND_NAME) ?: ""
        val marketNameStr = intent.getStringExtra(EXTRA_MARKET_NAME) ?: ""
        val memoryConfigStr = intent.getStringExtra(EXTRA_MEMORY_CONFIG) ?: ""
        val frontCameraStr = intent.getStringExtra(EXTRA_FRONT_CAMERA) ?: ""
        val rearCameraStr = intent.getStringExtra(EXTRA_REAR_CAMERA) ?: ""
        val resolutionStr = intent.getStringExtra(EXTRA_RESOLUTION) ?: ""
        val screenSizeStr = intent.getStringExtra(EXTRA_SCREEN_SIZE) ?: ""
        val sellingPointStr = intent.getStringExtra(EXTRA_SELLING_POINT) ?: ""
        val priceStr = intent.getStringExtra(EXTRA_PRICE) ?: ""
        val imageResourceId = intent.getIntExtra(EXTRA_IMAGE_RESOURCE_ID, 0)
        
        // 设置手机图片
        if (imageResourceId != 0) {
            phoneDetailImage.setImageResource(imageResourceId)
        } else {
            phoneDetailImage.setImageResource(R.mipmap.icon_iphone) // 默认图片
        }
        
        // 设置手机名称
        phoneDetailName.text = if (phoneModel.isNotEmpty()) phoneModel else "未知型号"
        
        // 设置价格
        phoneDetailPrice.text = if (priceStr.isNotEmpty()) {
            if (priceStr.startsWith("¥") || priceStr.startsWith("$")) {
                priceStr
            } else {
                "¥$priceStr"
            }
        } else {
            "价格待定"
        }
        
        // 设置内存配置
        memoryConfig.text = if (memoryConfigStr.isNotEmpty()) memoryConfigStr else "配置信息暂无"
        
        // 设置屏幕尺寸
        screenSize.text = if (screenSizeStr.isNotEmpty()) screenSizeStr else "屏幕信息暂无"
        
        // 设置分辨率
        resolution.text = if (resolutionStr.isNotEmpty()) resolutionStr else "分辨率信息暂无"
        
        // 设置前摄
        frontCamera.text = if (frontCameraStr.isNotEmpty()) frontCameraStr else "前摄信息暂无"
        
        // 设置后摄
        rearCamera.text = if (rearCameraStr.isNotEmpty()) rearCameraStr else "后摄信息暂无"
        
        // 设置卖点
        sellingPoint.text = if (sellingPointStr.isNotEmpty()) sellingPointStr else "暂无卖点信息"
        
        // 发送广播到MainActivity添加到历史记录
        sendHistoryBroadcast(phoneId, phoneModel, priceStr, imageResourceId)
    }
    
    private fun sendHistoryBroadcast(phoneId: Int, phoneName: String, phonePrice: String, imageResourceId: Int) {
        val intent = Intent(MainActivity.ACTION_ADD_TO_HISTORY).apply {
            putExtra(MainActivity.EXTRA_PHONE_ID, phoneId)
            putExtra(MainActivity.EXTRA_PHONE_NAME, phoneName)
            putExtra(MainActivity.EXTRA_PHONE_PRICE, phonePrice)
            putExtra(MainActivity.EXTRA_PHONE_IMAGE_RESOURCE, imageResourceId)
        }

        sendBroadcast(intent)
    }
}