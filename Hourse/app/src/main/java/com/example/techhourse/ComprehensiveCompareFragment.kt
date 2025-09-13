package com.example.techhourse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComprehensiveCompareFragment : Fragment() {
    
    private var phoneData: PhoneData? = null
    private var comparePhoneData: PhoneData? = null
    
    companion object {
        fun newInstance(phoneData: PhoneData, comparePhoneData: PhoneData? = null): ComprehensiveCompareFragment {
            val fragment = ComprehensiveCompareFragment()
            val args = Bundle()
            args.putSerializable("phoneData", phoneData)
            comparePhoneData?.let {
                args.putSerializable("comparePhoneData", it)
            }
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phoneData = it.getSerializable("phoneData") as? PhoneData
            comparePhoneData = it.getSerializable("comparePhoneData") as? PhoneData
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comprehensive_compare, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val aiAnalysisText = view.findViewById<TextView>(R.id.tv_ai_analysis)
        val sellingPointText = view.findViewById<TextView>(R.id.tv_selling_point_value)
        
        // 设置主要卖点
        phoneData?.let { phone ->
            sellingPointText.text = phone.sellingPoint
        }
        
        // 显示加载状态
        aiAnalysisText.text = "AI正在分析中..."
        
        // 模拟AI分析过程
        CoroutineScope(Dispatchers.IO).launch {
            val analysisResult = generateAIAnalysis()
            
            withContext(Dispatchers.Main) {
                aiAnalysisText.text = analysisResult
            }
        }
    }
    
    private suspend fun generateAIAnalysis(): String {
        // 模拟AI分析延迟
        kotlinx.coroutines.delay(2000)
        
        val phone = phoneData ?: return "无法获取手机信息"
        val comparePhone = comparePhoneData
        
        return if (comparePhone != null) {
            // 对比分析
            buildString {
                append("📱 AI智能对比分析\n\n")
                append("🔍 综合评估：\n")
                append("${phone.name} vs ${comparePhone.name}\n\n")
                
                append("💰 价格对比：\n")
                append("${phone.name}：${phone.price}\n")
                append("${comparePhone.name}：${comparePhone.price}\n\n")
                
                append("📊 性能分析：\n")
                append("• 内存配置：${phone.ram}/${phone.rom} vs ${comparePhone.ram}/${comparePhone.rom}\n")
                append("• 摄像头：${phone.rearCamera} vs ${comparePhone.rearCamera}\n")
                append("• 屏幕：${phone.screenSize} vs ${comparePhone.screenSize}\n\n")
                
                append("🎯 推荐建议：\n")
                append("根据配置对比，两款手机各有优势。")
                append("建议根据个人使用需求和预算进行选择。")
            }
        } else {
            // 单机分析
            buildString {
                append("📱 AI智能分析\n\n")
                append("🔍 ${phone.name} 综合评估：\n\n")
                
                append("💰 价格分析：\n")
                append("${phone.price} - 在同类产品中具有竞争力\n\n")
                
                append("📊 配置亮点：\n")
                append("• 内存：${phone.ram}/${phone.rom} - 满足日常使用需求\n")
                append("• 摄像头：${phone.rearCamera} - 拍照效果出色\n")
                append("• 屏幕：${phone.screenSize} - 显示效果清晰\n\n")
                
                append("🎯 核心卖点：\n")
                append(phone.sellingPoint)
                append("\n\n")
                
                append("📈 推荐指数：★★★★☆\n")
                append("适合注重性价比的用户选择。")
            }
        }
    }
}