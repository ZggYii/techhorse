package com.example.techhourse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.DatabaseInitializer
import com.example.techhourse.PhoneUsageInfoManager
import com.example.techhourse.utils.RoomUserDatabase
import com.example.techhourse.utils.SnackbarUtils
import com.example.techhourse.utils.UserBehaviorProcessor
import com.example.techhourse.utils.DatabaseDebugHelper
import com.example.techhourse.utils.DatabaseFixer
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.database.entity.UserHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    
    // 搜索相关视图
    private lateinit var searchText: EditText
    private lateinit var searchButton: ImageView
    
    // 用户头像
    private lateinit var userAvatar: ImageView
    
    private val historyPhoneCards = mutableListOf<PhoneCard>()
    private lateinit var historyAdapter: HistoryCardAdapter
    
    // 广播接收器
    private lateinit var historyBroadcastReceiver: BroadcastReceiver
    
    // 用户数据库
    private lateinit var roomUserDatabase: RoomUserDatabase
    
    companion object {
        const val ACTION_ADD_TO_HISTORY = "com.example.techhourse.ADD_TO_HISTORY"
        const val EXTRA_PHONE_ID = "phone_id"
        const val EXTRA_PHONE_NAME = "phone_name"
        const val EXTRA_PHONE_PRICE = "phone_price"
        const val EXTRA_PHONE_IMAGE_RESOURCE = "phone_image_resource"
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initViews()
        setupTabBar()
        setupRecyclerView()
        setupHistoryRecyclerView()
        setupSearchFunction()
        setupUserAvatar()
        setDefaultTab()

        // 启动时检查数据库状态并初始化
        initializeDatabaseOnStartup()
        
        // 初始化广播接收器
        setupBroadcastReceiver()
        
        // 初始化用户数据库
        roomUserDatabase = RoomUserDatabase(this)
        
        // 加载用户历史记录
        loadUserHistory()
    }

    private fun initializeDatabaseOnStartup() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取数据库实例
                val database = AppDatabase.getDatabase(this@MainActivity)

                // 修复imageResourceId错误赋值问题
                val fixedCount = DatabaseFixer.fixImageResourceIds(this@MainActivity)
                if (fixedCount > 0) {
                    withContext(Dispatchers.Main) {
                        SnackbarUtils.showSnackbar(
                            this@MainActivity,
                            "已修复 $fixedCount 个图片资源错误"
                        )
                    }
                }

                // 1. 重新初始化手机库数据（因为添加了新的图片字段）
                // DatabaseInitializer.reinitializePhoneData(this@MainActivity, database.phoneDao())
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
                        // 用户行为表已有数据，检查是否是首次启动
                        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val isFirstLaunch = sharedPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
                        
                        if (isFirstLaunch) {
                            // 首次启动，显示欢迎提示并标记为已启动
                            SnackbarUtils.showSnackbar(this@MainActivity, "欢迎回来！数据已加载完成")
                            sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
                        }
                        // 非首次启动，不显示提示
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

        // 初始化搜索相关视图
        searchText = findViewById(R.id.search_text)
        searchButton = findViewById(R.id.searchForCondi)
        
        // 初始化用户头像
        userAvatar = findViewById(R.id.user_avatar)
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
            PhoneCard(1, "TECNO", "中高端手机", "联发科", R.mipmap.image4),
            PhoneCard(2, "itel", "性价比之王", "联发科", R.mipmap.image31),
            PhoneCard(3, "Infinix", "发烧友最爱", "联发科", R.mipmap.image9)
        )

        // 设置布局管理器（水平滚动）
        rvPhoneCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 设置适配器
        val adapter = PhoneCardAdapter(phoneCards) { phoneCard ->
            // 点击事件处理 - 跳转到PhoneLibraryActivity并传递筛选条件
            val intent = Intent(this, PhoneLibraryActivity::class.java)
            intent.putExtra(PhoneLibraryActivity.EXTRA_SEARCH_KEYWORD, phoneCard.name)
            startActivity(intent)
        }

        rvPhoneCards.adapter = adapter
    }

    private fun setupHistoryRecyclerView() {
        // 设置布局管理器（水平滚动）
        rvHistoryCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 设置适配器
        historyAdapter = HistoryCardAdapter(historyPhoneCards) { phoneCard ->
            // 历史记录点击事件处理 - 通过id查询数据库并跳转到PhoneDetailActivity
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val database = AppDatabase.getDatabase(this@MainActivity)
                    val phoneEntity = database.phoneDao().getPhoneById(phoneCard.id)
                    
                    withContext(Dispatchers.Main) {
                        if (phoneEntity != null) {
                            // 跳转到PhoneDetailActivity并传递完整的手机信息
                            val intent = Intent(this@MainActivity, PhoneDetailActivity::class.java).apply {
                                putExtra(PhoneDetailActivity.EXTRA_PHONE_ID, phoneEntity.id.toInt())
                                putExtra(PhoneDetailActivity.EXTRA_PHONE_MODEL, phoneEntity.phoneModel)
                                putExtra(PhoneDetailActivity.EXTRA_BRAND_NAME, phoneEntity.brandName)
                                putExtra(PhoneDetailActivity.EXTRA_MARKET_NAME, phoneEntity.marketName)
                                putExtra(PhoneDetailActivity.EXTRA_MEMORY_CONFIG, phoneEntity.memoryConfig)
                                putExtra(PhoneDetailActivity.EXTRA_FRONT_CAMERA, phoneEntity.frontCamera)
                                putExtra(PhoneDetailActivity.EXTRA_REAR_CAMERA, phoneEntity.rearCamera)
                                putExtra(PhoneDetailActivity.EXTRA_RESOLUTION, phoneEntity.resolution)
                                putExtra(PhoneDetailActivity.EXTRA_SCREEN_SIZE, phoneEntity.screenSize)
                                putExtra(PhoneDetailActivity.EXTRA_SELLING_POINT, phoneEntity.sellingPoint)
                                putExtra(PhoneDetailActivity.EXTRA_PRICE, phoneEntity.price)
                                putExtra(PhoneDetailActivity.EXTRA_IMAGE_RESOURCE_ID, phoneEntity.imageResourceId)
                            }
                            startActivity(intent)
                        } else {
                            // 如果数据库中没有找到对应的手机信息，显示提示
                            SnackbarUtils.showSnackbar(this@MainActivity, "未找到该手机的详细信息")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        SnackbarUtils.showSnackbar(this@MainActivity, "加载手机信息失败")
                    }
                }
            }
        }

        rvHistoryCards.adapter = historyAdapter

        // 添加一些初始历史记录数据用于测试
//        addToHistory(PhoneCard(1, "itel", "性价比之王", "联发科", R.mipmap.image2))
//        addToHistory(PhoneCard(2, "TECNO", "中高端手机", "联发科", R.mipmap.image12))
//        addToHistory(PhoneCard(3, "Infinix", "发烧友最爱", "联发科", R.mipmap.infinix))
    }

    private fun setupUserAvatar() {
        userAvatar.setOnClickListener {
            showUserProfileBottomSheet()
        }
    }
    
    private fun showUserProfileBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_user_profile, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        
        // 获取当前用户信息并显示手机号和密码状态
        val roomUserDatabase = RoomUserDatabase(this)
        val phoneNumInfo = bottomSheetView.findViewById<TextView>(R.id.phoneNum_info)
        val passwordStatus = bottomSheetView.findViewById<TextView>(R.id.passw_set)
        
        CoroutineScope(Dispatchers.Main).launch {
            // 添加调试信息，查看数据库中的所有用户
            DatabaseDebugHelper.printAllUsers(this@MainActivity)
            
            val currentUser = roomUserDatabase.getCurrentUser()
            if (currentUser != null) {
                // 隐私处理：4-7位显示为*号
                val phoneNumber = currentUser.phoneNumber
                val maskedPhone = if (phoneNumber.length >= 7) {
                    phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7)
                } else {
                    phoneNumber // 如果手机号长度不足7位，直接显示
                }
                phoneNumInfo?.text = maskedPhone
                
                // 检查密码状态
                if (currentUser.password.isNotEmpty()) {
                    passwordStatus?.text = "已设置"
                } else {
                    passwordStatus?.text = "未设置"
                }
            } else {
                phoneNumInfo?.text = "未登录"
                passwordStatus?.text = "未设置"
            }
        }
        
        // 设置底部弹出框的点击事件
        bottomSheetView.findViewById<LinearLayout>(R.id.ll_phone)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            CoroutineScope(Dispatchers.Main).launch {
                val currentUser = roomUserDatabase.getCurrentUser()
                if (currentUser == null) {
                    // 只有在未登录状态下才跳转到登录页面
                    val intent = Intent(this@MainActivity, PhoneLoginActivity::class.java)
                    startActivity(intent)
                }
                // 如果已登录，则不执行任何操作
            }
        }
        
        bottomSheetView.findViewById<LinearLayout>(R.id.ll_passw)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            CoroutineScope(Dispatchers.Main).launch {
                val currentUser = roomUserDatabase.getCurrentUser()
                if (currentUser != null) {
                    // 检查密码是否已经设置
                    if (currentUser.password.isNotEmpty()) {
                        // 密码已设置，跳转到修改密码界面
                        val intent = Intent(this@MainActivity, ModifyPasswordActivity::class.java)
                        startActivity(intent)
                    } else {
                        // 密码未设置，跳转到设置密码界面
                        val intent = Intent(this@MainActivity, SetPasswordActivity::class.java)
                        intent.putExtra("phone_number", currentUser.phoneNumber)
                        startActivity(intent)
                    }
                } else {
                    SnackbarUtils.showNormalSnackbar(this@MainActivity, "用户未登录")
                }
            }
        }
        
        bottomSheetView.findViewById<LinearLayout>(R.id.ll_trans)?.setOnClickListener {
            // 处理交易账户点击事件
            bottomSheetDialog.dismiss()
            // 可以跳转到交易账户页面
        }
        
        bottomSheetView.findViewById<LinearLayout>(R.id.ll_real)?.setOnClickListener {
            // 处理实名认证点击事件
            bottomSheetDialog.dismiss()
            // 可以跳转到实名认证页面
        }
        
        bottomSheetView.findViewById<Button>(R.id.switchCount)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            CoroutineScope(Dispatchers.Main).launch {
                val currentUser = roomUserDatabase.getCurrentUser()
                if (currentUser != null) {
                    // 有登录用户，弹出确认对话框
                    showBottomConfirmDialog(
                        title = "切换账户",
                        message = "确定要切换到其他账户吗？",
                        onConfirm = {
                            CoroutineScope(Dispatchers.Main).launch {
                                roomUserDatabase.logout()
                                // 重置首次启动标记，让新用户登录时能看到欢迎提示
                                val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply()
                                val intent = Intent(this@MainActivity, PhoneLoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    )
                } else {
                    // 没有登录用户，直接跳转到登录界面
                    val intent = Intent(this@MainActivity, PhoneLoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        
        
        bottomSheetView.findViewById<Button>(R.id.logout)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            CoroutineScope(Dispatchers.Main).launch {
                val currentUser = roomUserDatabase.getCurrentUser()
                if (currentUser != null) {
                    // 有登录用户，弹出确认对话框
                    showBottomConfirmDialog(
                        title = "退出登录",
                        message = "确定要退出当前账户吗？",
                        onConfirm = {
                            CoroutineScope(Dispatchers.Main).launch {
                                roomUserDatabase.logout()
                                // 重置首次启动标记，让新用户登录时能看到欢迎提示
                                val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                sharedPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply()
                                val intent = Intent(this@MainActivity, PhoneLoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    )
                } else {
                    // 没有登录用户，直接跳转到登录界面
                    val intent = Intent(this@MainActivity, PhoneLoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        
        bottomSheetDialog.show()
    }
    
    /**
     * 显示底部确认对话框
     */
    private fun showBottomConfirmDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_bottom_confirm, null)
        
        // 设置对话框内容
        val titleTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val messageTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_message)
        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        val confirmButton = dialogView.findViewById<Button>(R.id.btn_confirm)
        
        titleTextView.text = title
        messageTextView.text = message
        
        // 设置按钮点击事件
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        
        confirmButton.setOnClickListener {
            bottomSheetDialog.dismiss()
            onConfirm()
        }
        
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }

    private fun setupSearchFunction() {
        searchButton.setOnClickListener {
            val searchQuery = searchText.text.toString().trim()

            if (searchQuery.isEmpty()) {
                // 如果搜索框为空，显示所有手机信息
                startPhoneLibraryActivity(null)
                return@setOnClickListener
            }

            // 判断输入类型
            if (isEnglishInput(searchQuery)) {
                // 英文输入：转为小写并进行模糊匹配
                startPhoneLibraryActivity(searchQuery.lowercase())
            } else {
                // 中文、符号、数字输入：显示所有手机信息
                startPhoneLibraryActivity(null)
            }
        }
    }

    private fun isEnglishInput(input: String): Boolean {
        // 检查输入是否只包含英文字母（包括大小写）
        return input.matches(Regex("^[a-zA-Z\\s]+$"))
    }

    private fun startPhoneLibraryActivity(searchQuery: String?) {
        val intent = Intent(this, PhoneLibraryActivity::class.java)
        searchQuery?.let {
            intent.putExtra(PhoneLibraryActivity.EXTRA_SEARCH_KEYWORD, it)
        }
        startActivity(intent)
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

        // 限制历史记录数量为7个
        if (historyPhoneCards.size > 7) {
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
            SnackbarUtils.showSnackbar(this, "权限已允许")
            bottomSheetDialog.dismiss()
            
            // 调用PhoneUsageInfoManager获取使用信息
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 获取数据库实例
                    val database = AppDatabase.getDatabase(this@MainActivity)
                    
                    // 重新初始化手机数据库（确保包含图片字段）
                    DatabaseInitializer.reinitializePhoneData(this@MainActivity, database.phoneDao())
                    
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
                        SnackbarUtils.showLongSnackbar(this@MainActivity, "使用信息已收集并保存")
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 切换到主线程显示错误信息
                    withContext(Dispatchers.Main) {
                        SnackbarUtils.showLongSnackbar(this@MainActivity, "收集使用信息时出错: ${e.message}")
                    }
                }
            }
        }
        
        // 设置不允许按钮点击事件
        dialogView.findViewById<Button>(R.id.btn_not_allow).setOnClickListener {
            SnackbarUtils.showSnackbar(this, "权限已拒绝")
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }
    
    private fun setupBroadcastReceiver() {
        historyBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_ADD_TO_HISTORY) {
                    val phoneId = intent.getIntExtra(EXTRA_PHONE_ID, -1)
                    val phoneName = intent.getStringExtra(EXTRA_PHONE_NAME) ?: ""
                    val phonePrice = intent.getStringExtra(EXTRA_PHONE_PRICE) ?: ""
                    val phoneImageResource = intent.getIntExtra(EXTRA_PHONE_IMAGE_RESOURCE, 0)
                    
                    // 创建PhoneCard对象并添加到历史记录
                    val phoneCard = PhoneCard(
                        id = phoneId,
                        name = phoneName,
                        description = "", // 历史记录不需要description
                        price = phonePrice,
                        imageResource = phoneImageResource
                    )

                    addToHistory(phoneCard)
                }
            }
        }

        // 注册广播接收器
        val filter = IntentFilter(ACTION_ADD_TO_HISTORY)
        // 动态注册广播
        registerReceiver(historyBroadcastReceiver, filter, Context.RECEIVER_EXPORTED)

    }

    private fun setDefaultTab() {
        // 默认选中首页
        switchTab(0)
    }
    
    override fun onResume() {
        super.onResume()
        // 重新加载用户头像状态
        setupUserAvatar()
        // 当从其他Activity返回时，重置导航栏到首页状态
        setDefaultTab()
        // 检查用户登录状态变化并处理历史记录
        handleUserLoginStatusChange()
    }
    
    /**
     * 处理用户登录状态变化
     * 只有在用户登录状态发生变化时才重新加载历史记录
     */
    private fun handleUserLoginStatusChange() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = roomUserDatabase.getCurrentUser()
                val isCurrentlyLoggedIn = currentUser != null
                
                // 检查登录状态是否发生变化
                val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val wasLoggedIn = sharedPrefs.getBoolean("was_logged_in", false)
                
                if (isCurrentlyLoggedIn != wasLoggedIn) {
                    // 登录状态发生变化，更新状态并重新加载历史记录
                    sharedPrefs.edit().putBoolean("was_logged_in", isCurrentlyLoggedIn).apply()
                    
                    withContext(Dispatchers.Main) {
                        if (isCurrentlyLoggedIn) {
                            // 用户刚登录，从数据库加载历史记录
                            historyPhoneCards.clear()
                            loadUserHistory()
                        } else {
                            // 用户刚登出，清空历史记录
                            historyPhoneCards.clear()
                            historyAdapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 注销广播接收器
        unregisterReceiver(historyBroadcastReceiver)
    }
    
    private fun loadUserHistory() {
         lifecycleScope.launch(Dispatchers.IO) {
             try {
                 val currentUser = roomUserDatabase.getCurrentUser()
                 if (currentUser != null) {
                     // 检查是否是app重启后的首次加载（通过检查UI历史记录是否为空）
                     val shouldLoadFromDatabase = historyPhoneCards.isEmpty()
                     
                     if (shouldLoadFromDatabase) {
                         val database = AppDatabase.getDatabase(this@MainActivity)
                         val historyList = database.userHistoryDao().getHistoryByUserIdWithLimit(currentUser.id.toLong(), 7)
                         
                         withContext(Dispatchers.Main) {
                             // 只有在UI历史记录为空时才从数据库加载
                             historyPhoneCards.clear()
                             
                             // 将数据库中的历史记录转换为PhoneCard并添加到列表
                             historyList.forEach { history ->
                                 lifecycleScope.launch(Dispatchers.IO) {
                                     try {
                                         val phoneEntity = database.phoneDao().getPhoneById(history.phoneId.toInt())
                                         if (phoneEntity != null) {
                                             val phoneCard = PhoneCard(
                                                 id = phoneEntity.id.toInt(),
                                                 name = phoneEntity.phoneModel,
                                                 description = phoneEntity.brandName,
                                                 price = phoneEntity.price,
                                                 imageResource = phoneEntity.imageResourceId
                                             )
                                             
                                             withContext(Dispatchers.Main) {
                                                 historyPhoneCards.add(phoneCard)
                                                 historyAdapter.notifyDataSetChanged()
                                             }
                                         }
                                     } catch (e: Exception) {
                                         e.printStackTrace()
                                     }
                                 }
                             }
                         }
                     }
                     // 如果UI历史记录不为空，说明是当前会话中的操作，不从数据库重新加载
                 } else {
                     // 用户未登录，清空历史记录
                     withContext(Dispatchers.Main) {
                         historyPhoneCards.clear()
                         historyAdapter.notifyDataSetChanged()
                     }
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
         }
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