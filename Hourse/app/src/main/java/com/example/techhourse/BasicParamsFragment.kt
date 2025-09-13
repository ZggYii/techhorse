package com.example.techhourse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class BasicParamsFragment : Fragment() {
    
    private var phoneData: PhoneData? = null
    
    companion object {
        fun newInstance(phoneData: PhoneData): BasicParamsFragment {
            val fragment = BasicParamsFragment()
            val args = Bundle()
            args.putSerializable("phoneData", phoneData)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            phoneData = it.getSerializable("phoneData") as? PhoneData
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_basic_params, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        phoneData?.let { phone ->

            view.findViewById<TextView>(R.id.tv_ram_value).text = phone.ram
            view.findViewById<TextView>(R.id.tv_rom_value).text = phone.rom
            view.findViewById<TextView>(R.id.tv_screen_size_value).text = phone.screenSize
            view.findViewById<TextView>(R.id.tv_front_camera_value).text = phone.frontCamera
            view.findViewById<TextView>(R.id.tv_rear_camera_value).text = phone.rearCamera
        }
    }
}