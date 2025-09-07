package com.example.techhourse.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar

object SnackbarUtils {
    
    /**
     * 显示不遮挡底部导航栏的Snackbar
     * @param activity 当前Activity
     * @param message 要显示的消息
     * @param duration 显示时长
     * @param bottomMargin 底部边距，默认为80dp（底部导航栏高度）
     */
    fun showSnackbar(
        activity: Activity,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        bottomMargin: Int = 80
    ) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(rootView, message, duration)
        
        // 获取Snackbar的视图
        val snackbarView = snackbar.view
        
        // 设置底部边距，避免遮挡底部导航栏
        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = dpToPx(activity, bottomMargin)
        snackbarView.layoutParams = params
        
        snackbar.show()
    }
    
    /**
     * 显示长时间的Snackbar
     */
    fun showLongSnackbar(
        activity: Activity,
        message: String,
        bottomMargin: Int = 80
    ) {
        showSnackbar(activity, message, Snackbar.LENGTH_LONG, bottomMargin)
    }
    
    /**
     * 显示带操作按钮的Snackbar
     */
    fun showActionSnackbar(
        activity: Activity,
        message: String,
        actionText: String,
        action: () -> Unit,
        duration: Int = Snackbar.LENGTH_LONG,
        bottomMargin: Int = 80
    ) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(rootView, message, duration)
        
        // 设置操作按钮
        snackbar.setAction(actionText) { action() }
        
        // 获取Snackbar的视图并设置底部边距
        val snackbarView = snackbar.view
        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = dpToPx(activity, bottomMargin)
        snackbarView.layoutParams = params
        
        snackbar.show()
    }
    
    /**
     * 将dp转换为px
     */
    private fun dpToPx(activity: Activity, dp: Int): Int {
        val density = activity.resources.displayMetrics.density
        return (dp * density).toInt()
    }
    
    /**
     * 为没有底部导航栏的Activity显示普通Snackbar
     */
    fun showNormalSnackbar(
        activity: Activity,
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT
    ) {
        showSnackbar(activity, message, duration, 0)
    }
}