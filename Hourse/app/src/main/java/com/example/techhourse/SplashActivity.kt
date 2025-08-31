package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // 获取动画元素
        val logoImageView = findViewById<ImageView>(R.id.iv_logo)
        val appNameTextView = findViewById<TextView>(R.id.tv_app_name)
        val taglineTextView = findViewById<TextView>(R.id.tv_tagline)
        
        // 加载动画
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        
        // 设置动画监听器
        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            
            override fun onAnimationEnd(animation: Animation?) {
                // 动画结束后延迟跳转到主界面
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1000) // 延迟1秒
            }
            
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        
        // 开始动画
        logoImageView.startAnimation(scaleAnimation)
        appNameTextView.startAnimation(slideUpAnimation)
        taglineTextView.startAnimation(fadeInAnimation)
    }
}
