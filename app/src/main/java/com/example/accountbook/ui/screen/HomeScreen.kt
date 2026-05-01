package com.example.accountbook.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accountbook.data.entity.TransactionWithCategory
import com.example.accountbook.viewmodel.MainViewModel
import com.example.accountbook.viewmodel.TransactionFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthOffset by viewModel.currentMonthOffset.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<TransactionWithCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", fontSize = 28.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month selector and summary
            MonthSelector(
                monthOffset = monthOffset,
                expense = monthlyExpense,
                income = monthlyIncome,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                onReset = { viewModel.resetMonth() }
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = {
                            Text(
                                when (f) {
                                    TransactionFilter.ALL -> "全部"
                                    TransactionFilter.EXPENSE -> "支出"
                                    TransactionFilter.INCOME -> "收入"
                                }
                            )
                        }
                    )
                }
            }

            // Transaction list
            if (transactions.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (monthOffset == 0) "还没有账单，点 + 记一笔吧" else "这个月没有账单",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(transactions, key = { it.transaction.id }) { twc ->
                        TransactionItem(
                            twc = twc,
                            onLongClick = { showDeleteDialog = twc }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { twc ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除账单") },
            text = {
                Text("确定要删除这笔 ${if (twc.transaction.type == "EXPENSE") "支出" else "收入"} 吗？")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(twc.transaction)
                    showDeleteDialog = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun MonthSelector(
    monthOffset: Int,
    expense: Double,
    income: Double,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReset: () -> Unit
) {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, monthOffset)
    val dateFormat = SimpleDateFormat("yyyy年MM月", Locale.CHINA)
    val monthText = dateFormat.format(cal.time)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上月")
            }
            Text(
                text = monthText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onReset() },
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下月")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("支出", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(
                    text = formatAmount(expense),
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("收入", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(
                    text = formatAmount(income),
                    color = Color(0xFF43A047),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("结余", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                val balance = income - expense
                Text(
                    text = formatAmount(balance),
                    color = if (balance >= 0) Color(0xFF43A047) else Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    twc: TransactionWithCategory,
    onLongClick: () -> Unit
) {
    val t = twc.transaction
    val c = twc.category
    val isExpense = t.type == "EXPENSE"

    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLongClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon placeholder
        Column(
            modifier = Modifier
                .size(44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = c.name.first().toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = c.name, fontWeight = FontWeight.Medium)
            if (t.note.isNotEmpty()) {
                Text(
                    text = t.note,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = dateFormat.format(Date(t.date)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${if (isExpense) "-" else "+"}${formatAmount(t.amount)}",
            color = if (isExpense) Color(0xFFE53935) else Color(0xFF43A047),
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

private fun formatAmount(amount: Double): String {
    return String.format("%.2f", amount)
}
