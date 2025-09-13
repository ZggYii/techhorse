package com.example.techhourse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class CompareDifferenceFragment : Fragment() {
    
    private var compareData: CompareData? = null
    
    companion object {
        fun newInstance(compareData: CompareData): CompareDifferenceFragment {
            val fragment = CompareDifferenceFragment()
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
        return inflater.inflate(R.layout.fragment_compare_difference, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        compareData?.let { data ->
            // 价格差异分析
            val priceDiff = analyzePriceDifference(data.phone1Price, data.phone2Price)
            view.findViewById<TextView>(R.id.tv_price_difference).text = priceDiff
            
            // 性能差异分析
            val performanceDiff = analyzePerformanceDifference(data)
            view.findViewById<TextView>(R.id.tv_performance_difference).text = performanceDiff
            
            // 摄像头差异分析
            val cameraDiff = analyzeCameraDifference(data)
            view.findViewById<TextView>(R.id.tv_camera_difference).text = cameraDiff
            
            // 推荐建议
            val recommendation = generateRecommendation(data)
            view.findViewById<TextView>(R.id.tv_recommendation).text = recommendation
        }
    }
    
    private fun analyzePriceDifference(price1: String, price2: String): String {
        return "${price1} vs ${price2}"
    }
    
    private fun analyzePerformanceDifference(data: CompareData): String {
        return "内存配置：${data.phone1Memory} vs ${data.phone2Memory}"
    }
    
    private fun analyzeCameraDifference(data: CompareData): String {
        return "前置摄像头：${data.phone1FrontCamera ?: "未知"} vs ${data.phone2FrontCamera ?: "未知"}\n后置摄像头：${data.phone1RearCamera ?: "未知"} vs ${data.phone2RearCamera ?: "未知"}"
    }
    
    private fun generateRecommendation(data: CompareData): String {
        return "推荐选择：\n• 性价比优选：${data.phone1Model}\n• 拍照体验优选：${data.phone2Model}"
    }
}