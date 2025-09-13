package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.techhourse.utils.SnackbarUtils
import com.example.techhourse.utils.RoomUserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.text.TextWatcher
import android.text.Editable

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etPhoneNumber: EditText
    private lateinit var tvSecurityQuestion: TextView
    private lateinit var etSecurityAnswer: EditText
    private lateinit var btnConfirm: androidx.appcompat.widget.AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initViews()
        setupListeners()
        
        // 接收从登录界面传递过来的手机号
        val phoneNumber = intent.getStringExtra("phone_number")
        if (!phoneNumber.isNullOrEmpty()) {
            etPhoneNumber.setText(phoneNumber)
        }
    }

    private fun initViews() {
        etPhoneNumber = findViewById(R.id.et_phone_number)
        tvSecurityQuestion = findViewById(R.id.tv_security_question)
        etSecurityAnswer = findViewById(R.id.et_security_answer)
        btnConfirm = findViewById(R.id.btn_confirm)

        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.forgot_password_back_arrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // 手机号输入框监听器 - 当手机号输入完成后自动获取密保问题
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val phoneNumber = s.toString().trim()
                if (phoneNumber.length == 11 && isValidPhoneNumber(phoneNumber)) {
                    // 手机号输入完成且格式正确，获取密保问题
                    loadSecurityQuestion(phoneNumber)
                } else {
                    // 清空密保问题显示
                    tvSecurityQuestion.text = "请先输入手机号码获取密保问题"
                }
            }
        })
        
        // 确认按钮点击事件
        btnConfirm.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val phoneNumber = etPhoneNumber.text.toString()
        val securityAnswer = etSecurityAnswer.text.toString()
        val currentQuestion = tvSecurityQuestion.text.toString()

        // 验证手机号
        if (phoneNumber.isEmpty()) {
            SnackbarUtils.showNormalSnackbar(this, "请输入手机号")
            return
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            SnackbarUtils.showNormalSnackbar(this, "请输入正确的手机号")
            return
        }
        
        // 检查密保问题是否已加载
        if (currentQuestion == "请先输入手机号码获取密保问题" || currentQuestion == "该手机号未注册") {
            if (currentQuestion == "请先输入手机号码获取密保问题") {
                SnackbarUtils.showNormalSnackbar(this, "请先输入正确的手机号")
            } else {
                SnackbarUtils.showNormalSnackbar(this, currentQuestion)
            }
            return
        }

        // 验证密保答案
        if (securityAnswer.isEmpty()) {
            SnackbarUtils.showNormalSnackbar(this, "请输入密保答案")
            return
        }

        // 验证密保答案
        verifySecurityAnswer(phoneNumber, securityAnswer)
    }
    
    /**
     * 加载密保问题
     */
    private fun loadSecurityQuestion(phoneNumber: String) {
        val roomUserDatabase = RoomUserDatabase(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            val securityQuestion = roomUserDatabase.getSecurityQuestion(phoneNumber)
            
            if (securityQuestion != null) {
                tvSecurityQuestion.text = securityQuestion
            } else {
                tvSecurityQuestion.text = "该手机号未注册"
            }
        }
    }
    
    private fun verifySecurityAnswer(phoneNumber: String, answer: String) {
        val roomUserDatabase = RoomUserDatabase(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            // 验证密保答案
            val isValid = roomUserDatabase.verifySecurityAnswer(phoneNumber, answer)
            
            if (isValid) {
                showVerificationSuccessDialog(phoneNumber)
            } else {
                showVerificationFailureDialog("密保答案错误，请重新输入")
            }
        }
    }
    
    private fun showVerificationSuccessDialog(phoneNumber: String) {
        AlertDialog.Builder(this)
            .setTitle("验证成功")
            .setMessage("密保验证成功，即将跳转到重设密码界面")
            .setPositiveButton("确定") { _, _ ->
                // 跳转到重设密码界面
                val intent = Intent(this, SetPasswordActivity::class.java)
                intent.putExtra("phone_number", phoneNumber)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showVerificationFailureDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("验证失败")
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                // 清空密保答案输入框
                etSecurityAnswer.text.clear()
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