package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.example.techhourse.utils.SnackbarUtils

class PhoneLoginActivity : AppCompatActivity() {

    private lateinit var etPhoneNumber: EditText
    private lateinit var btnVerifyLogin: Button
    private lateinit var cbAgreement: CheckBox
    private lateinit var tvNumberUnavailable: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etPhoneNumber = findViewById(R.id.et_phone_number)
        btnVerifyLogin = findViewById(R.id.btn_verify_login)
        cbAgreement = findViewById(R.id.cb_agreement)

        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.login_back_arrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // 验证并登录按钮点击事件
        btnVerifyLogin.setOnClickListener {
            validateAndLogin()
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

        // 验证手机号
        if (phoneNumber.isEmpty()) {
            SnackbarUtils.showNormalSnackbar(this, "请输入手机号")
            return
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            SnackbarUtils.showNormalSnackbar(this, "请输入正确的手机号")
            return
        }

        // 验证用户协议
        if (!cbAgreement.isChecked) {
            SnackbarUtils.showNormalSnackbar(this, "请先同意用户协议")
            return
        }

        SnackbarUtils.showNormalSnackbar(this, "登录成功")
        finish()
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
