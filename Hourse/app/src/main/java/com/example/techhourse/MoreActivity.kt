package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.content.ContextCompat

class MoreActivity : AppCompatActivity() {

    private lateinit var tabSettings: LinearLayout
    private lateinit var tabFeedback: LinearLayout
    private lateinit var tabAbout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        initViews()
        setupTabBar()
    }

    private fun initViews() {
        // 初始化more界面的布局
        tabSettings = findViewById(R.id.ll_more_settings)
        tabFeedback = findViewById(R.id.ll_more_feedback)
        tabAbout = findViewById(R.id.ll_more_about)
    }

    private fun setupTabBar() {
        // 设置点击监听器
        tabSettings.setOnClickListener { switchTab(0) }
        tabFeedback.setOnClickListener { switchTab(1) }
        tabAbout.setOnClickListener { switchTab(2) }
    }

    private fun switchTab(position: Int) {

        // 根据位置设置选中状态
        when (position) {
            0 -> {
                // settings_detail
                startActivity(Intent(this, SettingsDetailActivity::class.java))
            }
            1 -> {
                // feedback_detail
                startActivity(Intent(this, FeedBackDetailActivity::class.java))
            }
            2 -> {
                // about_detail
                startActivity(Intent(this, AboutDetailActivity::class.java))
            }
        }
    }
}
