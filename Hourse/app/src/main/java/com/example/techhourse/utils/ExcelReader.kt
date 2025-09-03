package com.example.techhourse.utils

import android.content.Context
import com.example.techhourse.database.entity.PhoneEntity
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

/**
 * Excel文件读取工具类
 */
class ExcelReader {
    
    companion object {
        /**
         * 从Excel文件读取手机数据
         * @param context 上下文
         * @param fileName Excel文件名（放在assets目录下）
         * @return 手机数据列表
         */
        fun readPhonesFromExcel(context: Context, fileName: String): List<PhoneEntity> {
            val phoneList = mutableListOf<PhoneEntity>()
            
            try {
                val inputStream: InputStream = context.assets.open(fileName)
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0) // 获取第一个工作表
                
                // 跳过第一行（标题行），从第二行开始读取数据
                for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                    val row = sheet.getRow(rowIndex) ?: continue
                    
                    try {
                        // 读取各列数据，确保不为空
                        val phoneModel = getCellValueAsString(row.getCell(0)) ?: continue
                        val brandName = getCellValueAsString(row.getCell(1)) ?: ""
                        val marketName = getCellValueAsString(row.getCell(2)) ?: ""
                        val memoryConfig = getCellValueAsString(row.getCell(3)) ?: ""
                        val frontCamera = getCellValueAsString(row.getCell(4)) ?: ""
                        val rearCamera = getCellValueAsString(row.getCell(5)) ?: ""
                        val resolution = getCellValueAsString(row.getCell(6)) ?: ""
                        val screenSize = getCellValueAsString(row.getCell(7)) ?: ""
                        val sellingPoint = getCellValueAsString(row.getCell(8)) ?: ""
                        val price = getCellValueAsString(row.getCell(9)) ?: ""
                        
                        val phone = PhoneEntity(
                            phoneModel = phoneModel,
                            brandName = brandName,
                            marketName = marketName,
                            memoryConfig = memoryConfig,
                            frontCamera = frontCamera,
                            rearCamera = rearCamera,
                            resolution = resolution,
                            screenSize = screenSize,
                            sellingPoint = sellingPoint,
                            price = price
                        )
                        
                        phoneList.add(phone)
                    } catch (e: Exception) {
                        // 跳过有问题的行，继续处理下一行
                        e.printStackTrace()
                        continue
                    }
                }
                
                workbook.close()
                inputStream.close()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return phoneList
        }
        
        /**
         * 获取单元格值并转换为字符串
         */
        private fun getCellValueAsString(cell: org.apache.poi.ss.usermodel.Cell?): String? {
            return when (cell?.cellType) {
                org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue
                org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue.toString()
                org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
                org.apache.poi.ss.usermodel.CellType.FORMULA -> cell.cellFormula
                else -> null
            }
        }
    }
}