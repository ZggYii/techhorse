package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.entity.FavoriteEntity
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.database.entity.UserHistoryEntity
import com.example.techhourse.PhoneCard
import com.example.techhourse.utils.RoomUserDatabase
import kotlinx.coroutines.launch

/**
 * 手机详情页面Activity
 */
class PhoneDetailActivity : AppCompatActivity() {
    
    // UI组件
    private lateinit var phoneDetailImage: ImageView
    private lateinit var phoneDetailName: TextView
    private lateinit var phoneDetailPrice: TextView
    private lateinit var favoriteButton: ImageView

    
    // 新增的TabLayout和ViewPager2组件
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: PhoneDetailPagerAdapter
    
    // 数据库相关
    private lateinit var roomUserDatabase: RoomUserDatabase
    private lateinit var appDatabase: AppDatabase
    
    // 当前手机ID
    private var currentPhoneId: Int = -1
    
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
        
        // 初始化数据库
        initDatabase()
        
        // 初始化视图
        initViews()
        
        // 加载手机数据
        loadPhoneData()
        
        // 设置收藏按钮点击事件
        setupFavoriteButton()
    }
    
    private fun initDatabase() {
        roomUserDatabase = RoomUserDatabase(this)
        appDatabase = AppDatabase.getDatabase(this)
    }
    
    private fun initViews() {
        phoneDetailImage = findViewById(R.id.iv_phone_detail_image)
        phoneDetailName = findViewById(R.id.tv_phone_detail_name)
        phoneDetailPrice = findViewById(R.id.tv_phone_detail_price)
        favoriteButton = findViewById(R.id.iv_favorite)
        
        // 初始化TabLayout和ViewPager2
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
    }
    
    private fun loadPhoneData() {
        // 从Intent中获取传递的手机数据
        val phoneId = intent.getIntExtra(EXTRA_PHONE_ID, -1)
        currentPhoneId = phoneId
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
        
        // 保存用户历史记录到数据库
        saveUserHistory(phoneId, phoneModel, brandNameStr)
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
    
    /**
     * 保存用户历史记录到数据库
     * 只有在用户已登录的情况下才保存
     */
    private fun saveUserHistory(phoneId: Int, phoneModel: String, phoneBrand: String) {
        lifecycleScope.launch {
            try {
                val currentUser = roomUserDatabase.getCurrentUser()
                
                // 只有在用户已登录的情况下才保存历史记录
                if (currentUser != null) {
                    val userHistoryDao = appDatabase.userHistoryDao()
                    
                    // 创建历史记录实体
                    val historyEntity = UserHistoryEntity(
                        userId = currentUser.id.toLong(),
                        phoneId = phoneId.toLong(),
                        phoneModel = phoneModel,
                        phoneBrand = phoneBrand,
                        viewTime = System.currentTimeMillis()
                    )
                    
                    // 插入或更新历史记录（如果用户之前查看过该手机，则更新查看时间）
                    userHistoryDao.insertOrUpdateHistory(historyEntity)
                    
                    // 可选：限制每个用户的历史记录数量，删除最旧的记录
                    val historyCount = userHistoryDao.getUserHistoryCount(currentUser.id.toLong())
                    val maxHistoryCount = 50 // 最多保存50条历史记录，UI显示最新7条
                    
                    if (historyCount > maxHistoryCount) {
                        val deleteCount = historyCount - maxHistoryCount
                        userHistoryDao.deleteOldestHistory(currentUser.id.toLong(), deleteCount)
                    }
                }
            } catch (e: Exception) {
                // 记录异常，但不影响用户体验
                e.printStackTrace()
            }
        }
    }
    
    private fun setupFavoriteButton() {
        // 初始化收藏按钮状态
        updateFavoriteButtonState()
        
        favoriteButton.setOnClickListener {
            handleFavoriteClick()
        }
    }
    
    private fun handleFavoriteClick() {
        lifecycleScope.launch {
            val currentUser = roomUserDatabase.getCurrentUser()
            
            if (currentUser == null) {
                // 用户未登录，显示登录提醒弹窗
                showLoginDialog()
            } else {
                // 用户已登录，切换收藏状态
                toggleFavoriteStatus(currentUser.id)
            }
        }
    }
    
    private fun showLoginDialog() {
        AlertDialog.Builder(this)
            .setTitle("登录提醒")
            .setMessage("您需要登录后才能使用收藏功能，是否前往登录？")
            .setPositiveButton("确定") { _, _ ->
                // 跳转到登录界面
                val intent = Intent(this, PhoneLoginActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private suspend fun toggleFavoriteStatus(userId: Int) {
        val favoriteDao = appDatabase.favoriteDao()
        val isFavorite = favoriteDao.isFavorite(userId, currentPhoneId)
        
        if (isFavorite) {
            // 取消收藏
            favoriteDao.deleteFavoriteByUserAndPhone(userId, currentPhoneId)
        } else {
            // 添加收藏
            val favorite = FavoriteEntity(
                userId = userId,
                phoneId = currentPhoneId
            )
            favoriteDao.insertFavorite(favorite)
        }
        
        // 更新收藏按钮状态
        updateFavoriteButtonState()
    }
    
    private fun updateFavoriteButtonState() {
        lifecycleScope.launch {
            val currentUser = roomUserDatabase.getCurrentUser()
            
            if (currentUser == null) {
                // 未登录状态，显示未收藏图标
                favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            } else {
                // 已登录，检查收藏状态
                val favoriteDao = appDatabase.favoriteDao()
                val isFavorite = favoriteDao.isFavorite(currentUser.id, currentPhoneId)
                
                if (isFavorite) {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                } else {
                    favoriteButton.setImageResource(R.drawable.ic_favorite_border)
                }
            }
        }
    }
}