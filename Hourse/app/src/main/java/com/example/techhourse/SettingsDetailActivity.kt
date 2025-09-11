package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.content.Intent
import android.widget.Button
import kotlin.random.Random
import com.example.techhourse.utils.RoomUserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsDetailActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_SET_PASSWORD = 1001
    }

    private lateinit var tvPasswordStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_detail)
        
        // 设置ActionBar标题和返回按钮
        supportActionBar?.title = "设置"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.settings_back_arrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }
        
        // 初始化密码状态TextView
        tvPasswordStatus = findViewById(R.id.passw_set)
        
        // 设置密码设置项点击事件
        val passwordLayout = findViewById<LinearLayout>(R.id.ll_passw)
        passwordLayout.setOnClickListener {
            // 检查密码是否已经设置
            if (tvPasswordStatus.text == "已设置") {
                // 密码已设置，跳转到修改密码界面
                val intent = Intent(this, ModifyPasswordActivity::class.java)
                startActivityForResult(intent, REQUEST_SET_PASSWORD)
            } else {
                // 密码未设置，跳转到设置密码界面
                val intent = Intent(this, SetPasswordActivity::class.java)
                startActivityForResult(intent, REQUEST_SET_PASSWORD)
            }
        }

        // 设置手机号点击事件
        val phoneLayout = findViewById<LinearLayout>(R.id.ll_phone)
        phoneLayout.setOnClickListener {
            val intent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(intent)
        }

        // 设置切换账号点击事件
        val switchCountButton = findViewById<Button>(R.id.switchCount)
        switchCountButton.setOnClickListener {
            val roomUserDatabase = RoomUserDatabase(this)
            CoroutineScope(Dispatchers.Main).launch {
                roomUserDatabase.logout()
                val intent = Intent(this@SettingsDetailActivity, PhoneLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // 设置退出登录点击事件
        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            val roomUserDatabase = RoomUserDatabase(this)
            CoroutineScope(Dispatchers.Main).launch {
                roomUserDatabase.logout()
                val intent = Intent(this@SettingsDetailActivity, PhoneLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_SET_PASSWORD && resultCode == RESULT_OK) {
            // 密码设置成功，更新状态显示
            tvPasswordStatus.text = "已设置"
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}