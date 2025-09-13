package com.example.techhourse

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CompareFragmentAdapter(
    private val fragmentActivity: FragmentActivity,
    private val compareData: CompareData
) : FragmentStateAdapter(fragmentActivity) {
    
    private val fragments = mutableMapOf<Int, Fragment>()
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> CompareBasicParamsFragment.newInstance(compareData)
            1 -> CompareComprehensiveFragment.newInstance(compareData)
            2 -> CompareDifferenceFragment.newInstance(compareData)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
        fragments[position] = fragment
        return fragment
    }
    
    fun getFragment(position: Int): Fragment? {
        return fragments[position]
    }
}