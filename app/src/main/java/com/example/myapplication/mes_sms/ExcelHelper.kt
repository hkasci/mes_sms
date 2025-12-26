package com.example.myapplication.mes_sms

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExcelHelper {
    private const val TAG = "ExcelHelper"
    private const val FOLDER_NAME = "MES_SMS_Exports"

    fun appendResponsesToCsv(context: Context, filenamePrefix: String, rows: List<List<String>>) {
        try {
            val exportsDir = File(context.getExternalFilesDir(null), FOLDER_NAME)
            if (!exportsDir.exists()) exportsDir.mkdirs()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(exportsDir, "${filenamePrefix}_$timestamp.csv")
            FileWriter(file, true).use { fw ->
                rows.forEach { cols ->
                    fw.append(cols.joinToString(",") { escapeCsv(it) })
                    fw.append("\n")
                }
                fw.flush()
            }
            Log.d(TAG, "Wrote CSV to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write CSV: ${e.message}")
        }
    }

    private fun escapeCsv(field: String): String {
        var f = field.replace("\"", "\"\"")
        if (f.contains(",") || f.contains("\n")) {
            f = "\"$f\""
        }
        return f
    }
}

