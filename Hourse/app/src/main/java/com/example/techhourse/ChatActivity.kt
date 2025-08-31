package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class ChatActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: ImageView
    private lateinit var btnNewChat: ImageView
    private lateinit var etQuery: EditText
    private lateinit var btnSend: ImageView
    
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
        
        // 设置NavigationView监听器
        navigationView.setNavigationItemSelectedListener(this)
    }
    
    private fun setupListeners() {
        // 菜单按钮点击事件
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        
        // 新建对话按钮点击事件
        btnNewChat.setOnClickListener {
            // 清空输入框并重新开始对话
            etQuery.text.clear()
            Snackbar.make(findViewById(android.R.id.content), "已经在新对话中", Snackbar.LENGTH_SHORT).show()
        }
        
        // 发送按钮点击事件
        btnSend.setOnClickListener {
            sendMessage()
        }
        
        // 输入框回车键发送
        etQuery.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }
    
    private fun sendMessage() {
        val message = etQuery.text.toString().trim()
        
        if (TextUtils.isEmpty(message)) {
            Snackbar.make(findViewById(android.R.id.content), "请输入消息", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        // 这里可以添加发送消息的逻辑
        // 例如：调用API、保存到数据库等
        Snackbar.make(findViewById(android.R.id.content), "发送消息: $message", Snackbar.LENGTH_SHORT).show()
        
        // 清空输入框
        etQuery.text.clear()
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // 处理侧边菜单项点击
        when (item.itemId) {
            R.id.nav_check_update -> {
                // 提示版本信息
                Snackbar.make(findViewById(android.R.id.content), "目前已是最新版本", Snackbar.LENGTH_SHORT).show()
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
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
