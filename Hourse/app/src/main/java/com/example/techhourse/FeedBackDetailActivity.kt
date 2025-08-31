package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random

class FeedBackDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_detail)
        
        // 设置ActionBar标题和返回按钮
        supportActionBar?.title = "意见反馈"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.feedback_back_arrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }

        // 获取TextView并设置随机内容
        val contentTextView = findViewById<TextView>(R.id.tv_feedback_content)

        val randomContents = listOf(
            "感谢您使用我们的应用！\n\n我们非常重视您的反馈意见，您的建议将帮助我们不断改进产品。\n\n反馈渠道：\n• 应用内反馈\n• 邮箱：feedback@nextphone.com\n• 客服热线：400-123-4567\n\n常见问题：\n1. 如何重置密码？\n2. 如何更改个人信息？\n3. 如何开启推送通知？\n4. 如何导出数据？\n\n我们会认真处理每条反馈，并在3个工作日内回复您。",

            "您的反馈是我们进步的动力！\n\n我们致力于提供最好的用户体验，您的每一条建议都对我们非常重要。\n\n联系方式：\n• 在线客服：24小时在线\n• 邮箱：support@nextphone.com\n• 电话：400-888-9999\n\n热门问题：\n• 应用闪退怎么办？\n• 数据丢失如何恢复？\n• 如何联系客服？\n• 功能建议如何提交？\n\n我们承诺在24小时内回复您的反馈。",

            "欢迎来到反馈中心！\n\n您的意见对我们至关重要，我们将根据您的反馈持续优化产品。\n\n反馈方式：\n• 应用内反馈系统\n• 官方邮箱：help@nextphone.com\n• 客服微信：nextphone_support\n\n用户常见问题：\n1. 忘记密码怎么办？\n2. 如何修改绑定手机？\n3. 推送通知不显示？\n4. 如何备份数据？\n\n感谢您的支持与理解！"
        )

        val randomContent = randomContents[Random.nextInt(randomContents.size)]
        contentTextView.text = randomContent
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}