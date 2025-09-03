package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.DatabaseInitializer
import com.example.techhourse.PhoneUsageInfoManager
import com.example.techhourse.utils.UserBehaviorProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    private lateinit var tabHome: LinearLayout
    private lateinit var tabChat: LinearLayout
    private lateinit var tabCompare: LinearLayout
    private lateinit var tabMore: LinearLayout
    
    private lateinit var ivHome: ImageView
    private lateinit var ivChat: ImageView
    private lateinit var ivCompare: ImageView
    private lateinit var ivMore: ImageView
    
    private lateinit var tvHome: TextView
    private lateinit var tvChat: TextView
    private lateinit var tvCompare: TextView
    private lateinit var tvMore: TextView
    
    private lateinit var rvPhoneCards: RecyclerView
    private lateinit var rvHistoryCards: RecyclerView
    
    private val historyPhoneCards = mutableListOf<PhoneCard>()
    private lateinit var historyAdapter: HistoryCardAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        initViews()
        setupTabBar()
        setupRecyclerView()
        setupHistoryRecyclerView()
        setDefaultTab()
        
        // 启动时检查数据库状态并初始化
        initializeDatabaseOnStartup()
    }
    
    private fun initializeDatabaseOnStartup() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取数据库实例
                val database = AppDatabase.getDatabase(this@MainActivity)
                
                // 1. 检查并初始化手机库数据
                DatabaseInitializer.initializePhoneData(this@MainActivity, database.phoneDao())
                
                // 2. 检查用户行为表是否有数据
                val userBehaviorCount = database.userBehaviorDao().getUserBehaviorCount()
                
                // 切换到主线程更新UI
                withContext(Dispatchers.Main) {
                    if (userBehaviorCount == 0) {
                        // 用户行为表没有数据，延迟显示权限对话框
                        Handler(Looper.getMainLooper()).postDelayed({
                            showPermissionDialog()
                        }, 500) // 延迟0.5秒显示
                    } else {
                        // 用户行为表已有数据，不显示权限对话框
                        // 可以在这里添加一些提示信息
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "欢迎回来！数据已加载完成",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                // 如果初始化出错，仍然显示权限对话框
                withContext(Dispatchers.Main) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        showPermissionDialog()
                    }, 500)
                }
            }
        }
    }
    
    private fun initViews() {
        // 初始化Tab布局
        tabHome = findViewById(R.id.tab_home)
        tabChat = findViewById(R.id.tab_chat)
        tabCompare = findViewById(R.id.tab_compare)
        tabMore = findViewById(R.id.tab_more)
        
        // 初始化图标
        ivHome = findViewById(R.id.iv_home)
        ivChat = findViewById(R.id.iv_chat)
        ivCompare = findViewById(R.id.iv_compare)
        ivMore = findViewById(R.id.iv_more)
        
        // 初始化文本
        tvHome = findViewById(R.id.tv_home)
        tvChat = findViewById(R.id.tv_chat)
        tvCompare = findViewById(R.id.tv_compare)
        tvMore = findViewById(R.id.tv_more)
        
        // 初始化RecyclerView
        rvPhoneCards = findViewById(R.id.rv_phone_cards)
        rvHistoryCards = findViewById(R.id.his_phone_cards)
    }
    
    private fun setupTabBar() {
        // 设置点击监听器
        tabHome.setOnClickListener { switchTab(0) }
        tabChat.setOnClickListener { switchTab(1) }
        tabCompare.setOnClickListener { switchTab(2) }
        tabMore.setOnClickListener { switchTab(3) }
    }
    
    private fun setupRecyclerView() {
        // 创建手机数据
        val phoneCards = listOf(
            PhoneCard(1, "Itel", "性价比之王", "联发科", R.mipmap.itel),
            PhoneCard(2, "TECNO", "中高端手机", "联发科", R.mipmap.tecno),
            PhoneCard(3, "Infinix", "发烧友最爱", "联发科", R.mipmap.infinix)
        )
        
        // 设置布局管理器（水平滚动）
        rvPhoneCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        // 设置适配器
        val adapter = PhoneCardAdapter(phoneCards) { phoneCard ->
            // 点击事件处理
            Snackbar.make(findViewById(android.R.id.content), "点击了: ${phoneCard.name}", Snackbar.LENGTH_SHORT).show()
            
            // 添加到历史记录
            addToHistory(phoneCard)
        }
        
        rvPhoneCards.adapter = adapter
    }
    
    private fun setupHistoryRecyclerView() {
        // 设置布局管理器（水平滚动）
        rvHistoryCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        // 设置适配器
        historyAdapter = HistoryCardAdapter(historyPhoneCards) { phoneCard ->
            // 历史记录点击事件处理
            Snackbar.make(findViewById(android.R.id.content), "历史记录: ${phoneCard.name}", Snackbar.LENGTH_SHORT).show()
        }
        
        rvHistoryCards.adapter = historyAdapter
        
        // 添加一些初始历史记录数据用于测试
        addToHistory(PhoneCard(1, "Itel", "性价比之王", "联发科", R.mipmap.itel))
        addToHistory(PhoneCard(2, "TECNO", "中高端手机", "联发科", R.mipmap.tecno))
        addToHistory(PhoneCard(3, "Infinix", "发烧友最爱", "联发科", R.mipmap.infinix))
    }
    
    private fun addToHistory(phoneCard: PhoneCard) {
        // 检查是否已经存在相同ID的记录
        val existingIndex = historyPhoneCards.indexOfFirst { it.id == phoneCard.id }
        
        if (existingIndex != -1) {
            // 如果已存在，移除旧记录
            historyPhoneCards.removeAt(existingIndex)
        }
        
        // 添加到历史记录开头
        historyPhoneCards.add(0, phoneCard)
        
        // 限制历史记录数量为5个
        if (historyPhoneCards.size > 5) {
            historyPhoneCards.removeAt(historyPhoneCards.size - 1)
        }
        
        // 通知适配器数据已更新
        historyAdapter.notifyDataSetChanged()
    }
    
    private fun showPermissionDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.permission_dialog_layout, null)
        
        // 设置允许按钮点击事件
        dialogView.findViewById<Button>(R.id.btn_allow).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), "权限已允许", Snackbar.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
            
            // 调用PhoneUsageInfoManager获取使用信息
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 获取数据库实例
                    val database = AppDatabase.getDatabase(this@MainActivity)
                    
                    // 初始化手机数据库（如果还没有初始化）
                    DatabaseInitializer.initializePhoneData(this@MainActivity, database.phoneDao())
                    
                    // 获取手机使用信息
                    val phoneUsageInfoManager = PhoneUsageInfoManager(this@MainActivity)
                    val usageInfoMap = phoneUsageInfoManager.getAllUsageInfo()
                    
                    // 处理并保存用户行为数据
                    UserBehaviorProcessor.processAndSaveUserBehavior(
                        usageInfoMap,
                        database.userBehaviorDao()
                    )
                    
                    // 切换到主线程更新UI
                    withContext(Dispatchers.Main) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "使用信息已收集并保存",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 切换到主线程显示错误信息
                    withContext(Dispatchers.Main) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "收集使用信息时出错: ${e.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        
        // 设置不允许按钮点击事件
        dialogView.findViewById<Button>(R.id.btn_not_allow).setOnClickListener {
            Snackbar.make(findViewById(android.R.id.content), "权限已拒绝", Snackbar.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }
    
    private fun setDefaultTab() {
        // 默认选中首页
        switchTab(0)
    }
    
    private fun switchTab(position: Int) {
        // 重置所有Tab状态
        resetAllTabs()
        
        // 根据位置设置选中状态
        when (position) {
            0 -> {
                // Home
                ivHome.setColorFilter(ContextCompat.getColor(this, R.color.primary))
                tvHome.setTextColor(ContextCompat.getColor(this, R.color.primary))
                // 这里可以加载Home Fragment或跳转到Home Activity
            }
            1 -> {
                // Chat
                ivChat.setColorFilter(ContextCompat.getColor(this, R.color.primary))
                tvChat.setTextColor(ContextCompat.getColor(this, R.color.primary))
                // 跳转到Chat Activity
                startActivity(Intent(this, ChatActivity::class.java))
            }
            2 -> {
                // 机型比较
                ivCompare.setColorFilter(ContextCompat.getColor(this, R.color.primary))
                tvCompare.setTextColor(ContextCompat.getColor(this, R.color.primary))
                // 跳转到机型比较 Activity
                startActivity(Intent(this, CompareActivity::class.java))
            }
            3 -> {
                // 更多
                ivMore.setColorFilter(ContextCompat.getColor(this, R.color.primary))
                tvMore.setTextColor(ContextCompat.getColor(this, R.color.primary))
                // 跳转到更多 Activity
                startActivity(Intent(this, MoreActivity::class.java))
            }
        }
    }
    
    private fun resetAllTabs() {
        // 重置所有图标颜色
        ivHome.setColorFilter(ContextCompat.getColor(this, R.color.gray))
        ivChat.setColorFilter(ContextCompat.getColor(this, R.color.gray))
        ivCompare.setColorFilter(ContextCompat.getColor(this, R.color.gray))
        ivMore.setColorFilter(ContextCompat.getColor(this, R.color.gray))
        
        // 重置所有文本颜色
        tvHome.setTextColor(ContextCompat.getColor(this, R.color.gray))
        tvChat.setTextColor(ContextCompat.getColor(this, R.color.gray))
        tvCompare.setTextColor(ContextCompat.getColor(this, R.color.gray))
        tvMore.setTextColor(ContextCompat.getColor(this, R.color.gray))
    }
}