package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        initViews()
        setupTabBar()
        setDefaultTab()
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
    }
    
    private fun setupTabBar() {
        // 设置点击监听器
        tabHome.setOnClickListener { switchTab(0) }
        tabChat.setOnClickListener { switchTab(1) }
        tabCompare.setOnClickListener { switchTab(2) }
        tabMore.setOnClickListener { switchTab(3) }
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