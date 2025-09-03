package com.example.techhourse.database.dao

import androidx.room.*
import com.example.techhourse.database.entity.PhoneEntity

/**
 * 手机库表数据访问对象
 */
@Dao
interface PhoneDao {
    
    @Query("SELECT * FROM phone_library")
    suspend fun getAllPhones(): List<PhoneEntity>
    
    @Query("SELECT * FROM phone_library WHERE id = :id")
    suspend fun getPhoneById(id: Int): PhoneEntity?
    
    @Query("SELECT * FROM phone_library WHERE brandName = :brandName")
    suspend fun getPhonesByBrand(brandName: String): List<PhoneEntity>
    
    @Query("SELECT * FROM phone_library WHERE phoneModel LIKE '%' || :keyword || '%' OR marketName LIKE '%' || :keyword || '%'")
    suspend fun searchPhones(keyword: String): List<PhoneEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhone(phone: PhoneEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhones(phones: List<PhoneEntity>): List<Long>
    
    @Update
    suspend fun updatePhone(phone: PhoneEntity)
    
    @Delete
    suspend fun deletePhone(phone: PhoneEntity)
    
    @Query("SELECT COUNT(*) FROM phone_library")
    suspend fun getPhoneCount(): Int

    
    @Query("DELETE FROM phone_library")
    suspend fun deleteAllPhones()
}