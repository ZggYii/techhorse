package com.example.techhourse

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PhoneDetailPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val phoneData: PhoneData,
    private val comparePhoneData: PhoneData? = null
) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BasicParamsFragment.newInstance(phoneData)
            1 -> ComprehensiveCompareFragment.newInstance(phoneData, comparePhoneData)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}