package com.example.techhourse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.techhourse.database.AppDatabase
import com.example.techhourse.database.entity.PhoneEntity
import com.example.techhourse.utils.RoomUserDatabase
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
    private lateinit var roomUserDatabase: RoomUserDatabase
    
    companion object {
        const val EXTRA_SEARCH_KEYWORD = "search_keyword"
        const val EXTRA_SHOW_ALL = "show_all"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_library)
        
        // 初始化数据库
        database = AppDatabase.getDatabase(this)
        roomUserDatabase = RoomUserDatabase(this)
        
        // 初始化视图
        initViews()
        
        // 加载数据
        loadPhoneData()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.rv_phones)
        
        // 设置网格布局管理器，每行显示2个item
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        
        // 添加网格间距装饰器
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true))
        
        // 初始化适配器
        phoneAdapter = PhoneAdapter(emptyList())
        recyclerView.adapter = phoneAdapter
    }
    
    private fun loadPhoneData() {
        // 获取从MainActivity传递过来的搜索参数
        val searchQuery = intent.getStringExtra(EXTRA_SEARCH_KEYWORD)
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 调试：显示数据库中所有的品牌名称
                val allPhones = database.phoneDao().getAllPhones()

                var hasSearchResults = true
                val phoneList = if (searchQuery.isNullOrEmpty()) {
                    // 显示所有手机
                    allPhones
                } else {
                    
                    // 首先尝试精确品牌匹配（用于PhoneCard点击）
                    var searchResults = database.phoneDao().getPhonesByBrand(searchQuery.lowercase())
                    
                    // 如果精确匹配没有结果，尝试模糊搜索（用于搜索框输入）
                    if (searchResults.isEmpty()) {
                        searchResults = database.phoneDao().searchPhones(searchQuery)
                    }
                    
                    if (searchResults.isEmpty()) {
                        // 如果都没有匹配结果，显示所有手机
                        hasSearchResults = false
                        allPhones
                    } else {
                        searchResults
                    }
                }
                
                // 根据收藏状态排序手机列表，收藏的手机优先显示
                val sortedPhoneList = sortPhonesByFavoriteStatus(phoneList)
                
                // 获取当前用户的收藏状态
                val currentUser = roomUserDatabase.getCurrentUser()
                val favoritePhoneIds = if (currentUser != null) {
                    try {
                        val favoriteDao = database.favoriteDao()
                        favoriteDao.getFavoritesByUserId(currentUser.id)
                            .map { it.phoneId.toLong() }
                            .toSet()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptySet<Long>()
                    }
                } else {
                    emptySet<Long>()
                }
                
                // 切换到主线程更新UI
                withContext(Dispatchers.Main) {
                    phoneAdapter.updatePhones(sortedPhoneList)
                    phoneAdapter.updateFavoriteStatus(favoritePhoneIds)
                    
                    // 设置标题
                    supportActionBar?.title = if (searchQuery.isNullOrEmpty()) {
                        "手机库 (${sortedPhoneList.size}款)"
                    } else {
                        if (hasSearchResults) {
                            "搜索结果: $searchQuery (${sortedPhoneList.size}款)"
                        } else {
                            "找不到${searchQuery}中的内容 - 显示所有手机 (${sortedPhoneList.size}款)"
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
    
    /**
     * 根据收藏状态排序手机列表，收藏的手机优先显示
     */
    private suspend fun sortPhonesByFavoriteStatus(phoneList: List<PhoneEntity>): List<PhoneEntity> {
        return try {
            val currentUser = roomUserDatabase.getCurrentUser()
            
            if (currentUser == null) {
                // 用户未登录，返回原始列表
                phoneList
            } else {
                // 用户已登录，获取收藏列表
                val favoriteDao = database.favoriteDao()
                val favoritePhoneIds: Set<Int> = favoriteDao.getFavoritesByUserId(currentUser.id)
                    .map { it.phoneId }
                    .toSet()
                
                // 将手机列表分为收藏和未收藏两部分，然后合并
                val favoritePhones = phoneList.filter { favoritePhoneIds.contains(it.id) }
                val nonFavoritePhones = phoneList.filter { !favoritePhoneIds.contains(it.id) }
                
                // 收藏的手机在前，未收藏的在后
                favoritePhones + nonFavoritePhones
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 发生错误时返回原始列表
            phoneList
        }
    }
}