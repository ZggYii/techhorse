package com.example.techhourse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class CompareBasicParamsFragment : Fragment() {
    
    private var compareData: CompareData? = null
    
    companion object {
        fun newInstance(compareData: CompareData): CompareBasicParamsFragment {
            val fragment = CompareBasicParamsFragment()
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
        return inflater.inflate(R.layout.fragment_compare_basic_params, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        compareData?.let { data ->
            // 手机型号
            view.findViewById<TextView>(R.id.tv_phone1_model).text = data.phone1Model
            view.findViewById<TextView>(R.id.tv_phone2_model).text = data.phone2Model
            
            // 价格
            view.findViewById<TextView>(R.id.tv_phone1_price).text = data.phone1Price
            view.findViewById<TextView>(R.id.tv_phone2_price).text = data.phone2Price
            
            // 内存配置
            view.findViewById<TextView>(R.id.tv_phone1_memory).text = data.phone1Memory
            view.findViewById<TextView>(R.id.tv_phone2_memory).text = data.phone2Memory
            
            // 屏幕尺寸
            view.findViewById<TextView>(R.id.tv_phone1_screen_size).text = data.phone1ScreenSize ?: "未知"
            view.findViewById<TextView>(R.id.tv_phone2_screen_size).text = data.phone2ScreenSize ?: "未知"
            
            // 屏幕分辨率
            view.findViewById<TextView>(R.id.tv_phone1_screen_resolution).text = data.phone1Resolution ?: "未知"
            view.findViewById<TextView>(R.id.tv_phone2_screen_resolution).text = data.phone2Resolution ?: "未知"
            
            // 前置摄像头
            view.findViewById<TextView>(R.id.tv_phone1_front_camera).text = data.phone1FrontCamera ?: "未知"
            view.findViewById<TextView>(R.id.tv_phone2_front_camera).text = data.phone2FrontCamera ?: "未知"
            
            // 后置摄像头
            view.findViewById<TextView>(R.id.tv_phone1_rear_camera).text = data.phone1RearCamera ?: "未知"
            view.findViewById<TextView>(R.id.tv_phone2_rear_camera).text = data.phone2RearCamera ?: "未知"
            

        }
    }
}