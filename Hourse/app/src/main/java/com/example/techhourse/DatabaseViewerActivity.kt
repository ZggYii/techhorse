package com.example.techhourse

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.utils.DatabaseDebugHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据库查看器Activity
 * 用于查看和调试数据库中的用户数据
 */
class DatabaseViewerActivity : AppCompatActivity() {
    
    private lateinit var tvDatabaseInfo: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnBack: Button
    private lateinit var scrollView: ScrollView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database_viewer)
        
        initViews()
        setupClickListeners()
        loadDatabaseInfo()
    }
    
    private fun initViews() {
        tvDatabaseInfo = findViewById(R.id.tv_database_info)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnBack = findViewById(R.id.btn_back)
        scrollView = findViewById(R.id.scroll_view)
    }
    
    private fun setupClickListeners() {
        btnRefresh.setOnClickListener {
            loadDatabaseInfo()
        }
        
        btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun loadDatabaseInfo() {
        tvDatabaseInfo.text = "正在加载数据库信息..."
        
        CoroutineScope(Dispatchers.Main).launch {
            val databaseInfo = getDatabaseInfo()
            tvDatabaseInfo.text = databaseInfo
        }
    }
    
    private suspend fun getDatabaseInfo(): String = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(this@DatabaseViewerActivity)
            val userInfoDao = database.userInfoDao()
            
            val allUsers = userInfoDao.getAllUsers()
            val userCount = userInfoDao.getUserCount()
            val databasePath = DatabaseDebugHelper.getDatabasePath(this@DatabaseViewerActivity)
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            buildString {
                appendLine("=== 数据库信息 ===")
                appendLine("数据库名称: horse_racing_database")
                appendLine("数据库路径: $databasePath")
                appendLine("用户表名: user_info")
                appendLine("用户总数: $userCount")
                appendLine("查询时间: ${dateFormat.format(Date())}")
                appendLine()
                
                if (allUsers.isEmpty()) {
                    appendLine("❌ 数据库中没有用户数据！")
                    appendLine()
                    appendLine("可能的原因:")
                    appendLine("1. 还没有用户注册")
                    appendLine("2. 注册过程中出现了错误")
                    appendLine("3. 数据库迁移问题")
                    appendLine("4. 数据存储到了其他位置")
                    appendLine()
                    appendLine("建议解决方案:")
                    appendLine("1. 尝试重新注册一个用户")
                    appendLine("2. 检查Logcat中的错误信息")
                    appendLine("3. 清除应用数据后重新安装")
                } else {
                    appendLine("✅ 找到 ${allUsers.size} 个用户:")
                    appendLine()
                    
                    allUsers.forEachIndexed { index, user ->
                        appendLine("--- 用户 ${index + 1} ---")
                        appendLine("ID: ${user.id}")
                        appendLine("手机号: ${user.phoneNumber}")
                        appendLine("密码: ${if (user.password.isNotEmpty()) "已设置(${user.password.length}位)" else "未设置"}")
                        appendLine("密保问题: ${user.securityQuestion}")
                        appendLine("密保答案: ${if (user.securityAnswer.isNotEmpty()) "已设置" else "未设置"}")
                        appendLine("当前用户: ${if (user.isCurrentUser == 1) "是" else "否"}")
                        appendLine("创建时间: ${dateFormat.format(Date(user.createTime))}")
                        appendLine("更新时间: ${dateFormat.format(Date(user.updateTime))}")
                        appendLine()
                    }
                }
                
                appendLine("=== 调试提示 ===")
                appendLine("如果看不到注册的用户数据，请:")
                appendLine("1. 检查注册时是否有错误提示")
                appendLine("2. 查看Logcat日志中的错误信息")
                appendLine("3. 确认注册流程是否完整执行")
                appendLine("4. 尝试清除应用数据重新安装")
            }
            
        } catch (e: Exception) {
            "❌ 查询数据库时出错: ${e.message}\n\n错误详情: ${e.stackTraceToString()}"
        }
    }
}