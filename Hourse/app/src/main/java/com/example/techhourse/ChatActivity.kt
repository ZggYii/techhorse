package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.utils.SystemPromptGenerator
import com.example.techhourse.utils.TestDataGenerator
import com.google.android.material.snackbar.Snackbar
import com.example.techhourse.utils.SnackbarUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import com.example.techhourse.OpenAIApiClient
import com.example.techhourse.utils.RoomUserDatabase

class ChatActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: ImageView
    private lateinit var btnNewChat: ImageView
    private lateinit var etQuery: EditText
    private lateinit var btnSend: ImageView
    
    // 聊天相关组件
    private lateinit var welcomeLayout: LinearLayout
    private lateinit var rvChatMessages: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var openAIClient: OpenAIApiClient
    private val messages = mutableListOf<Message>()
    private var isChatStarted = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        initViews()
        setupListeners()
    }
    
    private fun initViews() {
        // 初始化DrawerLayout和NavigationView
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        
        // 初始化顶部按钮
        btnMenu = findViewById(R.id.btn_menu)
        btnNewChat = findViewById(R.id.btn_new_chat)
        
        // 初始化输入区域
        etQuery = findViewById(R.id.et_query)
        btnSend = findViewById(R.id.btn_send)
        
        // 初始化聊天相关组件
        welcomeLayout = findViewById(R.id.welcome_layout)
        rvChatMessages = findViewById(R.id.rv_chat_messages)
        
        // 初始化聊天组件
        initChatComponents()
        
        // 设置NavigationView监听器
        navigationView.setNavigationItemSelectedListener(this)
        
        // 设置侧滑界面用户名
        setupNavigationHeader()
    }
    
    private fun initChatComponents() {
        // 初始化OpenAI客户端
        openAIClient = OpenAIApiClient.getInstance()
        
        // 初始化聊天适配器
        chatAdapter = ChatAdapter(messages)
        rvChatMessages.layoutManager = LinearLayoutManager(this)
        rvChatMessages.adapter = chatAdapter
        
        // 初始状态显示欢迎界面
        showWelcomeScreen()
    }
    
    private fun showWelcomeScreen() {
        welcomeLayout.visibility = View.VISIBLE
        rvChatMessages.visibility = View.GONE
        isChatStarted = false
    }
    
    private fun showChatScreen() {
        welcomeLayout.visibility = View.GONE
        rvChatMessages.visibility = View.VISIBLE
        isChatStarted = true
    }
    
    private fun setupNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tv_user_name)
        
        lifecycleScope.launch {
            val roomUserDatabase = RoomUserDatabase.getInstance(this@ChatActivity)
            val currentUser = roomUserDatabase.getCurrentUser()
            
            if (currentUser != null && currentUser.phoneNumber.isNotEmpty()) {
                // 脱敏处理：4-7位用*号显示
                val phoneNumber = currentUser.phoneNumber
                val maskedPhone = if (phoneNumber.length >= 7) {
                    phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7)
                } else {
                    phoneNumber
                }
                tvUserName.text = maskedPhone
            } else {
                tvUserName.text = "zggyii"
            }
        }
    }
    
    private fun startNewChat() {
          messages.clear()
          chatAdapter.notifyDataSetChanged()
          showWelcomeScreen()
          etQuery.text.clear()
          SnackbarUtils.showNormalSnackbar(this, "已开始新对话")
      }
      
      private fun checkLoginAndSendMessage(query: String) {
          lifecycleScope.launch {
              val roomUserDatabase = RoomUserDatabase.getInstance(this@ChatActivity)
              val currentUser = roomUserDatabase.getCurrentUser()
              
              if (currentUser == null) {
                  // 未登录，显示提示框
                  showLoginRequiredDialog()
              } else {
                  // 已登录，直接发送消息
                  sendMessage(query)
              }
          }
      }
      
      private fun showLoginRequiredDialog() {
           val bottomSheetDialog = BottomSheetDialog(this)
           val dialogView = layoutInflater.inflate(R.layout.bottom_dialog_layout, null)
           
           // 设置标题和内容
           dialogView.findViewById<TextView>(R.id.tv_dialog_title).text = "登录提示"
           dialogView.findViewById<TextView>(R.id.tv_dialog_content).text = "请先登录才能使用聊天功能"
           
           // 设置关闭按钮点击事件
           dialogView.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
               bottomSheetDialog.dismiss()
           }
           
           // 设置取消按钮点击事件
           dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
               bottomSheetDialog.dismiss()
           }
           
           // 设置确认按钮点击事件（跳转登录）
           dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
               bottomSheetDialog.dismiss()
               // 跳转到登录页面
               val intent = Intent(this, PhoneLoginActivity::class.java)
               startActivity(intent)
           }
           
           bottomSheetDialog.setContentView(dialogView)
           bottomSheetDialog.show()
       }
     
     private fun sendMessage(messageText: String) {
         // 如果是第一次发送消息，切换到聊天界面
         if (!isChatStarted) {
             showChatScreen()
         }
         
         // 添加用户消息
         val userMessage = Message(messageText, SenderType.USER)
         messages.add(userMessage)
         chatAdapter.notifyItemInserted(messages.size - 1)
         
         // 滚动到最新消息
         rvChatMessages.scrollToPosition(messages.size - 1)
         
         // 清空输入框
         etQuery.text.clear()
         
         // 添加加载状态的AI消息
        val loadingMessage = Message("waiting...", SenderType.AI, status = MessageStatus.LOADING)
        messages.add(loadingMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChatMessages.scrollToPosition(messages.size - 1)
        
        val loadingMessageIndex = messages.size - 1
        
        // 异步调用API，带超时处理
        lifecycleScope.launch {
            try {
                // 动态生成个性化系统提示词
                val database = AppDatabase.getDatabase(this@ChatActivity)
                
                // 确保有测试数据（仅在开发阶段使用）
                TestDataGenerator.insertSampleUserBehavior(this@ChatActivity, database.userBehaviorDao(), database.phoneDao())
                
                val systemPrompt = SystemPromptGenerator.generateSystemPrompt(database.userBehaviorDao(), database.phoneDao())
                
                // 输出生成的系统提示词到日志（用于调试）
                Log.d("ChatActivity", "生成的系统提示词: $systemPrompt")
                
                // 设置20秒超时
                val aiResponse = withTimeout(20000L) {
                    openAIClient.chatCompletion(messageText, systemPrompt)
                }

                Log.d("ChatActivity", "AI Response: $aiResponse")

                // 更新加载消息为AI回复
                chatAdapter.updateLastMessage(aiResponse, MessageStatus.NORMAL)
                rvChatMessages.scrollToPosition(messages.size - 1)
                
            } catch (e: TimeoutCancellationException) {
                // 超时处理
                chatAdapter.updateLastMessage("响应超时", MessageStatus.TIMEOUT)
                rvChatMessages.scrollToPosition(messages.size - 1)
                
            } catch (e: Exception) {
                // 其他错误处理
                val errorMsg = when {
                    e.message?.contains("API key") == true -> "请先配置Gemini API密钥"
                    e.message?.contains("network") == true -> "网络连接错误，请检查网络设置"
                    else -> "抱歉，发生了错误：${e.message}"
                }
                chatAdapter.updateLastMessage(errorMsg, MessageStatus.NORMAL)
                rvChatMessages.scrollToPosition(messages.size - 1)
            }
        }
     }
    
    private fun setupListeners() {
        // 菜单按钮点击事件
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        
        // 添加DrawerLayout监听器，每次打开侧滑菜单时刷新用户名
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                setupNavigationHeader()
            }
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
        
        // 新建对话按钮点击事件
        btnNewChat.setOnClickListener {
            // 新建对话逻辑
            startNewChat()
        }
        
        // 发送按钮点击事件
        btnSend.setOnClickListener {
            val query = etQuery.text.toString().trim()
            if (query.isNotEmpty()) {
                checkLoginAndSendMessage(query)
            }
        }
        
        // 输入框回车键发送
        etQuery.setOnEditorActionListener { _, _, _ ->
            val query = etQuery.text.toString().trim()
            if (query.isNotEmpty()) {
                checkLoginAndSendMessage(query)
            }
            true
        }
    }
    

    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // 处理侧边菜单项点击
        when (item.itemId) {
            R.id.nav_check_update -> {
                // 提示版本信息
                SnackbarUtils.showNormalSnackbar(this, "目前已是最新版本")
            }
            R.id.nav_proto -> {
                // 展示服务协议
                showBottomDialog("服务协议", getRandomServiceAgreement())
            }
            R.id.nav_contractUs -> {
                // 跳转到联系页面
                showBottomDialog("联系我们", getRandomContactInfo())
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun showBottomDialog(title: String, content: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.bottom_dialog_layout, null)
        
        // 设置标题和内容
        dialogView.findViewById<TextView>(R.id.tv_dialog_title).text = title
        dialogView.findViewById<TextView>(R.id.tv_dialog_content).text = content
        
        // 设置关闭按钮点击事件
        dialogView.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }
    
    private fun getRandomServiceAgreement(): String {
        val agreements = arrayOf(
            "欢迎使用我们的服务！本协议规定了您使用我们服务时的权利和义务。我们致力于为用户提供优质的服务体验，同时保护用户的合法权益。",
            "服务协议条款：\n1. 用户应遵守相关法律法规；\n2. 不得从事违法违规活动；\n3. 保护个人隐私信息；\n4. 合理使用服务资源。\n",
            "用户在使用本服务时，应当遵守中华人民共和国相关法律法规，不得利用本服务从事违法违规活动，包括但不限于发布违法信息、侵犯他人权益等。"
        )
        return agreements.random()
    }
    
    private fun getRandomContactInfo(): String {
        val contactInfos = arrayOf(
            "客服电话：400-123-4567\n工作时间：周一至周五 9:00-18:00\n邮箱：support@nextphone.com\n地址：深圳市南山区",
            "联系我们：\n电话：010-12345678\n邮箱：contact@nextphone.com\n微信：nextphone_service\n",
            "客户服务：\n热线：800-123-4567\n在线客服：工作日 9:00-21:00\n邮箱：service@nextphone.com\n地址：深圳市南山区"
        )
        return contactInfos.random()
    }
    
    override fun onPause() {
        super.onPause()
        // 离开Activity时清除对话框内容
        clearChatContent()
    }
    
    private fun clearChatContent() {
        // 清空输入框
        etQuery.text.clear()
        
        // 清空聊天记录
        messages.clear()
        chatAdapter.notifyDataSetChanged()
        
        // 重置聊天状态
        isChatStarted = false
        
        // 显示欢迎界面
        showWelcomeScreen()
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
