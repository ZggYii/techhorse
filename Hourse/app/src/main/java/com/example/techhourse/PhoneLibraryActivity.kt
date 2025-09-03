package com.example.techhourse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.entity.PhoneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 手机库展示Activity
 * 用于展示所有手机信息或根据搜索条件筛选的手机信息
 */
class PhoneLibraryActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var phoneAdapter: PhoneAdapter
    private lateinit var database: AppDatabase
    
    companion object {
        const val EXTRA_SEARCH_KEYWORD = "search_keyword"
        const val EXTRA_SHOW_ALL = "show_all"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_library)
        
        // 初始化数据库
        database = AppDatabase.getDatabase(this)
        
        // 初始化视图
        initViews()
        
        // 加载数据
        loadPhoneData()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.rv_phones)
        
        // 设置网格布局管理器，每行显示2个item
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        
        // 初始化适配器
        phoneAdapter = PhoneAdapter(emptyList())
        recyclerView.adapter = phoneAdapter
    }
    
    private fun loadPhoneData() {
        // 获取从MainActivity传递过来的搜索参数
        val searchQuery = intent.getStringExtra("search_query")
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var hasSearchResults = true
                val phoneList = if (searchQuery.isNullOrEmpty()) {
                    // 显示所有手机
                    database.phoneDao().getAllPhones()
                } else {
                    // 根据phoneModel进行模糊匹配搜索（已转为小写）
                    val searchResults = database.phoneDao().searchPhonesByModel(searchQuery)
                    if (searchResults.isEmpty()) {
                        // 如果没有匹配结果，显示所有手机
                        hasSearchResults = false
                        database.phoneDao().getAllPhones()
                    } else {
                        searchResults
                    }
                }
                
                // 切换到主线程更新UI
                withContext(Dispatchers.Main) {
                    phoneAdapter.updatePhones(phoneList)
                    
                    // 设置标题
                    supportActionBar?.title = if (searchQuery.isNullOrEmpty()) {
                        "手机库 (${phoneList.size}款)"
                    } else {
                        if (hasSearchResults) {
                            "搜索结果: $searchQuery (${phoneList.size}款)"
                        } else {
                            "找不到${searchQuery}中的内容 - 显示所有手机 (${phoneList.size}款)"
                        }
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    supportActionBar?.title = "加载失败"
                }
            }
        }
    }
}