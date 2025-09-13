package com.example.techhourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.techhourse.utils.SnackbarUtils
import com.example.techhourse.utils.RoomUserDatabase
import com.example.techhourse.utils.DatabaseDebugHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var etPhoneNumber: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnConfirm: Button
    private lateinit var cbAgreement: CheckBox

    // 预设的密保问题
    private val securityQuestions = arrayOf(
        "请选择密保问题",
        "您的出生地是哪里？",
        "您的第一个宠物叫什么名字？",
        "您最喜欢的颜色是什么？",
        "您的小学班主任姓什么？",
        "您母亲的姓名是什么？",
        "自定义问题"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        backArrow = findViewById(R.id.register_back_arrow)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnConfirm = findViewById(R.id.btn_confirm)
        cbAgreement = findViewById(R.id.cb_agreement)
    }

    private fun setupClickListeners() {
        backArrow.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            handleRegisterConfirm()
        }
    }

    private fun handleRegisterConfirm() {
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // 表单验证
        if (phoneNumber.isEmpty()) {
            SnackbarUtils.showSnackbar(this, "请输入手机号")
            return
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            SnackbarUtils.showSnackbar(this, "请输入有效的手机号")
            return
        }

        if (password.isEmpty()) {
            SnackbarUtils.showSnackbar(this, "请输入密码")
            return
        }

        if (password.length < 6) {
            SnackbarUtils.showSnackbar(this, "密码长度不能少于6位")
            return
        }

        if (confirmPassword.isEmpty()) {
            SnackbarUtils.showSnackbar(this, "请再次输入密码")
            return
        }

        if (password != confirmPassword) {
            SnackbarUtils.showSnackbar(this, "两次输入的密码不一致")
            return
        }

        if (!cbAgreement.isChecked) {
            SnackbarUtils.showSnackbar(this, "请先同意用户协议")
            return
        }

        // 表单验证通过，显示密保问题设置弹框
        showSecurityQuestionDialog(phoneNumber, password)
    }

    private fun showSecurityQuestionDialog(phoneNumber: String, password: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_security_question, null)
        bottomSheetDialog.setContentView(dialogView)

        val spinnerQuestion = dialogView.findViewById<Spinner>(R.id.spinner_security_question)
        val etCustomQuestion = dialogView.findViewById<EditText>(R.id.et_custom_question)
        val etSecurityAnswer = dialogView.findViewById<EditText>(R.id.et_security_answer)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirmSecurity = dialogView.findViewById<Button>(R.id.btn_confirm_security)

        // 设置Spinner适配器
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, securityQuestions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerQuestion.adapter = adapter

        // Spinner选择监听
        spinnerQuestion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == securityQuestions.size - 1) { // 选择了"自定义问题"
                    etCustomQuestion.visibility = View.VISIBLE
                } else {
                    etCustomQuestion.visibility = View.GONE
                    etCustomQuestion.text.clear()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 取消按钮
        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // 确认按钮
        btnConfirmSecurity.setOnClickListener {
            val selectedPosition = spinnerQuestion.selectedItemPosition
            val securityQuestion: String
            val securityAnswer = etSecurityAnswer.text.toString().trim()

            if (selectedPosition == 0) {
                SnackbarUtils.showSnackbar(this, "请选择密保问题")
                return@setOnClickListener
            }

            if (selectedPosition == securityQuestions.size - 1) { // 自定义问题
                val customQuestion = etCustomQuestion.text.toString().trim()
                if (customQuestion.isEmpty()) {
                    SnackbarUtils.showSnackbar(this, "请输入自定义问题")
                    return@setOnClickListener
                }
                securityQuestion = customQuestion
            } else {
                securityQuestion = securityQuestions[selectedPosition]
            }

            if (securityAnswer.isEmpty()) {
                SnackbarUtils.showSnackbar(this, "请输入密保答案")
                return@setOnClickListener
            }

            // 处理注册逻辑
            handleRegistration(phoneNumber, password, securityQuestion, securityAnswer)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun handleRegistration(phoneNumber: String, password: String, securityQuestion: String, securityAnswer: String) {
        val roomUserDatabase = RoomUserDatabase(this)
        
        // 使用协程执行注册
        CoroutineScope(Dispatchers.Main).launch {
            val registrationSuccess = roomUserDatabase.registerUser(phoneNumber, password, securityQuestion, securityAnswer)
            
            if (registrationSuccess) {
                // 注册成功，打印调试信息
                DatabaseDebugHelper.printAllUsers(this@RegisterActivity)
                DatabaseDebugHelper.checkPhoneNumber(this@RegisterActivity, phoneNumber)
                showSuccessDialog()
            } else {
                // 注册失败 - 手机号已被注册
                showFailureDialog("该手机号已被注册，请使用其他手机号或直接登录")
            }
        }
    }
    
    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("注册成功")
            .setMessage("恭喜您注册成功！即将跳转到登录界面")
            .setPositiveButton("确定") { _, _ ->
                // 跳转到登录界面
                val intent = Intent(this, PhoneLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showFailureDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("注册失败")
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // 简单的手机号验证，可以根据需要调整
        return phoneNumber.matches(Regex("^1[3-9]\\d{9}$"))
    }
}