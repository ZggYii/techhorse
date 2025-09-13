package com.example.techhourse

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompareData(
    val phone1Id: String,
    val phone1Model: String,
    val phone1Price: String,
    val phone1Memory: String,
    val phone1FrontCamera: String?,
    val phone1RearCamera: String?,
    val phone1Resolution: String?,
    val phone1ScreenSize: String?,
    val phone1SellingPoint: String?,
    
    val phone2Id: String,
    val phone2Model: String,
    val phone2Price: String,
    val phone2Memory: String,
    val phone2FrontCamera: String?,
    val phone2RearCamera: String?,
    val phone2Resolution: String?,
    val phone2ScreenSize: String?,
    val phone2SellingPoint: String?
) : Parcelable