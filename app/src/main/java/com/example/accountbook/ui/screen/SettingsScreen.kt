package com.example.accountbook.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.viewmodel.ExportState
import com.example.accountbook.viewmodel.ImportState
import com.example.accountbook.viewmodel.MainViewModel
import java.util.Calendar

private val presetColors = listOf(
    0xFFE53935 to "红色",
    0xFFE67E22 to "橙色",
    0xFFF1C40F to "黄色",
    0xFF43A047 to "绿色",
    0xFF00BCD4 to "青色",
    0xFF2196F3 to "蓝色",
    0xFF9C27B0 to "紫色",
    0xFFE91E63 to "粉色",
    0xFF757575 to "灰色",
    0xFFFFFFFF to "白色",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val widgetColor by viewModel.widgetColor.collectAsState()
    val widgetText by viewModel.widgetText.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()

    var textInput by remember { mutableStateOf(widgetText) }
    var exportYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var exportMonth by remember {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    }
    var pendingJsonData by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    // JSON export launcher
    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { u ->
            pendingJsonData?.let { json ->
                try {
                    context.contentResolver.openOutputStream(u)?.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                pendingJsonData = null
                viewModel.resetExportState()
            }
        }
    }

    // PDF export launcher
    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { u ->
            val pdfState = exportState
            if (pdfState is ExportState.PdfReady) {
                try {
                    context.contentResolver.openOutputStream(u)?.use { os ->
                        pdfState.pdfDocument.writeTo(os)
                    }
                    pdfState.pdfDocument.close()
                    Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                viewModel.resetExportState()
            }
        }
    }

    // JSON import launcher
    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { u ->
            try {
                val json = context.contentResolver.openInputStream(u)?.bufferedReader()
                    ?.readText() ?: ""
                viewModel.importFromJson(context, json)
            } catch (e: Exception) {
                Toast.makeText(context, "读取文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle export state changes
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportState.JsonReady -> {
                pendingJsonData = state.jsonData
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, exportYear)
                cal.set(Calendar.MONTH, exportMonth - 1)
                val filename = "AccountBook_${exportYear}_${exportMonth.toString().padStart(2, '0')}.json"
                jsonExportLauncher.launch(filename)
            }
            is ExportState.PdfReady -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, exportYear)
                cal.set(Calendar.MONTH, exportMonth - 1)
                val filename = "AccountBook_${exportYear}_${exportMonth.toString().padStart(2, '0')}.pdf"
                pdfExportLauncher.launch(filename)
            }
            is ExportState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetExportState()
            }
            else -> {}
        }
    }

    // Handle import state changes
    LaunchedEffect(importState) {
        when (val state = importState) {
            is ImportState.Success -> {
                Toast.makeText(context, "成功导入 ${state.count} 笔账单", Toast.LENGTH_SHORT).show()
                viewModel.resetImportState()
            }
            is ImportState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetImportState()
            }
            else -> {}
        }
    }

    // Trigger export/import
    fun doExportJson() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, exportYear)
        cal.set(Calendar.MONTH, exportMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        viewModel.exportAsJson(start, end)
    }

    fun doExportPdf() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, exportYear)
        cal.set(Calendar.MONTH, exportMonth - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis - 1
        viewModel.exportAsPdf(start, end)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget settings section
            item {
                Text(
                    "小组件设置",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            item {
                Text(
                    "小组件颜色",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    presetColors.forEach { (color, _) ->
                        val isSelected = widgetColor.toLong() == color
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (isSelected) Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ) else Modifier
                                )
                                .clickable {
                                    viewModel.setWidgetColor(context, color.toInt())
                                }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("小组件文字") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.setWidgetText(context, textInput)
                            Toast.makeText(context, "文字已更新", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用文字")
                }
            }

            item { HorizontalDivider() }

            // Export section
            item {
                Text(
                    "数据导出",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("导出月份:", fontSize = 14.sp)
                    // Simple year selector
                    val yearRange = (exportYear - 2)..(exportYear + 1)
                    OutlinedButton(onClick = {
                        exportYear = if (exportYear > yearRange.first) exportYear - 1
                        else exportYear
                    }) { Text("<") }
                    Text("${exportYear}年", fontWeight = FontWeight.Medium)
                    OutlinedButton(onClick = {
                        exportYear = if (exportYear < yearRange.last) exportYear + 1
                        else exportYear
                    }) { Text(">") }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = {
                        exportMonth = if (exportMonth > 1) exportMonth - 1 else 12
                    }) { Text("<") }
                    Text("${exportMonth}月", fontWeight = FontWeight.Medium)
                    OutlinedButton(onClick = {
                        exportMonth = if (exportMonth < 12) exportMonth + 1 else 1
                    }) { Text(">") }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { doExportJson() },
                        enabled = exportState !is ExportState.Loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("导出 JSON")
                    }
                    OutlinedButton(
                        onClick = { doExportPdf() },
                        enabled = exportState !is ExportState.Loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("导出 PDF")
                    }
                }
            }

            item { HorizontalDivider() }

            // Import section
            item {
                Text(
                    "数据导入",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        jsonImportLauncher.launch(arrayOf("application/json"))
                    },
                    enabled = importState !is ImportState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("从 JSON 导入")
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
