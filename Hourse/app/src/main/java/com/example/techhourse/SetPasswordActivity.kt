package com.example.techhourse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.example.techhourse.utils.SnackbarUtils
import com.example.techhourse.utils.RoomUserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetPasswordActivity : AppCompatActivity() {

    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivPasswordVisibility1: ImageView
    private lateinit var ivPasswordVisibility2: ImageView
    private lateinit var btnComplete: Button
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)

        // 获取传递的手机号
        phoneNumber = intent.getStringExtra("phone_number") ?: ""
        
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
                SnackbarUtils.showNormalSnackbar(this, "请输入新密码")
                return
            }
            confirmPassword.isEmpty() -> {
                SnackbarUtils.showNormalSnackbar(this, "请再次输入密码")
                return
            }
            newPassword != confirmPassword -> {
                SnackbarUtils.showNormalSnackbar(this, "两次输入的密码不一致")
                return
            }
            !isValidPassword(newPassword) -> {
                SnackbarUtils.showNormalSnackbar(this, "密码格式不正确，请按要求设置密码")
                return
            }
        }

        // 密码验证通过，执行设置密码操作
        updatePassword(newPassword)
    }

    private fun isValidPassword(password: String): Boolean {
        // 检查密码长度
        return password.length >= 6
    }
    
    private fun updatePassword(newPassword: String) {
        val roomUserDatabase = RoomUserDatabase(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            val success = roomUserDatabase.updatePassword(phoneNumber, newPassword)
            
            if (success) {
                showPasswordUpdateSuccessDialog()
            } else {
                showPasswordUpdateFailureDialog()
            }
        }
    }

    private fun showPasswordUpdateSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("密码修改成功")
            .setMessage("您的密码已成功修改，请使用新密码登录")
            .setPositiveButton("确定") { _, _ ->
                // 跳转到登录界面
                val intent = Intent(this, PhoneLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPasswordUpdateFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("密码修改失败")
            .setMessage("密码修改失败，请重试")
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
