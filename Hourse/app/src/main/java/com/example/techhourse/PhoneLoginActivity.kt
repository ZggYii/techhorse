package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.example.techhourse.utils.SnackbarUtils
import com.example.techhourse.utils.RoomUserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhoneLoginActivity : AppCompatActivity() {

    private lateinit var etPhoneNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var cbAgreement: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etPhoneNumber = findViewById(R.id.et_phone_number)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        cbAgreement = findViewById(R.id.cb_agreement)

        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.login_back_arrow)
        backArrow.setOnClickListener {
            // 返回到主界面
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupListeners() {
        // 登录按钮点击事件
        btnLogin.setOnClickListener {
            validateAndLogin()
        }

        // 注册按钮点击事件
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 忘记密码点击事件
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            // 如果手机号输入框有内容，携带到忘记密码界面
            val phoneNumber = etPhoneNumber.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                intent.putExtra("phone_number", phoneNumber)
            }
            startActivity(intent)
        }


        // 帮助按钮点击事件
        val tvHelp = findViewById<TextView>(R.id.tv_help)
        tvHelp.setOnClickListener {
            SnackbarUtils.showNormalSnackbar(this, "帮助")
        }

        // 第三方登录点击事件
        val ivWechat = findViewById<ImageView>(R.id.iv_wechat_login)
        ivWechat.setOnClickListener {
            SnackbarUtils.showNormalSnackbar(this, "微信登录")
        }

        val ivQQ = findViewById<ImageView>(R.id.iv_qq_login)
        ivQQ.setOnClickListener {
            SnackbarUtils.showNormalSnackbar(this, "QQ登录")
        }

        val ivApple = findViewById<ImageView>(R.id.iv_weibo_login)
        ivApple.setOnClickListener {
            SnackbarUtils.showNormalSnackbar(this, "微博登录")
        }

    }

    private fun validateAndLogin() {
        val phoneNumber = etPhoneNumber.text.toString()
        val password = etPassword.text.toString()

        // 验证手机号
        if (phoneNumber.isEmpty()) {
            SnackbarUtils.showNormalSnackbar(this, "请输入手机号")
            return
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            SnackbarUtils.showNormalSnackbar(this, "请输入正确的手机号")
            return
        }

        // 验证密码
        if (password.isEmpty()) {
            SnackbarUtils.showNormalSnackbar(this, "请输入密码")
            return
        }

        if (password.length < 6) {
            SnackbarUtils.showNormalSnackbar(this, "密码长度不能少于6位")
            return
        }

        // 验证用户协议
        if (!cbAgreement.isChecked) {
            SnackbarUtils.showNormalSnackbar(this, "请先同意用户协议")
            return
        }

        // 执行登录验证
        performLogin(phoneNumber, password)
    }
    
    private fun performLogin(phoneNumber: String, password: String) {
        val roomUserDatabase = RoomUserDatabase(this)
        
        // 使用协程执行登录
        CoroutineScope(Dispatchers.Main).launch {
            val userId = roomUserDatabase.loginUser(phoneNumber, password)
            
            if (userId != null) {
                // 登录成功
                showLoginSuccessDialog()
            } else {
                // 登录失败
                showLoginFailureDialog()
            }
        }
    }
    
    private fun showLoginSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("登录成功")
            .setMessage("已登录，即将跳转到主界面")
            .setPositiveButton("确定") { _, _ ->
                // 跳转到主界面
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showLoginFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("登录失败")
            .setMessage("密码或手机号错误，请重新输入")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                // 清空密码输入框
                etPassword.text.clear()
            }
            .show()
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // 简单的手机号验证（中国大陆手机号）
        return phoneNumber.matches(Regex("^1[3-9]\\d{9}$"))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
