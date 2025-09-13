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
import kotlinx.coroutines.launch
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CompareActivity : AppCompatActivity() {
    
    private lateinit var tvPhone1: TextView
    private lateinit var tvPhone2: TextView
    private lateinit var database: AppDatabase
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private var allPhones: List<PhoneEntity> = emptyList()
    private var selectedPhone1: PhoneEntity? = null
    private var selectedPhone2: PhoneEntity? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)
        
        initViews()
        initDatabase()
        setupClickListeners()
        loadPhoneData()
    }
    
    private fun initViews() {
        tvPhone1 = findViewById(R.id.tv_phone_1_name)
        tvPhone2 = findViewById(R.id.tv_phone_2_name)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        
        // 设置点击监听器将在setupClickListeners中处理
    }
    
    private fun initDatabase() {
        database = AppDatabase.getDatabase(this)
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
        // 将手机列表随机打乱
        val shuffledPhones = allPhones.shuffled()
        val adapter = PhoneSimpleAdapter(shuffledPhones) { selectedPhone ->
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
                adapter.filterPhones(shuffledPhones, query)
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
        // 显示比较结果区域
        val llCompareResult = findViewById<LinearLayout>(R.id.ll_compare_result)
        llCompareResult.visibility = View.VISIBLE
        
        // 隐藏提示文本
        val tvHint = findViewById<TextView>(R.id.tv_hint)
        tvHint.visibility = View.GONE
        
        val phone1 = selectedPhone1!!
        val phone2 = selectedPhone2!!
        
        // 创建CompareData实例
        val compareData = CompareData(
            phone1Id = phone1.id.toString(),
            phone1Model = phone1.phoneModel,
            phone1Price = phone1.price,
            phone1Memory = phone1.memoryConfig,
            phone1FrontCamera = phone1.frontCamera,
            phone1RearCamera = phone1.rearCamera,
            phone1Resolution = phone1.resolution,
            phone1ScreenSize = phone1.screenSize,
            phone1SellingPoint = phone1.sellingPoint,
            
            phone2Id = phone2.id.toString(),
            phone2Model = phone2.phoneModel,
            phone2Price = phone2.price,
            phone2Memory = phone2.memoryConfig,
            phone2FrontCamera = phone2.frontCamera,
            phone2RearCamera = phone2.rearCamera,
            phone2Resolution = phone2.resolution,
            phone2ScreenSize = phone2.screenSize,
            phone2SellingPoint = phone2.sellingPoint
        )
        
        // 设置ViewPager2适配器
        val adapter = CompareFragmentAdapter(this, compareData)
        viewPager.adapter = adapter
        
        // 配置TabLayout与ViewPager2的联动
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "基本参数"
                1 -> "综合对比"
                2 -> "差异对比"
                else -> "Tab ${position + 1}"
            }
        }.attach()
        
        // 添加TabLayout点击监听器
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 当点击综合对比标签时触发AI分析
                if (tab?.position == 1) {
                    triggerAiAnalysisForComprehensive()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 重新点击综合对比标签时也触发AI分析
                if (tab?.position == 1) {
                    triggerAiAnalysisForComprehensive()
                }
            }
        })
    }
    
    private fun triggerAiAnalysisForComprehensive() {
        // 获取当前ViewPager中的Fragment
        val adapter = viewPager.adapter as? CompareFragmentAdapter
        adapter?.let {
            val fragment = it.getFragment(1) // 获取综合对比Fragment（位置1）
            if (fragment is CompareComprehensiveFragment) {
                fragment.performAiAnalysis()
            }
        }
    }
}
