package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.content.Intent
import com.google.android.material.snackbar.Snackbar

class SetPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivPasswordVisibility1: ImageView
    private lateinit var ivPasswordVisibility2: ImageView
    private lateinit var btnComplete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        ivPasswordVisibility1 = findViewById(R.id.iv_password_visibility_1)
        ivPasswordVisibility2 = findViewById(R.id.iv_password_visibility_2)
        btnComplete = findViewById(R.id.btn_complete)

        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.password_back_arrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // 密码可见性切换
        ivPasswordVisibility1.setOnClickListener {
            togglePasswordVisibility(etNewPassword, ivPasswordVisibility1)
        }

        ivPasswordVisibility2.setOnClickListener {
            togglePasswordVisibility(etConfirmPassword, ivPasswordVisibility2)
        }

        // 完成按钮点击事件
        btnComplete.setOnClickListener {
            validateAndSetPassword()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView) {
        try {
            val currentInputType = editText.inputType
            val isPasswordHidden = (currentInputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD
            
            if (isPasswordHidden) {
                // 显示密码
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imageView.setImageResource(R.drawable.ic_eye_on)
            } else {
                // 隐藏密码
                editText.inputType = InputType.TYPE_CLASS_TEXT
                imageView.setImageResource(R.drawable.ic_eye_off)
            }
            
            // 将光标移到末尾
            editText.setSelection(editText.text.length)
        } catch (e: Exception) {
            // 如果出现异常，重置为密码隐藏状态
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.ic_eye_off)
        }
    }

    private fun validateAndSetPassword() {
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // 验证密码
        when {
            newPassword.isEmpty() -> {
                Snackbar.make(findViewById(android.R.id.content), "请输入新密码", Snackbar.LENGTH_SHORT).show()
                return
            }
            confirmPassword.isEmpty() -> {
                Snackbar.make(findViewById(android.R.id.content), "请再次输入密码", Snackbar.LENGTH_SHORT).show()
                return
            }
            newPassword != confirmPassword -> {
                Snackbar.make(findViewById(android.R.id.content), "两次输入的密码不一致", Snackbar.LENGTH_SHORT).show()
                return
            }
            !isValidPassword(newPassword) -> {
                Snackbar.make(findViewById(android.R.id.content), "密码格式不正确，请按要求设置密码", Snackbar.LENGTH_SHORT).show()
                return
            }
        }

        // 密码验证通过，执行设置密码操作
        Snackbar.make(findViewById(android.R.id.content), "密码设置成功", Snackbar.LENGTH_SHORT).show()
        
        // 返回结果给SettingsDetailActivity
        val intent = Intent()
        intent.putExtra("password_set", true)
        setResult(RESULT_OK, intent)
        
        finish()
    }

    private fun isValidPassword(password: String): Boolean {
        // 检查密码长度
        if (password.length < 8 || password.length > 20) {
            return false
        }

        // 检查是否包含至少2种字符类型
        var hasLetter = false
        var hasDigit = false
        var hasSpecial = false

        for (char in password) {
            when {
                char.isLetter() -> hasLetter = true
                char.isDigit() -> hasDigit = true
                else -> hasSpecial = true
            }
        }

        val typeCount = listOf(hasLetter, hasDigit, hasSpecial).count { it }
        return typeCount >= 2
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
