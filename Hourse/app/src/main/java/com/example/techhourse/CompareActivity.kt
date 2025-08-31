package com.example.techhourse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class CompareActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 创建一个简单的布局
        val textView = TextView(this)
        textView.text = "机型比较页面"
        textView.textSize = 24f
        textView.setPadding(50, 50, 50, 50)
        
        setContentView(textView)
    }
}
