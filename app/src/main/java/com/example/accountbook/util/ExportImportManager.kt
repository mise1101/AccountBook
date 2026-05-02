package com.example.accountbook.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.accountbook.data.entity.Category
import com.example.accountbook.data.entity.Transaction
import com.example.accountbook.data.entity.TransactionWithCategory
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportImportManager {

    // ---- JSON Export ----
    fun exportToJson(
        transactions: List<TransactionWithCategory>,
        categories: List<Category>,
        monthLabel: String
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())
        root.put("month", monthLabel)

        val catArr = JSONArray()
        for (c in categories) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("icon", c.icon)
            obj.put("type", c.type)
            obj.put("isPredefined", c.isPredefined)
            obj.put("sortOrder", c.sortOrder)
            catArr.put(obj)
        }
        root.put("categories", catArr)

        val txArr = JSONArray()
        val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (twc in transactions) {
            val tx = twc.transaction
            val obj = JSONObject()
            obj.put("amount", tx.amount)
            obj.put("type", tx.type)
            obj.put("categoryName", twc.category.name)
            obj.put("note", tx.note)
            obj.put("date", tx.date)
            obj.put("dateStr", dateFmt.format(Date(tx.date)))
            txArr.put(obj)
        }
        root.put("transactions", txArr)
        return root.toString(2)
    }

    // ---- JSON Import ----
    data class ImportResult(
        val transactionsImported: Int,
        val errors: List<String>
    )

    data class ParsedTransaction(
        val amount: Double,
        val type: String,
        val categoryName: String,
        val note: String,
        val date: Long
    )

    fun parseJson(jsonString: String): List<ParsedTransaction> {
        val root = JSONObject(jsonString)
        val txArr = root.getJSONArray("transactions")
        val result = mutableListOf<ParsedTransaction>()
        for (i in 0 until txArr.length()) {
            val obj = txArr.getJSONObject(i)
            result.add(
                ParsedTransaction(
                    amount = obj.getDouble("amount"),
                    type = obj.getString("type"),
                    categoryName = obj.optString("categoryName", ""),
                    note = obj.optString("note", ""),
                    date = obj.optLong("date", System.currentTimeMillis())
                )
            )
        }
        return result
    }

    // ---- PDF Export ----
    fun exportToPdf(
        transactions: List<TransactionWithCategory>,
        monthLabel: String
    ): PdfDocument {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var currentPage = pdf.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        )
        var canvas: Canvas = currentPage.canvas

        val titlePaint = Paint().apply {
            textSize = 20f; isFakeBoldText = true; isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = 11f; isFakeBoldText = true; isAntiAlias = true
        }
        val cellPaint = Paint().apply {
            textSize = 10f; isAntiAlias = true
        }
        val linePaint = Paint().apply {
            strokeWidth = 0.5f; isAntiAlias = true
        }
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        var y = 40f

        fun newPageIfNeeded(needed: Float) {
            if (y + needed > pageHeight - 40f) {
                pdf.finishPage(currentPage)
                currentPage = pdf.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                )
                canvas = currentPage.canvas
                y = 40f
            }
        }

        canvas.drawText("账单 - $monthLabel", 40f, y, titlePaint)
        y += 30f

        val cols = floatArrayOf(20f, 120f, 220f, 340f, 440f)
        val headers = arrayOf("日期", "分类", "金额", "类型", "备注")
        for ((i, h) in headers.withIndex()) {
            canvas.drawText(h, cols[i], y, headerPaint)
        }
        y += 8f
        canvas.drawLine(20f, y, pageWidth - 20f, y, linePaint)
        y += 12f

        val totalExpense = transactions
            .filter { it.transaction.type == "EXPENSE" }
            .sumOf { it.transaction.amount }
        val totalIncome = transactions
            .filter { it.transaction.type == "INCOME" }
            .sumOf { it.transaction.amount }

        for (twc in transactions) {
            newPageIfNeeded(18f)
            val tx = twc.transaction
            canvas.drawText(
                dateFmt.format(Date(tx.date)), cols[0], y, cellPaint
            )
            canvas.drawText(twc.category.name, cols[1], y, cellPaint)
            val amountStr = String.format("%.2f", tx.amount)
            canvas.drawText(
                if (tx.type == "EXPENSE") "-$amountStr" else "+$amountStr",
                cols[2], y, cellPaint
            )
            canvas.drawText(
                if (tx.type == "EXPENSE") "支出" else "收入", cols[3], y, cellPaint
            )
            canvas.drawText(tx.note, cols[4], y, cellPaint)
            y += 18f
        }

        y += 16f
        newPageIfNeeded(40f)
        canvas.drawLine(20f, y, pageWidth - 20f, y, linePaint)
        y += 18f
        val summaryPaint = Paint().apply {
            textSize = 12f; isFakeBoldText = true; isAntiAlias = true
        }
        canvas.drawText(
            "支出合计: ${String.format("%.2f", totalExpense)}", 20f, y, summaryPaint
        )
        canvas.drawText(
            "收入合计: ${String.format("%.2f", totalIncome)}", 200f, y, summaryPaint
        )
        canvas.drawText(
            "结余: ${String.format("%.2f", totalIncome - totalExpense)}", 380f, y, summaryPaint
        )

        pdf.finishPage(currentPage)
        return pdf
    }
}
