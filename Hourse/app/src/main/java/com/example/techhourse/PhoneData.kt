package com.example.techhourse

import java.io.Serializable

/**
 * 手机数据类，用于Fragment间传递数据
 */
data class PhoneData(
    val id: Int,
    val name: String,
    val price: String,
    val ram: String,
    val rom: String,
    val screenSize: String,
    val screenResolution: String,
    val frontCamera: String,
    val rearCamera: String,
    val sellingPoint: String,
    val imageResourceId: Int
) : Serializable