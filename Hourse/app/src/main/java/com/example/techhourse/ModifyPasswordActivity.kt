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

class ModifyPasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var ivCurrentPasswordVisibility: ImageView
    private lateinit var ivNewPasswordVisibility: ImageView
    private lateinit var ivConfirmPasswordVisibility: ImageView
    private lateinit var btnComplete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_password)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etCurrentPassword = findViewById(R.id.et_current_password)
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password)
        ivCurrentPasswordVisibility = findViewById(R.id.iv_current_password_visibility)
        ivNewPasswordVisibility = findViewById(R.id.iv_new_password_visibility)
        ivConfirmPasswordVisibility = findViewById(R.id.iv_confirm_password_visibility)
        btnComplete = findViewById(R.id.btn_complete)

        // 设置返回箭头点击事件
        val backArrow = findViewById<ImageView>(R.id.modify_password_back_arrow)
        backArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // 密码可见性切换
        ivCurrentPasswordVisibility.setOnClickListener {
            togglePasswordVisibility(etCurrentPassword, ivCurrentPasswordVisibility)
        }

        ivNewPasswordVisibility.setOnClickListener {
            togglePasswordVisibility(etNewPassword, ivNewPasswordVisibility)
        }

        ivConfirmPasswordVisibility.setOnClickListener {
            togglePasswordVisibility(etConfirmNewPassword, ivConfirmPasswordVisibility)
        }

        // 完成按钮点击事件
        btnComplete.setOnClickListener {
            validateAndModifyPassword()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView) {
        try {
            val currentInputType = editText.inputType
            val isPasswordHidden = (currentInputType and InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD
            
            if (isPasswordHidden) {
                // 显示密码
                editText.inputType = InputType.TYPE_CLASS_TEXT
                imageView.setImageResource(R.drawable.ic_eye_on)
            } else {
                // 隐藏密码
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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

    private fun validateAndModifyPassword() {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmNewPassword = etConfirmNewPassword.text.toString()

        // 验证输入
        when {
            currentPassword.isEmpty() -> {
                SnackbarUtils.showNormalSnackbar(this, "请输入当前密码")
                return
            }
            newPassword.isEmpty() -> {
                SnackbarUtils.showNormalSnackbar(this, "请输入新密码")
                return
            }
            confirmNewPassword.isEmpty() -> {
                SnackbarUtils.showNormalSnackbar(this, "请再次输入新密码")
                return
            }
            newPassword != confirmNewPassword -> {
                SnackbarUtils.showNormalSnackbar(this, "两次输入的新密码不一致")
                return
            }
            !isValidPassword(newPassword) -> {
                SnackbarUtils.showNormalSnackbar(this, "新密码格式不正确，请按要求设置密码")
                return
            }
            currentPassword == newPassword -> {
                SnackbarUtils.showNormalSnackbar(this, "新密码不能与当前密码相同")
                return
            }
        }

        // 验证当前密码并更新新密码
        val roomUserDatabase = RoomUserDatabase(this)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 获取当前用户
                val currentUser = roomUserDatabase.getCurrentUser()
                if (currentUser == null) {
                    SnackbarUtils.showNormalSnackbar(this@ModifyPasswordActivity, "用户未登录")
                    return@launch
                }
                
                // 验证当前密码
                val isCurrentPasswordValid = roomUserDatabase.verifyPassword(currentUser.phoneNumber, currentPassword)
                if (!isCurrentPasswordValid) {
                    SnackbarUtils.showNormalSnackbar(this@ModifyPasswordActivity, "当前密码不正确")
                    return@launch
                }
                
                // 更新密码
                val success = roomUserDatabase.updatePassword(currentUser.phoneNumber, newPassword)
                
                if (success) {
                    showPasswordUpdateSuccessDialog()
                } else {
                    SnackbarUtils.showNormalSnackbar(this@ModifyPasswordActivity, "密码修改失败，请重试")
                }
            } catch (e: Exception) {
                SnackbarUtils.showNormalSnackbar(this@ModifyPasswordActivity, "密码修改失败：${e.message}")
            }
        }
    }

    private fun isValidPassword(password: String): Boolean {
        // 检查密码长度
        return password.length >= 6
    }
    
    private fun showPasswordUpdateSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("密码修改成功")
            .setMessage("您的密码已成功修改，请重新登录")
            .setPositiveButton("确定") { _, _ ->
                // 退出登录并跳转到登录界面
                val roomUserDatabase = RoomUserDatabase(this@ModifyPasswordActivity)
                CoroutineScope(Dispatchers.Main).launch {
                    roomUserDatabase.logout()
                    val intent = Intent(this@ModifyPasswordActivity, PhoneLoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
