package com.example.techhourse.utils

import com.example.techhourse.database.dao.UserBehaviorDao
import com.example.techhourse.database.dao.PhoneDao
import com.example.techhourse.database.entity.UserBehaviorEntity
import com.example.techhourse.database.entity.PhoneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 系统提示词生成器
 * 根据用户行为数据动态生成个性化的系统提示词
 */
class SystemPromptGenerator {
    
    companion object {
        /**
         * 根据用户行为数据和手机库数据生成系统提示词
         * @param userBehaviorDao 用户行为数据访问对象
         * @param phoneDao 手机库数据访问对象
         * @return 生成的系统提示词
         */
        suspend fun generateSystemPrompt(userBehaviorDao: UserBehaviorDao, phoneDao: PhoneDao): String {
            return withContext(Dispatchers.IO) {
                try {
                    // 获取最新的用户行为数据
                    val latestBehavior = userBehaviorDao.getLatestUserBehavior()
                    
                    // 获取手机库数据
                    val phoneLibrary = phoneDao.getAllPhones()
                    
                    if (latestBehavior != null) {
                        buildPersonalizedPrompt(latestBehavior, phoneLibrary)
                    } else {
                        // 如果没有用户行为数据，返回包含手机库信息的默认提示词
                        getDefaultSystemPromptWithPhoneLibrary(phoneLibrary)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 发生错误时返回默认提示词
                    getDefaultSystemPrompt()
                }
            }
        }
        
        /**
         * 根据用户行为数据和手机库数据构建个性化提示词
         */
        private fun buildPersonalizedPrompt(behavior: UserBehaviorEntity, phoneLibrary: List<PhoneEntity>): String {
            val promptBuilder = StringBuilder()
            
            // 基础角色定义
            promptBuilder.append("你是一个智能手机推荐助手，专门根据用户的使用习惯和需求推荐最适合的手机产品。")
            promptBuilder.append("\n\n根据用户的行为数据分析：\n")
            
            // 解析并添加屏幕使用时长信息
            parseScreenUsageTime(behavior.screenUsageTime, promptBuilder)
            
            // 解析并添加电池容量信息
            parseBatteryCapacity(behavior.batteryCapacity, promptBuilder)
            
            // 解析并添加内存使用信息
            parseMemoryUsage(behavior.memoryUsage, promptBuilder)
            
            // 解析并添加使用时段信息
            parsePhoneUsagePeriod(behavior.phoneUsagePeriod, promptBuilder)
            
            // 解析并添加存储使用信息
            parseGalleryStorageRatio(behavior.galleryStorageRatio, promptBuilder)
            
            // 解析并添加游戏时间信息
            parseDailyGameTime(behavior.dailyGameTime, promptBuilder)
            
            // 解析并添加夜间拍照信息
            parseNightPhotography(behavior.nightPhotography, promptBuilder)
            
            // 添加手机库信息
            addPhoneLibraryInfo(phoneLibrary, promptBuilder)
            
            // 添加推荐指导原则
            promptBuilder.append("\n\n请基于以上用户行为特征和可用手机库，在回答手机相关问题时：")
            promptBuilder.append("\n1. 优先推荐符合用户使用习惯的手机型号")
            promptBuilder.append("\n2. 重点关注用户最关心的功能特性")
            promptBuilder.append("\n3. 从手机库中选择最匹配的产品进行推荐")
            promptBuilder.append("\n3. 提供专业、个性化的建议")
            promptBuilder.append("\n4. 保持友善、耐心的服务态度")
            
            return promptBuilder.toString()
        }
        
        /**
         * 解析屏幕使用时长信息
         */
        private fun parseScreenUsageTime(screenUsageTime: String, promptBuilder: StringBuilder) {
            promptBuilder.append("- 屏幕使用习惯：$screenUsageTime")
            
            when {
                screenUsageTime.contains("游戏", ignoreCase = true) -> {
                    promptBuilder.append(" (用户偏好游戏应用，建议关注手机的游戏性能和散热能力)")
                }
                screenUsageTime.contains("社交", ignoreCase = true) -> {
                    promptBuilder.append(" (用户偏好社交应用，建议关注手机的拍照功能和续航能力)")
                }
                screenUsageTime.contains("视频", ignoreCase = true) -> {
                    promptBuilder.append(" (用户偏好视频应用，建议关注手机的屏幕质量和续航能力)")
                }
            }
            promptBuilder.append("\n")
        }
        
        /**
         * 解析电池容量信息
         */
        private fun parseBatteryCapacity(batteryCapacity: String, promptBuilder: StringBuilder) {
            promptBuilder.append("- 电池状态：$batteryCapacity")
            
            // 尝试提取电量百分比
            val percentageRegex = "(\\d+)%".toRegex()
            val matchResult = percentageRegex.find(batteryCapacity)
            
            if (matchResult != null) {
                val percentage = matchResult.groupValues[1].toIntOrNull()
                when {
                    percentage != null && percentage < 30 -> {
                        promptBuilder.append(" (电量较低，用户可能需要大容量电池或快充功能)")
                    }
                    percentage != null && percentage > 80 -> {
                        promptBuilder.append(" (电量充足，用户电池管理良好)")
                    }
                }
            }
            promptBuilder.append("\n")
        }
        
        /**
         * 解析内存使用信息
         */
        private fun parseMemoryUsage(memoryUsage: String, promptBuilder: StringBuilder) {
            promptBuilder.append("- 内存使用情况：$memoryUsage")
            
            // 分析内存使用率
            if (memoryUsage.contains("/")) {
                val parts = memoryUsage.split("/")
                if (parts.size == 2) {
                    val used = parts[0].replace("[^\\d.]".toRegex(), "").toDoubleOrNull()
                    val total = parts[1].replace("[^\\d.]".toRegex(), "").toDoubleOrNull()
                    
                    if (used != null && total != null && total > 0) {
                        val usageRatio = used / total
                        when {
                            usageRatio > 0.8 -> {
                                promptBuilder.append(" (内存使用率较高，建议推荐大内存手机)")
                            }
                            usageRatio < 0.5 -> {
                                promptBuilder.append(" (内存使用率适中，当前配置满足需求)")
                            }
                        }
                    }
                }
            }
            promptBuilder.append("\n")
        }
        
        /**
         * 解析手机使用时段信息
         */
        private fun parsePhoneUsagePeriod(phoneUsagePeriod: String, promptBuilder: StringBuilder) {
            promptBuilder.append("- 主要使用时段：$phoneUsagePeriod")
            
            when {
                phoneUsagePeriod.contains("夜间", ignoreCase = true) || phoneUsagePeriod.contains("晚上", ignoreCase = true) -> {
                    promptBuilder.append(" (夜间使用较多，建议关注护眼功能和夜间模式)")
                }
                phoneUsagePeriod.contains("白天", ignoreCase = true) || phoneUsagePeriod.contains("上午", ignoreCase = true) -> {
                    promptBuilder.append(" (白天使用较多，建议关注屏幕亮度和户外可视性)")
                }
            }
            promptBuilder.append("\n")
        }
        
        /**
         * 解析图库存储占比信息
         */
        private fun parseGalleryStorageRatio(galleryStorageRatio: String, promptBuilder: StringBuilder) {
            promptBuilder.append("- 图库存储占比：$galleryStorageRatio")
            
            // 提取百分比数值
            val percentageRegex = "(\\d+(?:\\.\\d+)?)%".toRegex()
            val matchResult = percentageRegex.find(galleryStorageRatio)
            
            if (matchResult != null) {
                val percentage = matchResult.groupValues[1].toDoubleOrNull()
                when {
                    percentage != null && percentage > 70 -> {
                        promptBuilder.append(" (图库占用存储较多，建议推荐大存储容量手机或云存储功能)")
                    }
                    percentage != null && percentage > 50 -> {
                        promptBuilder.append(" (用户较重视拍照存储，建议关注相机功能和存储扩展)")
                    }
                }
            }
            promptBuilder.append("\n")
        }
        
        /**
         * 解析日均游戏时间信息
         */
        private fun parseDailyGameTime(dailyGameTime: String, promptBuilder: StringBuilder) {
            promptBuilder.append("- 日均游戏时间：$dailyGameTime")
            
            // 提取时间数值（小时）
            val timeRegex = "(\\d+(?:\\.\\d+)?)(?:小时|h|H)".toRegex()
            val matchResult = timeRegex.find(dailyGameTime)
            
            if (matchResult != null) {
                val hours = matchResult.groupValues[1].toDoubleOrNull()
                when {
                    hours != null && hours > 3 -> {
                        promptBuilder.append(" (重度游戏用户，建议推荐游戏手机或高性能处理器)")
                    }
                    hours != null && hours > 1 -> {
                        promptBuilder.append(" (中度游戏用户，建议关注处理器性能和散热)")
                    }
                    hours != null && hours < 0.5 -> {
                        promptBuilder.append(" (轻度游戏用户，性能要求不高)")
                    }
                }
            }
            promptBuilder.append("\n")
        }
        
        /**
         * 解析夜间拍照信息
         */
        private fun parseNightPhotography(nightPhotography: String, promptBuilder: StringBuilder) {
            promptBuilder.append("- 夜间拍照需求：$nightPhotography")
            
            when {
                nightPhotography.contains("经常", ignoreCase = true) || nightPhotography.contains("频繁", ignoreCase = true) -> {
                    promptBuilder.append(" (经常夜间拍照，建议推荐夜景拍照功能强的手机)")
                }
                nightPhotography.contains("偶尔", ignoreCase = true) -> {
                    promptBuilder.append(" (偶尔夜间拍照，可关注基础夜景功能)")
                }
                nightPhotography.contains("很少", ignoreCase = true) || nightPhotography.contains("不", ignoreCase = true) -> {
                    promptBuilder.append(" (夜间拍照需求较低)")
                }
            }
            promptBuilder.append("\n")
        }
        
        /**
         * 添加手机库信息到提示词中
         * 将每一行数据都作为一个手机的完整特征展示
         */
        private fun addPhoneLibraryInfo(phoneLibrary: List<PhoneEntity>, promptBuilder: StringBuilder) {
            if (phoneLibrary.isNotEmpty()) {
                promptBuilder.append("\n\n=== 可推荐手机库信息 ===\n")
                promptBuilder.append("当前手机库共有 ${phoneLibrary.size} 款手机可供推荐，详细信息如下：\n\n")
                
                // 展示每一款手机的完整信息
                phoneLibrary.forEachIndexed { index, phone ->
                    promptBuilder.append("${index + 1}. ${phone.phoneModel}\n")
                    promptBuilder.append("   品牌: ${phone.brandName}\n")
                    promptBuilder.append("   市场名: ${phone.marketName}\n")
                    promptBuilder.append("   内存配置: ${phone.memoryConfig}\n")
                    promptBuilder.append("   前摄: ${phone.frontCamera}\n")
                    promptBuilder.append("   后摄: ${phone.rearCamera}\n")
                    promptBuilder.append("   分辨率: ${phone.resolution}\n")
                    promptBuilder.append("   屏幕尺寸: ${phone.screenSize}\n")
                    promptBuilder.append("   主要卖点: ${phone.sellingPoint}\n")
                    promptBuilder.append("   价格: ${phone.price}\n")
                    if (index < phoneLibrary.size - 1) {
                        promptBuilder.append("\n")
                    }
                }
                
                promptBuilder.append("\n\n请基于以上完整的手机库信息为用户提供精准的推荐建议。\n")
            }
        }

        
        /**
         * 获取包含手机库信息的默认系统提示词
         */
        private fun getDefaultSystemPromptWithPhoneLibrary(phoneLibrary: List<PhoneEntity>): String {
            val promptBuilder = StringBuilder()
            
            promptBuilder.append("你是一个专业的智能手机推荐助手，拥有丰富的手机产品知识和推荐经验。")
            
            // 添加手机库信息
            addPhoneLibraryInfo(phoneLibrary, promptBuilder)
            
            promptBuilder.append("\n\n请基于可用的手机库信息，为用户提供：")
            promptBuilder.append("\n1. 专业的手机选购建议")
            promptBuilder.append("\n2. 详细的产品对比分析")
            promptBuilder.append("\n3. 个性化的推荐方案")
            promptBuilder.append("\n4. 友善、耐心的服务态度")
            promptBuilder.append("\n5. 每次都需要推荐至少三款最适合用户需求的手机")

            return promptBuilder.toString()
        }
        
        /**
         * 添加手机库信息到提示词中
         * 将每一行数据都作为一个手机的完整特征展示
         */
        fun addPhoneInfoToCompare(phoneCompare: List<PhoneEntity>): String {
            val promptBuilder = StringBuilder()

            if (phoneCompare.isNotEmpty()) {
                promptBuilder.append("当前共有 ${phoneCompare.size}款手机需要进行比较，详细信息如下：\n\n")

                // 展示每一款手机的完整信息
                phoneCompare.forEachIndexed { index, phone ->
                    promptBuilder.append("${index + 1}. ${phone.phoneModel}\n")
                    promptBuilder.append("   品牌: ${phone.brandName}\n")
                    promptBuilder.append("   市场名: ${phone.marketName}\n")
                    promptBuilder.append("   内存配置: ${phone.memoryConfig}\n")
                    promptBuilder.append("   前摄: ${phone.frontCamera}\n")
                    promptBuilder.append("   后摄: ${phone.rearCamera}\n")
                    promptBuilder.append("   分辨率: ${phone.resolution}\n")
                    promptBuilder.append("   屏幕尺寸: ${phone.screenSize}\n")
                    promptBuilder.append("   主要卖点: ${phone.sellingPoint}\n")
                    promptBuilder.append("   价格: ${phone.price}\n")
                    if (index < phoneCompare.size - 1) {
                        promptBuilder.append("\n")
                    }
                }

                promptBuilder.append("\n\n请基于以上${phoneCompare.size}款手机的特点给出总结，比如各自的特长在哪里，你觉得哪一个更优。\n")
                return promptBuilder.toString()
            }

            return ""
        }
        
        /**
         * 获取默认系统提示词
         */
        private fun getDefaultSystemPrompt(): String {
            return "你是一个友善、专业的AI助手。请用简洁明了的方式回答用户的问题，保持礼貌和耐心。"
        }
    }
}