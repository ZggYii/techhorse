package com.example.techhourse.database

import android.content.Context
import com.example.techhourse.database.dao.PhoneDao
import com.example.techhourse.utils.ExcelReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 数据库初始化类
 * 负责从Excel文件导入手机数据到数据库
 */
class DatabaseInitializer {
    
    companion object {
        /**
         * 初始化手机数据库
         * 从Excel文件读取数据并插入到数据库中
         */
        suspend fun initializePhoneData(context: Context, phoneDao: PhoneDao) {
            withContext(Dispatchers.IO) {
                try {
                    // 检查数据库是否已经有数据
                    val existingCount = phoneDao.getPhoneCount()
                    if (existingCount > 0) {
                        // 数据库已有数据，跳过初始化
                        return@withContext
                    }
                    
                    // 从Excel文件读取手机数据
                    val phoneList = ExcelReader.readPhonesFromExcel(context, "手机库.xlsx")
                    
                    if (phoneList.isNotEmpty()) {
                        // 批量插入数据
                        phoneDao.insertPhones(phoneList)
                        println("成功导入 ${phoneList.size} 条手机数据")
                    } else {
                        println("Excel文件中没有找到有效的手机数据")
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("初始化手机数据失败: ${e.message}")
                }
            }
        }
        
        /**
         * 重新初始化数据库
         * 清空现有数据并重新导入
         */
        suspend fun reinitializePhoneData(context: Context, phoneDao: PhoneDao) {
            withContext(Dispatchers.IO) {
                try {
                    // 清空现有数据
                    phoneDao.deleteAllPhones()
                    
                    // 重新导入数据
                    val phoneList = ExcelReader.readPhonesFromExcel(context, "手机库.xlsx")
                    
                    if (phoneList.isNotEmpty()) {
                        phoneDao.insertPhones(phoneList)
                        println("成功重新导入 ${phoneList.size} 条手机数据")
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("重新初始化手机数据失败: ${e.message}")
                }
            }
        }
    }
}