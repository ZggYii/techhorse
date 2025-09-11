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

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etPhoneNumber: EditText
    private lateinit var tvSecurityQuestion: TextView
    private lateinit var etSecurityAnswer: EditText
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initViews()
        setupListeners()
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
        // 确认按钮点击事件
        btnConfirm.setOnClickListener {
            validateAndSubmit()
        }

        // 帮助按钮点击事件
        val tvHelp = findViewById<TextView>(R.id.tv_help)
        tvHelp.setOnClickListener {
            SnackbarUtils.showNormalSnackbar(this, "帮助")
        }
    }

    private fun validateAndSubmit() {
        val phoneNumber = etPhoneNumber.text.toString()
        val securityAnswer = etSecurityAnswer.text.toString()

        // 验证手机号
        if (phoneNumber.isEmpty()) {
            SnackbarUtils.showNormalSnackbar(this, "请输入手机号")
            return
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            SnackbarUtils.showNormalSnackbar(this, "请输入正确的手机号")
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
    
    private fun verifySecurityAnswer(phoneNumber: String, answer: String) {
        val roomUserDatabase = RoomUserDatabase(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            // 首先检查用户是否存在并获取密保问题
            val securityQuestion = roomUserDatabase.getSecurityQuestion(phoneNumber)
            
            if (securityQuestion == null) {
                showVerificationFailureDialog("该手机号未注册")
                return@launch
            }
            
            // 更新密保问题显示
            tvSecurityQuestion.text = securityQuestion
            
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