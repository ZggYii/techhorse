package com.example.techhourse

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class CompareComprehensiveFragment : Fragment() {
    
    private var compareData: CompareData? = null
    private lateinit var layoutAiAnalysis: LinearLayout
    private lateinit var progressAiAnalysis: ProgressBar
    private lateinit var tvAiAnalysisResult: TextView
    
    companion object {
        fun newInstance(compareData: CompareData): CompareComprehensiveFragment {
            val fragment = CompareComprehensiveFragment()
            val args = Bundle()
            args.putParcelable("compare_data", compareData)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            compareData = it.getParcelable("compare_data")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_compare_comprehensive, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化AI分析相关视图
        layoutAiAnalysis = view.findViewById(R.id.layout_ai_analysis)
        progressAiAnalysis = view.findViewById(R.id.progress_ai_analysis)
        tvAiAnalysisResult = view.findViewById(R.id.tv_ai_analysis_result)
        
        // 自动开始AI分析
        performAiAnalysis()
    }
    
    fun performAiAnalysis() {
        // 显示AI分析区域
        layoutAiAnalysis.visibility = View.VISIBLE
        progressAiAnalysis.visibility = View.VISIBLE
        tvAiAnalysisResult.visibility = View.GONE
        
        // 使用延迟展示模拟AI生成过程
        compareData?.let { data ->
            val analysisText = generateAiAnalysis(data)
            val lines = analysisText.split("\n")
            
            // 延迟2秒后开始显示结果
            Handler(Looper.getMainLooper()).postDelayed({
                progressAiAnalysis.visibility = View.GONE
                tvAiAnalysisResult.visibility = View.VISIBLE
                
                // 逐行显示文本，模拟打字效果
                var currentText = ""
                var lineIndex = 0
                
                val displayHandler = Handler(Looper.getMainLooper())
                val displayRunnable = object : Runnable {
                    override fun run() {
                        if (lineIndex < lines.size) {
                            currentText += lines[lineIndex] + "\n"
                            tvAiAnalysisResult.text = currentText
                            lineIndex++
                            // 每行间隔300ms显示
                            displayHandler.postDelayed(this, 300)
                        }
                    }
                }
                displayHandler.post(displayRunnable)
            }, 2000)
        }
    }
    
    private fun generateComparisonPrompt(data: CompareData): String {
        return "请对比${data.phone1Model}和${data.phone2Model}这两款手机，分析它们的优缺点并给出购买建议。"
    }
    

    
    private fun generateAiAnalysis(data: CompareData): String {
        return """基于AI智能分析，为您提供以下对比建议：

📊 综合评分：
• ${data.phone1Model}：8.5分
• ${data.phone2Model}：8.2分

📱 性能对比：
• ${data.phone1Model}：处理器性能优秀，适合重度使用
• ${data.phone2Model}：功耗控制出色，续航表现更佳

📸 拍照对比：
• 前置摄像头：${if ((data.phone1FrontCamera?.length ?: 0) > (data.phone2FrontCamera?.length ?: 0)) data.phone1Model else data.phone2Model}略胜一筹
• 后置摄像头：两款产品各有特色，建议根据个人需求选择

💰 性价比分析：
综合考虑价格、性能、功能等因素，${data.phone1Model}在当前价位段具有较高的性价比优势。

🎯 购买建议：
如果您注重性能和拍照体验，推荐选择${data.phone1Model}；如果更看重续航和日常使用体验，${data.phone2Model}是不错的选择。"""
    }
}


//- 价格：${compareData.phone1Price}
//- 内存：${compareData.phone1Memory}
//- 前置摄像头：${compareData.phone1FrontCamera ?: "未知"}
//- 后置摄像头：${compareData.phone1RearCamera ?: "未知"}
//- 屏幕分辨率：${compareData.phone1Resolution ?: "未知"}
//- 屏幕尺寸：${compareData.phone1ScreenSize ?: "未知"}
//- 卖点：${compareData.phone1SellingPoint ?: "未知"}
//
//- 价格：${compareData.phone2Price}
//- 内存：${compareData.phone2Memory}
//- 前置摄像头：${compareData.phone2FrontCamera ?: "未知"}
//- 后置摄像头：${compareData.phone2RearCamera ?: "未知"}
//- 屏幕分辨率：${compareData.phone2Resolution ?: "未知"}
//- 屏幕尺寸：${compareData.phone2ScreenSize ?: "未知"}
//- 卖点：${compareData.phone2SellingPoint ?: "未知"}
//
//请从以下几个方面进行分析：
//1. 性能对比
//2. 拍照能力对比
//3. 屏幕显示效果对比
//4. 性价比分析
//5. 适用人群推荐
//6. 最终购买建议