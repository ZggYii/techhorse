package com.example.techhourse

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.utils.SnackbarUtils
import com.example.techhourse.utils.SystemPromptGenerator
import com.example.techhourse.OpenAIApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.ImageView
import android.widget.TextView

class CompareActivity : AppCompatActivity() {
    
    private lateinit var tvPhone1: TextView
    private lateinit var tvPhone2: TextView
    private lateinit var llCompareContent: LinearLayout
    private lateinit var svCompareResult: ScrollView
    private lateinit var btnAiAnalysis: Button
    private lateinit var database: AppDatabase
    private var allPhones: List<PhoneEntity> = emptyList()
    private var selectedPhone1: PhoneEntity? = null
    private var selectedPhone2: PhoneEntity? = null
    private lateinit var openAIClient: OpenAIApiClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)
        
        initViews()
        initDatabase()
        initOpenAIClient()
        setupClickListeners()
        loadPhoneData()
    }
    
    private fun initViews() {
        tvPhone1 = findViewById(R.id.tv_phone_1_name)
        tvPhone2 = findViewById(R.id.tv_phone_2_name)
        llCompareContent = findViewById(R.id.ll_compare_content)
        svCompareResult = findViewById(R.id.sv_compare_result)
        btnAiAnalysis = findViewById(R.id.btn_ai_analysis)
        
        // 设置点击监听器将在setupClickListeners中处理
    }
    
    private fun initDatabase() {
        database = AppDatabase.getDatabase(this)
    }
    
    private fun initOpenAIClient() {
        openAIClient = OpenAIApiClient.getInstance()
    }
    
    private fun setupClickListeners() {
        val llPhone1Selector = findViewById<LinearLayout>(R.id.ll_phone_1_selector)
        val llPhone2Selector = findViewById<LinearLayout>(R.id.ll_phone_2_selector)
        
        llPhone1Selector.setOnClickListener {
            showPhoneSelector(1)
        }
        
        llPhone2Selector.setOnClickListener {
            showPhoneSelector(2)
        }
        
        // AI分析按钮点击事件
        btnAiAnalysis.setOnClickListener {
            performAiAnalysis()
        }
    }
    
    private fun loadPhoneData() {
        lifecycleScope.launch {
            allPhones = database.phoneDao().getAllPhones()
        }
    }
    
    private fun showPhoneSelector(phonePosition: Int) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_phone_selector, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        
        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.rv_phone_list)
        val searchEditText = bottomSheetView.findViewById<EditText>(R.id.et_search)

        // 设置RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PhoneSimpleAdapter(allPhones) { selectedPhone ->
            onPhoneSelected(phonePosition, selectedPhone)
            bottomSheetDialog.dismiss()
        }
        recyclerView.adapter = adapter
        
        // 设置搜索功能
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                adapter.filterPhones(allPhones, query)
            }
        })
        
        bottomSheetDialog.show()
    }
    
    private fun onPhoneSelected(phonePosition: Int, phone: PhoneEntity) {
        when (phonePosition) {
            1 -> {
                selectedPhone1 = phone
                tvPhone1.text = "${phone.phoneModel}"
                
                // 更新第一个选择器的图片
                val imageView1 = findViewById<ImageView>(R.id.iv_phone_1_image)
                
                // 根据手机图片资源名称设置图片
                val imageResId = resources.getIdentifier(phone.imageResourceId.toString(), "drawable", packageName)
                if (imageResId != 0) {
                    imageView1.setImageResource(imageResId)
                    imageView1.scaleType = ImageView.ScaleType.CENTER_CROP
                    // 移除tint以显示原始图片颜色
                    imageView1.imageTintList = null
                    // 固定图片大小
                    val layoutParams = imageView1.layoutParams
                    layoutParams.width = resources.getDimensionPixelSize(R.dimen.phone_image_size)
                    layoutParams.height = resources.getDimensionPixelSize(R.dimen.phone_image_size)
                    imageView1.layoutParams = layoutParams
                } else {
                    // 如果找不到图片资源，保持默认的加号图标
                    imageView1.setImageResource(R.drawable.ic_add)
                }
            }
            2 -> {
                selectedPhone2 = phone
                tvPhone2.text = "${phone.phoneModel}"
                
                // 更新第二个选择器的图片
                val imageView2 = findViewById<ImageView>(R.id.iv_phone_2_image)
                
                // 根据手机图片资源名称设置图片
                val imageResId = resources.getIdentifier(phone.imageResourceId.toString(), "drawable", packageName)
                if (imageResId != 0) {
                    imageView2.setImageResource(imageResId)
                    imageView2.scaleType = ImageView.ScaleType.CENTER_CROP
                    // 移除tint以显示原始图片颜色
                    imageView2.imageTintList = null
                    // 固定图片大小
                    val layoutParams = imageView2.layoutParams
                    layoutParams.width = resources.getDimensionPixelSize(R.dimen.phone_image_size)
                    layoutParams.height = resources.getDimensionPixelSize(R.dimen.phone_image_size)
                    imageView2.layoutParams = layoutParams
                } else {
                    // 如果找不到图片资源，保持默认的加号图标
                    imageView2.setImageResource(R.drawable.ic_add)
                }
            }
        }
        
        // 如果两个手机都选择了，显示比较结果
        if (selectedPhone1 != null && selectedPhone2 != null) {
            showCompareResult()
        }
    }
    
    private fun showCompareResult() {
        llCompareContent.removeAllViews()
        svCompareResult.visibility = View.VISIBLE
        
        // 隐藏提示文本
        val tvHint = findViewById<TextView>(R.id.tv_hint)
        tvHint.visibility = View.GONE
        
        val phone1 = selectedPhone1!!
        val phone2 = selectedPhone2!!
        
        // 创建比较结果视图
        val compareView = layoutInflater.inflate(R.layout.layout_compare_result, llCompareContent, false)
        
        // 填充比较数据（这里可以根据需要添加更多比较项）
        val tvPhone1Price = compareView.findViewById<TextView>(R.id.tv_phone1_price)
        val tvPhone2Price = compareView.findViewById<TextView>(R.id.tv_phone2_price)
        val tvPhone1Memory = compareView.findViewById<TextView>(R.id.tv_phone1_memory)
        val tvPhone2Memory = compareView.findViewById<TextView>(R.id.tv_phone2_memory)

        tvPhone1Price.text = phone1.price
        tvPhone2Price.text = phone2.price
        tvPhone1Memory.text = phone1.memoryConfig
        tvPhone2Memory.text = phone2.memoryConfig
        
        // 设置前置摄像头对比
        compareView.findViewById<TextView>(R.id.tv_phone1_front_camera).text = phone1.frontCamera ?: "未知"
        compareView.findViewById<TextView>(R.id.tv_phone2_front_camera).text = phone2.frontCamera ?: "未知"
        
        // 设置后置摄像头对比
        compareView.findViewById<TextView>(R.id.tv_phone1_rear_camera).text = phone1.rearCamera ?: "未知"
        compareView.findViewById<TextView>(R.id.tv_phone2_rear_camera).text = phone2.rearCamera ?: "未知"
        
        // 设置屏幕分辨率对比
        compareView.findViewById<TextView>(R.id.tv_phone1_screen_resolution).text = phone1.resolution ?: "未知"
        compareView.findViewById<TextView>(R.id.tv_phone2_screen_resolution).text = phone2.resolution ?: "未知"
        
        // 设置屏幕尺寸对比
        compareView.findViewById<TextView>(R.id.tv_phone1_screen_size).text = phone1.screenSize ?: "未知"
        compareView.findViewById<TextView>(R.id.tv_phone2_screen_size).text = phone2.screenSize ?: "未知"
        
        // 设置卖点对比
        compareView.findViewById<TextView>(R.id.tv_phone1_selling_point).text = phone1.sellingPoint ?: "未知"
        compareView.findViewById<TextView>(R.id.tv_phone2_selling_point).text = phone2.sellingPoint ?: "未知"
        
        llCompareContent.addView(compareView)
        
        // 显示AI分析按钮
        btnAiAnalysis.visibility = View.VISIBLE
    }
    
    private fun performAiAnalysis() {
        val phone1 = selectedPhone1
        val phone2 = selectedPhone2
        
        if (phone1 == null || phone2 == null) {
            SnackbarUtils.showSnackbar(this, "请先选择两部手机进行比较")
            return
        }
        
        // 显示加载提示
        SnackbarUtils.showNormalSnackbar(this, "正在进行AI智能分析，请稍候...")
        
        // 异步调用OpenAI接口
        lifecycleScope.launch {
            try {
                // 将手机详细信息添加到系统提示词中
                val systemPrompt = ""
                
                // 用户查询保持简洁
                val userQuery = "给出购买建议"

//                val userQuery = "请对以下两款手机进行详细的对比分析"
                Log.d("CompareActivity", "用户查询: $userQuery")
                Log.d("CompareActivity", "开始AI分析...")
                Log.d("CompareActivity", "OpenAI客户端实例: ${openAIClient.javaClass.simpleName}")
                Log.d("CompareActivity", "API密钥配置状态: ${OpenAIApiClient.isApiKeyConfigured()}")
                
                // 设置20秒超时
                val aiResponse = withTimeout(20000L) {
                    openAIClient.chatCompletion(userQuery, systemPrompt)
                }
                
                Log.d("CompareActivity", "AI分析结果: $aiResponse")
                
                // 检查响应是否为错误信息
                if (aiResponse.contains("网络连接超时") || 
                    aiResponse.contains("无法连接到服务器") || 
                    aiResponse.contains("API密钥无效") || 
                    aiResponse.contains("请求失败") ||
                    aiResponse.contains("请求过于频繁")) {
                    // 这是一个错误响应
                    showAiAnalysisResult("AI分析遇到问题：$aiResponse")
                } else {
                    // 正常的AI分析结果
                    showAiAnalysisResult(aiResponse)
                }
                
            } catch (e: TimeoutCancellationException) {
                // 超时处理
                Log.e("CompareActivity", "AI分析超时", e)
                showAiAnalysisResult("AI分析响应超时，请稍后重试")
                
            } catch (e: Exception) {
                // 其他错误处理
                Log.e("CompareActivity", "AI分析出错", e)
                val errorMsg = when {
                    e.message?.contains("API key") == true || e.message?.contains("API密钥") == true -> "请先配置AI API密钥"
                    e.message?.contains("网络") == true || e.message?.contains("连接") == true || e.message?.contains("超时") == true -> "网络连接错误，请检查网络设置"
                    else -> "AI分析出现错误：${e.message}"
                }
                showAiAnalysisResult(errorMsg)
            }
        }
    }
    
    /**
     * 显示AI分析结果的底部弹窗
     */
    private fun showAiAnalysisResult(analysisResult: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.bottom_dialog_layout, null)
        
        // 设置标题和内容
        dialogView.findViewById<TextView>(R.id.tv_dialog_title).text = "AI智能分析结果"
        dialogView.findViewById<TextView>(R.id.tv_dialog_content).text = analysisResult
        
        // 设置关闭按钮点击事件
        dialogView.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()
    }
}
