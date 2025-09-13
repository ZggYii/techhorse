package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
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

    
    // 新增的TabLayout和ViewPager2组件
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: PhoneDetailPagerAdapter
    
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

        
        // 初始化TabLayout和ViewPager2
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
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
        

        
        // 设置ViewPager2适配器
        // 解析RAM和ROM
        val memoryParts = memoryConfigStr.split("+")
        val ram = if (memoryParts.isNotEmpty()) memoryParts[0].trim() else memoryConfigStr
        val rom = if (memoryParts.size > 1) memoryParts[1].trim() else ""
        
        val phoneData = PhoneData(
            id = phoneId,
            name = phoneModel,
            price = priceStr,
            ram = ram,
            rom = rom,
            screenSize = screenSizeStr,
            screenResolution = resolutionStr,
            frontCamera = frontCameraStr,
            rearCamera = rearCameraStr,
            sellingPoint = sellingPointStr,
            imageResourceId = imageResourceId
        )
        
        // 获取对比手机数据（如果有的话）
        val comparePhoneData = intent.getSerializableExtra("compare_phone_data") as? PhoneData
        
        pagerAdapter = PhoneDetailPagerAdapter(this, phoneData, comparePhoneData)
        viewPager.adapter = pagerAdapter
        
        // 设置TabLayout和ViewPager2的联动
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "基本参数"
                1 -> "综合对比"
                else -> ""
            }
        }.attach()
        
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