package com.example.techhourse

import android.Manifest
import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 手机使用情况信息管理器
 * 用于获取各种手机使用统计信息
 */
class PhoneUsageInfoManager(private val context: Context) {

    /**
     * 获取屏幕使用时长（今日）
     * 需要权限：PACKAGE_USAGE_STATS
     */
    fun getScreenUsageTime(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()
                
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                
                var totalTime = 0L
                for (stat in usageStats) {
                    totalTime += stat.totalTimeInForeground
                }
                
                val hours = totalTime / (1000 * 60 * 60)
                val minutes = (totalTime % (1000 * 60 * 60)) / (1000 * 60)
                "${hours}小时${minutes}分钟"
            } else {
                "系统无法版本过低，无法获取"
            }
        } catch (e: Exception) {
            "系统无法，需要授予使用情况访问权限"
        }
    }

    /**
     * 获取充电峰值功率
     * 注意：标准API无法直接获取充电峰值功率，这里提供电池相关信息
     */
    /**
    fun getChargingPeakPower(): String {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            // 获取当前充电功率（如果支持）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val chargingCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val voltage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_VOLTAGE_NOW)

                if (chargingCurrent > 0 && voltage > 0) {
                    val power = (chargingCurrent * voltage) / 1000000.0 // 转换为瓦特
                    String.format("%.2fW (当前)", power)
                } else {
                    "无法获取充电功率信息"
                }
            } else {
                "系统版本不支持"
            }
        } catch (e: Exception) {
            "充电峰值功率：标准API无法获取，需要ROOT权限或厂商API"
        }
    }
    */


    /**
     * 获取充电循环次数
     * 注意：标准Android API无法获取充电循环次数
     */
    /**
    fun getChargingCycles(): String {
        return "充电循环次数：标准API无法获取，需要ROOT权限或厂商特定API"
    }
    */


    /**
     * 获取电池容量
     */
    fun getBatteryCapacity(): String {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                
                if (chargeCounter > 0) {
                    val batteryCapacityMah = chargeCounter / 1000
                    "${batteryCapacityMah}mAh (当前电量: ${capacity}%)"
                } else {
                    "当前电量: ${capacity}%"
                }
            } else {
                "系统无法版本不支持，无法"
            }
        } catch (e: Exception) {
            "系统无法获取电池容量信息"
        }
    }

    /**
     * 获取内存使用情况
     */
    fun getMemoryUsage(): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            val totalMemory = memoryInfo.totalMem / (1024 * 1024) // MB
            val availableMemory = memoryInfo.availMem / (1024 * 1024) // MB
            val usedMemory = totalMemory - availableMemory
            
            "${usedMemory}MB / ${totalMemory}MB (${String.format("%.1f", (usedMemory.toFloat() / totalMemory) * 100)}%)"
        } catch (e: Exception) {
            "系统无法获取内存使用信息"
        }
    }

    /**
     * 获取手机使用时段
     * 基于应用使用统计分析活跃时段
     */
    fun getUsageTimeSlots(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7) // 过去7天
                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()
                
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                
                // 简化分析：基于总使用时间判断活跃时段
                val totalUsage = usageStats.sumOf { it.totalTimeInForeground }
                val avgDailyUsage = totalUsage / 7
                
                when {
                    avgDailyUsage > 8 * 60 * 60 * 1000 -> "重度使用 (全天活跃)"
                    avgDailyUsage > 4 * 60 * 60 * 1000 -> "中度使用 (白天活跃)"
                    else -> "轻度使用 (偶尔使用)"
                }
            } else {
                "系统无法版本不支持"
            }
        } catch (e: Exception) {
            "系统无法，需要使用情况访问权限"
        }
    }

    /**
     * 获取图库存储使用占比
     * 需要权限：READ_EXTERNAL_STORAGE
     */
    fun getGalleryStorageUsage(): String {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                return "系统无法，需要存储读取权限"
            }
            
            val dcimPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val picturesPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            
            var totalImageSize = 0L
            
            // 计算DCIM文件夹大小
            if (dcimPath.exists()) {
                totalImageSize += getFolderSize(dcimPath)
            }
            
            // 计算Pictures文件夹大小
            if (picturesPath.exists()) {
                totalImageSize += getFolderSize(picturesPath)
            }
            
            // 获取总存储空间
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val totalSpace = stat.totalBytes
            
            val imagesSizeMB = totalImageSize / (1024 * 1024)
            val totalSpaceGB = totalSpace / (1024 * 1024 * 1024)
            val percentage = (totalImageSize.toFloat() / totalSpace) * 100
            
            "${imagesSizeMB}MB / ${totalSpaceGB}GB (${String.format("%.2f", percentage)}%)"
        } catch (e: Exception) {
            "系统无法，无法获取图库存储信息: ${e.message}"
        }
    }

    /**
     * 获取低电量告警触发频率
     * 注意：标准API无法直接获取历史低电量告警记录
     */
    /**
    fun getLowBatteryAlertFrequency(): String {
        return "低电量告警频率：标准API无法获取历史记录，需要自定义监听和记录"
    }
    */



    /**
     * 获取日均游戏时间
     * 基于使用统计分析游戏类应用使用时间
     */
    fun getDailyGameTime(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()
                
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                
                // 简化实现：查找包含"game"关键词的应用
                var totalGameTime = 0L
                val packageManager = context.packageManager
                
                for (stat in usageStats) {
                    try {
                        val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString().lowercase()
                        
                        if (appName.contains("game") || appName.contains("游戏") || 
                            stat.packageName.contains("game")) {
                            totalGameTime += stat.totalTimeInForeground
                        }
                    } catch (e: Exception) {
                        // 忽略无法获取信息的应用
                    }
                }
                
                val avgDailyGameTime = totalGameTime / 7
                val hours = avgDailyGameTime / (1000 * 60 * 60)
                val minutes = (avgDailyGameTime % (1000 * 60 * 60)) / (1000 * 60)
                
                "${hours}小时${minutes}分钟/天"
            } else {
                "系统无法版本不支持"
            }
        } catch (e: Exception) {
            "系统无法，需要使用情况访问权限"
        }
    }

    /**
     * 获取夜间拍照统计
     * 通过分析照片EXIF信息统计夜间拍照次数
     */
    fun getNightPhotoCount(): String {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                return "系统无法，需要存储读取权限"
            }
            
            val dcimPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            var nightPhotoCount = 0
            var totalPhotoCount = 0
            
            if (dcimPath.exists()) {
                val result = analyzePhotosInFolder(dcimPath)
                nightPhotoCount = result.first
                totalPhotoCount = result.second
            }
            
            "夜间拍照: ${nightPhotoCount}张 / 总计: ${totalPhotoCount}张"
        } catch (e: Exception) {
            "系统无法分析夜间拍照: ${e.message}"
        }
    }

    /**
     * 获取单手模式启用时间
     * 注意：标准API无法获取单手模式使用统计
     */
    /**
    fun getOneHandModeUsage(): String {
        return "单手模式使用时间：标准API无法获取，需要厂商特定API或系统级权限"
    }
    */

    // 辅助方法：计算文件夹大小
    private fun getFolderSize(folder: File): Long {
        var size = 0L
        try {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    size += if (file.isDirectory) {
                        getFolderSize(file)
                    } else {
                        file.length()
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略无法访问的文件
        }
        return size
    }

    // 辅助方法：分析照片文件夹中的夜间拍照
    private fun analyzePhotosInFolder(folder: File): Pair<Int, Int> {
        var nightCount = 0
        var totalCount = 0
        
        try {
            val files = folder.listFiles { file ->
                file.isFile && (file.name.lowercase().endsWith(".jpg") || 
                               file.name.lowercase().endsWith(".jpeg") ||
                               file.name.lowercase().endsWith(".png"))
            }
            
            if (files != null) {
                for (file in files) {
                    try {
                        val exif = ExifInterface(file.absolutePath)
                        val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
                        
                        if (dateTime != null) {
                            val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                            val date = sdf.parse(dateTime)
                            
                            if (date != null) {
                                val calendar = Calendar.getInstance()
                                calendar.time = date
                                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                
                                // 定义夜间时间为22:00-06:00
                                if (hour >= 22 || hour <= 6) {
                                    nightCount++
                                }
                                totalCount++
                            }
                        }
                    } catch (e: Exception) {
                        // 忽略无法读取EXIF的文件
                    }
                }
                
                // 递归处理子文件夹
                val subFolders = folder.listFiles { file -> file.isDirectory }
                if (subFolders != null) {
                    for (subFolder in subFolders) {
                        val subResult = analyzePhotosInFolder(subFolder)
                        nightCount += subResult.first
                        totalCount += subResult.second
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略无法访问的文件夹
        }
        
        return Pair(nightCount, totalCount)
    }

    /**
     * 获取所有使用情况信息的汇总
     * 使用suspend函数确保耗时操作在IO线程执行
     */
    suspend fun getAllUsageInfo(): Map<String, String> {
        return withContext(Dispatchers.IO) {
            mapOf(
                "屏幕使用时长" to getScreenUsageTime(),
                "电池容量" to getBatteryCapacity(),
                "使用内存" to getMemoryUsage(),
                "手机使用时段" to getUsageTimeSlots(),
                "图库存储使用占比" to getGalleryStorageUsage(),
                "日均游戏时间" to getDailyGameTime(),
                "夜间拍照" to getNightPhotoCount()
            )
        }
    }
}