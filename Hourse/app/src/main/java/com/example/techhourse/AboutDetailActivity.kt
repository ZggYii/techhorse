package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random

class AboutDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_detail)
        
        // 设置ActionBar标题和返回按钮
        supportActionBar?.title = "关于我们"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.about_back_arrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }

    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}