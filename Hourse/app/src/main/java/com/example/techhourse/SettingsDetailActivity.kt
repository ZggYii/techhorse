package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Button
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.example.techhourse.utils.RoomUserDatabase
import com.example.techhourse.utils.SnackbarUtils
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
        
        // 获取当前用户信息并显示手机号
        val roomUserDatabase = RoomUserDatabase(this)
        val phoneNumInfo = findViewById<TextView>(R.id.phoneNum_info)
        
        CoroutineScope(Dispatchers.Main).launch {
            val currentUser = roomUserDatabase.getCurrentUser()
            if (currentUser != null) {
                // 隐私处理：4-7位显示为*号
                val phoneNumber = currentUser.phoneNumber
                val maskedPhone = if (phoneNumber.length >= 7) {
                    phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7)
                } else {
                    phoneNumber // 如果手机号长度不足7位，直接显示
                }
                phoneNumInfo?.text = maskedPhone
                
                // 检查密码状态
                if (currentUser.password.isNotEmpty()) {
                    tvPasswordStatus.text = "已设置"
                } else {
                    tvPasswordStatus.text = "未设置"
                }
            } else {
                phoneNumInfo?.text = "未登录"
                tvPasswordStatus.text = "未设置"
            }
        }
        
        // 设置手机号点击事件
        val phoneLayout = findViewById<LinearLayout>(R.id.ll_phone)
        phoneLayout.setOnClickListener {
            val intent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(intent)
        }
        
        // 设置密码设置项点击事件
        val passwordLayout = findViewById<LinearLayout>(R.id.ll_passw)
        passwordLayout.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val currentUser = roomUserDatabase.getCurrentUser()
                if (currentUser != null) {
                    // 检查密码是否已经设置
                    if (tvPasswordStatus.text == "已设置") {
                        // 密码已设置，跳转到修改密码界面
                        val intent = Intent(this@SettingsDetailActivity, ModifyPasswordActivity::class.java)
                        startActivityForResult(intent, REQUEST_SET_PASSWORD)
                    } else {
                        // 密码未设置，跳转到设置密码界面
                        val intent = Intent(this@SettingsDetailActivity, SetPasswordActivity::class.java)
                        intent.putExtra("phone_number", currentUser.phoneNumber)
                        startActivityForResult(intent, REQUEST_SET_PASSWORD)
                    }
                } else {
                    SnackbarUtils.showNormalSnackbar(this@SettingsDetailActivity, "用户未登录")
                }
            }
        }
        
        // 设置交易账户点击事件
        val transLayout = findViewById<LinearLayout>(R.id.ll_trans)
        transLayout.setOnClickListener {
            // 可以跳转到交易账户页面
        }
        
        // 设置实名认证点击事件
        val realLayout = findViewById<LinearLayout>(R.id.ll_real)
        realLayout.setOnClickListener {
            // 可以跳转到实名认证页面
        }

        // 设置切换账号点击事件
        val switchCountButton = findViewById<Button>(R.id.switchCount)
        switchCountButton.setOnClickListener {
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