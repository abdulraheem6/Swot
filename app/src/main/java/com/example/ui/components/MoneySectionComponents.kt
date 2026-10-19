package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyBudget
import com.example.data.MonthlyPaymentItem
import com.example.data.TransactionItem
import com.example.ui.VaultViewModel
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.UrgentRedBg
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.WarningYellow
import com.example.ui.theme.WarningYellowBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

val EXPENSE_CATEGORIES = listOf(
    "Food & Groceries" to "🛒",
    "Housing & Rent" to "🏠",
    "Utilities" to "⚡",
    "Transportation" to "🚗",
    "Shopping" to "🛍️",
    "Health" to "💊",
    "Entertainment" to "🎬",
    "Education" to "📚",
    "Travel" to "✈️",
    "Other" to "📦"
)

val INCOME_CATEGORIES = listOf(
    "Salary" to "💼",
    "Freelance" to "💻",
    "Investment" to "📈",
    "Business" to "🏢",
    "Rental Income" to "🏡",
    "Bonus" to "🎁",
    "Gift" to "🎉",
    "Other" to "💵"
)

val PAYMENT_METHODS = listOf("Card", "Cash", "UPI/Online", "Bank Transfer")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyFinanceContent(
    viewModel: VaultViewModel,
    onDismiss: () -> Unit = {}
) {
    val theme = LocalAppTheme.current
    val transactions by viewModel.transactions.collectAsState()
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsState()
    val monthlyPayments by viewModel.monthlyPayments.collectAsState()
    val activeMonth by viewModel.activeMoneyMonth.collectAsState()
    val selectedTab by viewModel.moneySelectedTab.collectAsState()
    val searchQuery by viewModel.moneySearchQuery.collectAsState()

    // Calculate month metrics
    val monthTransactions = remember(transactions, activeMonth) {
        transactions.filter { it.date.startsWith(activeMonth) }
    }

    val totalIncome = remember(monthTransactions) {
        monthTransactions.filter { it.type == "income" }.sumOf { it.amount }
    }

    val totalExpenses = remember(monthTransactions) {
        monthTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    }

    val netSavings = totalIncome - totalExpenses

    val currentBudget = remember(monthlyBudgets, activeMonth) {
        monthlyBudgets[activeMonth] ?: MonthlyBudget(month = activeMonth, totalBudget = 3000.0)
    }

    val pendingBillsCount = remember(monthlyPayments, activeMonth) {
        monthlyPayments.count { !it.isPaidForMonth(activeMonth) }
    }

    val pendingBillsAmount = remember(monthlyPayments, activeMonth) {
        monthlyPayments.filter { !it.isPaidForMonth(activeMonth) }.sumOf { it.amount }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .testTag("money_finance_content")
    ) {
        // Month Selector Bar
        MonthSelectorHeader(
            activeMonth = activeMonth,
            onMonthChange = { viewModel.setActiveMoneyMonth(it) },
            onAddExpense = { viewModel.openAddTransactionDialog("expense") },
            onAddIncome = { viewModel.openAddTransactionDialog("income") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Tabs (Dashboard, Expenses, Income, Bills, Budget, Receipts)
        val tabTitles = listOf(
            "Overview",
            "Expenses",
            "Income",
            "Monthly Bills",
            "Budget",
            "Receipts"
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = theme.surface,
            contentColor = theme.primary,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, theme.borderSubtle, RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                val badgeCount = when (index) {
                    1 -> monthTransactions.count { it.type == "expense" }
                    2 -> monthTransactions.count { it.type == "income" }
                    3 -> pendingBillsCount
                    5 -> monthTransactions.count { it.receiptName.isNotBlank() || it.receiptUri != null }
                    else -> 0
                }
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.setMoneySelectedTab(index) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) theme.primary else theme.textMuted
                            )
                            if (badgeCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (index == 3 && badgeCount > 0) UrgentRed else theme.primaryContainer)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (index == 3 && badgeCount > 0) Color.White else theme.primary
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("money_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Tab Content Area
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> MoneyOverviewTab(
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netSavings = netSavings,
                    budget = currentBudget,
                    monthTransactions = monthTransactions,
                    pendingBillsCount = pendingBillsCount,
                    pendingBillsAmount = pendingBillsAmount,
                    viewModel = viewModel
                )
                1 -> ExpensesListTab(
                    expenses = monthTransactions.filter { it.type == "expense" },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setMoneySearchQuery(it) },
                    viewModel = viewModel
                )
                2 -> IncomeListTab(
                    incomes = monthTransactions.filter { it.type == "income" },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setMoneySearchQuery(it) },
                    viewModel = viewModel
                )
                3 -> MonthlyBillsTab(
                    monthlyPayments = monthlyPayments,
                    activeMonth = activeMonth,
                    viewModel = viewModel
                )
                4 -> BudgetPlannerTab(
                    budget = currentBudget,
                    expenses = monthTransactions.filter { it.type == "expense" },
                    viewModel = viewModel
                )
                5 -> ReceiptVaultTab(
                    transactionsWithReceipts = transactions.filter { it.receiptName.isNotBlank() || it.receiptUri != null },
                    viewModel = viewModel
                )
            }
        }
    }
}

// -------------------------------------------------------------
// MONTH SELECTOR & ACTION HEADER
// -------------------------------------------------------------
@Composable
fun MonthSelectorHeader(
    activeMonth: String,
    onMonthChange: (String) -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit
) {
    val theme = LocalAppTheme.current
    val formattedMonthLabel = remember(activeMonth) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            val date = sdf.parse(activeMonth)
            if (date != null) {
                SimpleDateFormat("MMMM yyyy", Locale.US).format(date)
            } else activeMonth
        } catch (_: Exception) {
            activeMonth
        }
    }

    val isCurrentMonth = remember(activeMonth) {
        val nowMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time)
        activeMonth == nowMonth
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SafeGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Money & Finance Vault",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Spends, earnings, budget, bills & receipts",
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAddExpense,
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("money_btn_add_expense")
                ) {
                    Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("- Spend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAddIncome,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("money_btn_add_income")
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Earn", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Month Switcher Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderSubtle, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
                        try {
                            cal.time = sdf.parse(activeMonth) ?: Calendar.getInstance().time
                            cal.add(Calendar.MONTH, -1)
                            onMonthChange(sdf.format(cal.time))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.size(32.dp).testTag("btn_prev_month")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month", tint = theme.textSecondary, modifier = Modifier.size(16.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // Reset to current month on tap
                        val nowMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Calendar.getInstance().time)
                        onMonthChange(nowMonth)
                    }
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = theme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedMonthLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    if (isCurrentMonth) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Current", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                        }
                    }
                }

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
                        try {
                            cal.time = sdf.parse(activeMonth) ?: Calendar.getInstance().time
                            cal.add(Calendar.MONTH, 1)
                            onMonthChange(sdf.format(cal.time))
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.size(32.dp).testTag("btn_next_month")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Month", tint = theme.textSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW & DASHBOARD
// -------------------------------------------------------------
@Composable
fun MoneyOverviewTab(
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    budget: MonthlyBudget,
    monthTransactions: List<TransactionItem>,
    pendingBillsCount: Int,
    pendingBillsAmount: Double,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    val budgetTotal = if (budget.totalBudget <= 0.0) 3000.0 else budget.totalBudget
    val budgetProgress = (totalExpenses / budgetTotal).coerceIn(0.0, 1.0).toFloat()
    val budgetRemaining = (budgetTotal - totalExpenses).coerceAtLeast(0.0)

    val budgetStatusColor = when {
        totalExpenses > budgetTotal -> UrgentRed
        budgetProgress > 0.85f -> WarningYellow
        else -> SafeGreen
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // KPI Cards Grid (2x2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income Card
                KpiMetricCard(
                    title = "TOTAL INCOME",
                    amount = "+$${String.format(Locale.US, "%.2f", totalIncome)}",
                    subtitle = "Earnings this month",
                    icon = Icons.Default.TrendingUp,
                    accentColor = SafeGreen,
                    backgroundColor = SafeGreenBg,
                    modifier = Modifier.weight(1f)
                )

                // Expense Card
                KpiMetricCard(
                    title = "TOTAL SPEND",
                    amount = "-$${String.format(Locale.US, "%.2f", totalExpenses)}",
                    subtitle = "Expenses this month",
                    icon = Icons.Default.TrendingDown,
                    accentColor = UrgentRed,
                    backgroundColor = UrgentRedBg,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Net Savings Card
                KpiMetricCard(
                    title = "NET SAVINGS",
                    amount = (if (netSavings >= 0) "+$" else "-$") + String.format(Locale.US, "%.2f", kotlin.math.abs(netSavings)),
                    subtitle = if (netSavings >= 0) "Surplus balance" else "Deficit this month",
                    icon = Icons.Default.AccountBalance,
                    accentColor = if (netSavings >= 0) Color(0xFF0284C7) else WarningYellow,
                    backgroundColor = if (netSavings >= 0) Color(0x220284C7) else WarningYellowBg,
                    modifier = Modifier.weight(1f)
                )

                // Monthly Bills Pending
                KpiMetricCard(
                    title = "MONTHLY BILLS",
                    amount = "$pendingBillsCount Dues",
                    subtitle = if (pendingBillsCount == 0) "All bills paid ✅" else "$${String.format(Locale.US, "%.0f", pendingBillsAmount)} remaining",
                    icon = Icons.Default.Receipt,
                    accentColor = if (pendingBillsCount == 0) SafeGreen else Color(0xFFF59E0B),
                    backgroundColor = if (pendingBillsCount == 0) SafeGreenBg else Color(0x22F59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Budget Progress Meter Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
                    .testTag("card_budget_overview")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MONTHLY BUDGET HEALTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%.0f", totalExpenses)} spent of $${String.format(Locale.US, "%.0f", budgetTotal)} limit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.textPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(budgetStatusColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when {
                                    totalExpenses > budgetTotal -> "EXCEEDED"
                                    budgetProgress > 0.85f -> "NEAR LIMIT"
                                    else -> "HEALTHY"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = budgetStatusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { budgetProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = budgetStatusColor,
                        trackColor = theme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(budgetProgress * 100).toInt()}% Used",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.textMuted
                        )
                        Text(
                            text = if (totalExpenses > budgetTotal)
                                "$${String.format(Locale.US, "%.0f", totalExpenses - budgetTotal)} Over Limit!"
                            else "$${String.format(Locale.US, "%.0f", budgetRemaining)} Remaining",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalExpenses > budgetTotal) UrgentRed else SafeGreen
                        )
                    }
                }
            }
        }

        // Top Expense Category Breakdown
        item {
            val categoryExpenses = remember(monthTransactions) {
                monthTransactions.filter { it.type == "expense" }
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount } }
                    .toList()
                    .sortedByDescending { it.second }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPENDING BY CATEGORY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${categoryExpenses.size} categories",
                            fontSize = 11.sp,
                            color = theme.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (categoryExpenses.isEmpty()) {
                        Text(
                            text = "No expenses recorded this month yet.",
                            fontSize = 12.sp,
                            color = theme.textMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        categoryExpenses.take(5).forEach { (cat, amount) ->
                            val pct = if (totalExpenses > 0) (amount / totalExpenses).toFloat() else 0f
                            val emoji = EXPENSE_CATEGORIES.find { it.first == cat }?.second ?: "🏷️"

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cat,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.textPrimary
                                        )
                                    }
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", amount)} (${(pct * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(CircleShape),
                                    color = theme.primary,
                                    trackColor = theme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = { viewModel.setMoneySelectedTab(1) }) {
                    Text("View All (${monthTransactions.size})", fontSize = 11.sp, color = theme.primary)
                }
            }
        }

        if (monthTransactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💳", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No transactions logged for this month",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Use '+ Spend' or '+ Earn' above to start tracking.",
                            fontSize = 11.sp,
                            color = theme.textMuted
                        )
                    }
                }
            }
        } else {
            items(monthTransactions.take(6), key = { it.id }) { tx ->
                TransactionRowCard(
                    item = tx,
                    onEdit = { viewModel.openEditTransactionDialog(tx) },
                    onDelete = { viewModel.deleteTransaction(tx.id, tx.title) },
                    onViewReceipt = { viewModel.openReceiptViewer(tx) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// -------------------------------------------------------------
// TAB 1: EXPENSES & SPENDS LIST
// -------------------------------------------------------------
@Composable
fun ExpensesListTab(
    expenses: List<TransactionItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedMethodFilter by remember { mutableStateOf("All") }

    val filteredExpenses = remember(expenses, searchQuery, selectedCategoryFilter, selectedMethodFilter) {
        expenses.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "All" || item.category == selectedCategoryFilter
            val matchesMethod = selectedMethodFilter == "All" || item.paymentMethod == selectedMethodFilter
            matchesSearch && matchesCategory && matchesMethod
        }
    }

    val totalFilteredSpend = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search expenses, merchants, notes...", fontSize = 12.sp, color = theme.textMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = theme.textMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_expenses"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = theme.surface,
                unfocusedContainerColor = theme.surface,
                focusedBorderColor = theme.primary,
                unfocusedBorderColor = theme.borderSubtle
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategoryFilter == "All",
                onClick = { selectedCategoryFilter = "All" },
                label = { Text("All (${expenses.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primaryContainer)
            )

            EXPENSE_CATEGORIES.forEach { (cat, emoji) ->
                val count = expenses.count { it.category == cat }
                if (count > 0 || cat in listOf("Food & Groceries", "Housing & Rent", "Utilities")) {
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) "All" else cat },
                        label = { Text("$emoji $cat ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primaryContainer)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Total Spends Counter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredExpenses.size} EXPENSES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textMuted
            )
            Text(
                text = "Total: -$${String.format(Locale.US, "%.2f", totalFilteredSpend)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UrgentRed
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No expenses match your filters",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.openAddTransactionDialog("expense") },
                        colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ Log New Expense", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { item ->
                    TransactionRowCard(
                        item = item,
                        onEdit = { viewModel.openEditTransactionDialog(item) },
                        onDelete = { viewModel.deleteTransaction(item.id, item.title) },
                        onViewReceipt = { viewModel.openReceiptViewer(item) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: INCOME & EARNINGS LIST
// -------------------------------------------------------------
@Composable
fun IncomeListTab(
    incomes: List<TransactionItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    val totalIncome = remember(incomes) { incomes.sumOf { it.amount } }

    val filteredIncomes = remember(incomes, searchQuery) {
        if (searchQuery.isBlank()) incomes
        else incomes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.notes.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search income, sources, notes...", fontSize = 12.sp, color = theme.textMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(16.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("input_search_income"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = theme.surface,
                unfocusedContainerColor = theme.surface,
                focusedBorderColor = SafeGreen,
                unfocusedBorderColor = theme.borderSubtle
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Total Earnings Summary Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SafeGreenBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SafeGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SafeGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("TOTAL EARNINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                        Text("+$$${String.format(Locale.US, "%.2f", totalIncome)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    }
                }

                Button(
                    onClick = { viewModel.openAddTransactionDialog("income") },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Add Income", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredIncomes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💰", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No earnings recorded this month", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add your salary, freelance or passive income.", fontSize = 11.sp, color = theme.textMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredIncomes, key = { it.id }) { item ->
                    TransactionRowCard(
                        item = item,
                        onEdit = { viewModel.openEditTransactionDialog(item) },
                        onDelete = { viewModel.deleteTransaction(item.id, item.title) },
                        onViewReceipt = {}
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: MONTHLY BILLS & RECURRING PAYMENTS
// -------------------------------------------------------------
@Composable
fun MonthlyBillsTab(
    monthlyPayments: List<MonthlyPaymentItem>,
    activeMonth: String,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    var filterStatus by remember { mutableStateOf("all") } // "all", "due", "paid"

    val totalCommitment = remember(monthlyPayments) { monthlyPayments.sumOf { it.amount } }
    val totalPaid = remember(monthlyPayments, activeMonth) {
        monthlyPayments.filter { it.isPaidForMonth(activeMonth) }.sumOf { it.amount }
    }
    val totalPending = totalCommitment - totalPaid

    val filteredList = remember(monthlyPayments, activeMonth, filterStatus) {
        when (filterStatus) {
            "due" -> monthlyPayments.filter { !it.isPaidForMonth(activeMonth) }
            "paid" -> monthlyPayments.filter { it.isPaidForMonth(activeMonth) }
            else -> monthlyPayments
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Commitment Banner Card
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("MONTHLY RECURRING COMMITMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                        Text("$${String.format(Locale.US, "%.2f", totalCommitment)} / month", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    }

                    Button(
                        onClick = { viewModel.openAddMonthlyPaymentDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_add_monthly_bill")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Bill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Paid This Month", fontSize = 11.sp, color = theme.textMuted)
                        Text("$${String.format(Locale.US, "%.2f", totalPaid)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pending / Due", fontSize = 11.sp, color = theme.textMuted)
                        Text("$${String.format(Locale.US, "%.2f", totalPending)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (totalPending > 0) UrgentRed else SafeGreen)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips (All, Due / Unpaid, Paid)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filterStatus == "all",
                onClick = { filterStatus = "all" },
                label = { Text("All Bills (${monthlyPayments.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterStatus == "due",
                onClick = { filterStatus = "due" },
                label = {
                    val count = monthlyPayments.count { !it.isPaidForMonth(activeMonth) }
                    Text("Due / Pending ($count)", fontSize = 11.sp, color = if (count > 0) UrgentRed else theme.textPrimary)
                }
            )
            FilterChip(
                selected = filterStatus == "paid",
                onClick = { filterStatus = "paid" },
                label = {
                    val count = monthlyPayments.count { it.isPaidForMonth(activeMonth) }
                    Text("Paid ($count)", fontSize = 11.sp, color = SafeGreen)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No bills matching filter", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { bill ->
                    val isPaid = bill.isPaidForMonth(activeMonth)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isPaid) SafeGreen.copy(alpha = 0.3f) else theme.borderSubtle, RoundedCornerShape(14.dp))
                            .testTag("card_bill_${bill.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (bill.category.lowercase(Locale.US)) {
                                        "rent" -> "🏠"
                                        "subscription" -> "📺"
                                        "emi / loan" -> "🚗"
                                        "utility" -> "💡"
                                        "insurance" -> "🛡️"
                                        else -> "💳"
                                    }
                                    Text(icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(bill.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Due on ${bill.dueDay}th", fontSize = 11.sp, color = theme.textMuted)
                                            if (bill.autoPay) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("• AutoPay Active", fontSize = 10.sp, color = SafeGreen, fontWeight = FontWeight.SemiBold)
                                            }
                                            if (bill.alarmEnabled) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("• ⏰ ${bill.alarmTime}", fontSize = 10.sp, color = theme.primary)
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", bill.amount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                            }

                            if (bill.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(bill.notes, fontSize = 11.sp, color = theme.textSecondary)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    IconButton(
                                        onClick = { viewModel.openEditMonthlyPaymentDialog(bill) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = theme.textMuted, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteMonthlyPayment(bill.id, bill.title) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (isPaid) {
                                            viewModel.unmarkMonthlyPaymentPaid(bill.id, activeMonth)
                                        } else {
                                            viewModel.markMonthlyPaymentPaid(bill.id, activeMonth)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPaid) SafeGreenBg else theme.primary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_pay_bill_${bill.id}")
                                ) {
                                    if (isPaid) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PAID ✅ (Tap to undo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                                    } else {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("MARK AS PAID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: BUDGET PLANNER
// -------------------------------------------------------------
@Composable
fun BudgetPlannerTab(
    budget: MonthlyBudget,
    expenses: List<TransactionItem>,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current
    val totalSpent = remember(expenses) { expenses.sumOf { it.amount } }
    val totalBudget = budget.totalBudget
    val totalRemaining = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val totalProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Overall Budget Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.borderSubtle, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("MONTHLY BUDGET TARGET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                            Text("$${String.format(Locale.US, "%.0f", totalBudget)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                        }

                        Button(
                            onClick = { viewModel.openBudgetDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_edit_budget")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Limits", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { totalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = if (totalSpent > totalBudget) UrgentRed else if (totalProgress > 0.85f) WarningYellow else SafeGreen,
                        trackColor = theme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$${String.format(Locale.US, "%.0f", totalSpent)} Spent (${(totalProgress * 100).toInt()}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.textMuted
                        )
                        Text(
                            text = if (totalSpent > totalBudget)
                                "$${String.format(Locale.US, "%.0f", totalSpent - totalBudget)} Over Budget"
                            else "$${String.format(Locale.US, "%.0f", totalRemaining)} Available",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalSpent > totalBudget) UrgentRed else SafeGreen
                        )
                    }
                }
            }
        }

        // Category Budget Allocations
        item {
            Text(
                text = "CATEGORY ALLOCATIONS & LIMITS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primary,
                letterSpacing = 1.sp
            )
        }

        items(EXPENSE_CATEGORIES) { (cat, emoji) ->
            val catLimit = budget.categoryBudgets[cat] ?: 300.0
            val catSpent = remember(expenses, cat) { expenses.filter { it.category == cat }.sumOf { it.amount } }
            val catProgress = if (catLimit > 0) (catSpent / catLimit).toFloat().coerceIn(0f, 1f) else 0f
            val isOver = catSpent > catLimit

            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isOver) UrgentRed.copy(alpha = 0.4f) else theme.borderSubtle, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                        }

                        Text(
                            text = "$${String.format(Locale.US, "%.0f", catSpent)} / $${String.format(Locale.US, "%.0f", catLimit)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) UrgentRed else theme.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { catProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (isOver) UrgentRed else if (catProgress > 0.85f) WarningYellow else theme.primary,
                        trackColor = theme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(catProgress * 100).toInt()}% utilized",
                            fontSize = 10.sp,
                            color = theme.textMuted
                        )
                        Text(
                            text = if (isOver) "$${String.format(Locale.US, "%.0f", catSpent - catLimit)} Over"
                            else "$${String.format(Locale.US, "%.0f", catLimit - catSpent)} remaining",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) UrgentRed else SafeGreen
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// -------------------------------------------------------------
// TAB 5: RECEIPT VAULT
// -------------------------------------------------------------
@Composable
fun ReceiptVaultTab(
    transactionsWithReceipts: List<TransactionItem>,
    viewModel: VaultViewModel
) {
    val theme = LocalAppTheme.current

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(theme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = theme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("RECEIPT VAULT & PROOFS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                        Text("${transactionsWithReceipts.size} receipts stored", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    }
                }

                Button(
                    onClick = { viewModel.openAddTransactionDialog("expense") },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Add Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactionsWithReceipts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧾", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No receipts attached yet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Attach photos or files when adding an expense.", fontSize = 11.sp, color = theme.textMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactionsWithReceipts, key = { it.id }) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp))
                            .clickable { viewModel.openReceiptViewer(item) }
                            .testTag("receipt_card_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(theme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🧾", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                    Text(
                                        text = "${item.date} • ${item.category}",
                                        fontSize = 11.sp,
                                        color = theme.textMuted
                                    )
                                    if (item.isTaxDeductible) {
                                        Text("🛡️ Tax Deductible", fontSize = 10.sp, color = SafeGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${String.format(Locale.US, "%.2f", item.amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UrgentRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "View Proof →",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primary
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: KPI METRIC CARD
// -------------------------------------------------------------
@Composable
fun KpiMetricCard(
    title: String,
    amount: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textMuted, letterSpacing = 0.8.sp)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(amount, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = theme.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: TRANSACTION ROW CARD
// -------------------------------------------------------------
@Composable
fun TransactionRowCard(
    item: TransactionItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val theme = LocalAppTheme.current
    val isExpense = item.type == "expense"
    val emoji = if (isExpense) {
        EXPENSE_CATEGORIES.find { it.first == item.category }?.second ?: "💸"
    } else {
        INCOME_CATEGORIES.find { it.first == item.category }?.second ?: "💰"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp))
            .clickable { onEdit() }
            .testTag("tx_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isExpense) UrgentRedBg else SafeGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.date, fontSize = 11.sp, color = theme.textMuted)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("• ${item.paymentMethod}", fontSize = 11.sp, color = theme.textMuted)
                    }

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.receiptName.isNotBlank() || item.receiptUri != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(theme.primaryContainer)
                                    .clickable { onViewReceipt() }
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("🧾 Receipt", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                            }
                        }
                        if (item.isTaxDeductible) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SafeGreenBg)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("🛡️ Tax Deductible", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isExpense) "-$" else "+$") + String.format(Locale.US, "%.2f", item.amount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) UrgentRed else SafeGreen
                )

                Row(modifier = Modifier.padding(top = 2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = theme.textMuted, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG: ADD OR EDIT TRANSACTION
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialItem: TransactionItem? = null,
    defaultType: String = "expense",
    onDismiss: () -> Unit,
    onSave: (TransactionItem) -> Unit
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current

    var type by remember { mutableStateOf(initialItem?.type ?: defaultType) }
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var amountStr by remember { mutableStateOf(initialItem?.amount?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: if (type == "expense") "Food & Groceries" else "Salary") }
    var date by remember {
        mutableStateOf(
            initialItem?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        )
    }
    var paymentMethod by remember { mutableStateOf(initialItem?.paymentMethod ?: "Card") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var attachedReceiptUri by remember { mutableStateOf(initialItem?.receiptUri) }
    var attachedReceiptName by remember { mutableStateOf(initialItem?.receiptName ?: "") }
    var isTaxDeductible by remember { mutableStateOf(initialItem?.isTaxDeductible ?: false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedReceiptUri = uri.toString()
            attachedReceiptName = uri.lastPathSegment ?: "receipt_proof.jpg"
        }
    }

    val docReceiptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedReceiptUri = uri.toString()
            attachedReceiptName = uri.lastPathSegment ?: "receipt_document.pdf"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.background,
        modifier = Modifier.testTag("sheet_add_transaction")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialItem != null) "Edit Transaction" else "Record New Transaction",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Type Toggle: Expense vs Income
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surface)
                    .border(1.dp, theme.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (type == "expense") UrgentRed else Color.Transparent)
                        .clickable {
                            type = "expense"
                            if (category !in EXPENSE_CATEGORIES.map { it.first }) {
                                category = "Food & Groceries"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💸 Expense (Spend)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "expense") Color.White else theme.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (type == "income") SafeGreen else Color.Transparent)
                        .clickable {
                            type = "income"
                            if (category !in INCOME_CATEGORIES.map { it.first }) {
                                category = "Salary"
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💰 Income (Earning)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "income") Color.White else theme.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Input
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount ($)") },
                placeholder = { Text("0.00") },
                leadingIcon = { Text("$", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = theme.primary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_tx_amount")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Title / Merchant Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (type == "expense") "Merchant / Title" else "Source / Employer") },
                placeholder = { Text(if (type == "expense") "e.g., Whole Foods, Shell Gas, Netflix" else "e.g., Monthly Tech Salary, UI Contract") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_tx_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            Spacer(modifier = Modifier.height(6.dp))

            val currentCategoryList = if (type == "expense") EXPENSE_CATEGORIES else INCOME_CATEGORIES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentCategoryList.forEach { (cat, emoji) ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text("$emoji $cat", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primaryContainer)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date Picker (Native DatePickerDialog)
            OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Transaction Date") },
                trailingIcon = {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            cal.time = sdf.parse(date) ?: Calendar.getInstance().time
                        } catch (_: Exception) {}
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = theme.primary)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d -> date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d) },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment / Deposit Method
            Text("Payment / Deposit Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PAYMENT_METHODS.forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attach Receipt Photo Section
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, theme.borderSubtle, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bill / Receipt Attachment", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                        Text(
                            text = if (attachedReceiptName.isNotBlank()) "Attached: $attachedReceiptName" else "Upload photo or file proof",
                            fontSize = 11.sp,
                            color = if (attachedReceiptName.isNotBlank()) SafeGreen else theme.textMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    docReceiptPickerLauncher.launch(arrayOf("*/*"))
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📄 Doc/PDF", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Photo", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tax Deductible Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isTaxDeductible = !isTaxDeductible }
            ) {
                Checkbox(
                    checked = isTaxDeductible,
                    onCheckedChange = { isTaxDeductible = it },
                    colors = CheckboxDefaults.colors(checkedColor = SafeGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Tax Deductible Expense", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    Text("Flags this record for tax return export & audits", fontSize = 11.sp, color = theme.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Memo (Optional)") },
                placeholder = { Text("Item breakdown, warranty tags, project codes...") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Save & Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0
                        val finalTitle = title.ifBlank { "Untitled $type" }
                        val tx = TransactionItem(
                            id = initialItem?.id ?: java.util.UUID.randomUUID().toString(),
                            type = type,
                            title = finalTitle,
                            amount = parsedAmount,
                            category = category,
                            date = date,
                            paymentMethod = paymentMethod,
                            notes = notes,
                            receiptUri = attachedReceiptUri,
                            receiptName = attachedReceiptName,
                            isTaxDeductible = isTaxDeductible
                        )
                        onSave(tx)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (type == "expense") UrgentRed else SafeGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_transaction")
                ) {
                    Text("Save Record", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// DIALOG: SET MONTHLY BUDGET
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    currentBudget: MonthlyBudget,
    onDismiss: () -> Unit,
    onSave: (MonthlyBudget) -> Unit
) {
    val theme = LocalAppTheme.current
    var totalBudgetStr by remember { mutableStateOf(String.format(Locale.US, "%.0f", currentBudget.totalBudget)) }
    val categoryBudgetsState = remember {
        val map = currentBudget.categoryBudgets.toMutableMap()
        EXPENSE_CATEGORIES.forEach { (cat, _) ->
            if (!map.containsKey(cat)) map[cat] = 300.0
        }
        mutableStateOf(map)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Monthly Budget Limits", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Configure overall spend limit and per-category monthly allocations for ${currentBudget.month}.",
                    fontSize = 11.sp,
                    color = theme.textMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = totalBudgetStr,
                    onValueChange = { totalBudgetStr = it },
                    label = { Text("Overall Monthly Budget ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_total_budget")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Category Limits", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                EXPENSE_CATEGORIES.take(6).forEach { (cat, emoji) ->
                    var catVal by remember { mutableStateOf(String.format(Locale.US, "%.0f", categoryBudgetsState.value[cat] ?: 300.0)) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$emoji $cat", fontSize = 12.sp, color = theme.textPrimary, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = catVal,
                            onValueChange = {
                                catVal = it
                                val num = it.toDoubleOrNull() ?: 0.0
                                categoryBudgetsState.value = categoryBudgetsState.value.toMutableMap().apply { put(cat, num) }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(90.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tot = totalBudgetStr.toDoubleOrNull() ?: 3000.0
                    val updated = currentBudget.copy(
                        totalBudget = tot,
                        categoryBudgets = categoryBudgetsState.value
                    )
                    onSave(updated)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// DIALOG: ADD OR EDIT MONTHLY RECURRING BILL
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMonthlyPaymentDialog(
    initialItem: MonthlyPaymentItem? = null,
    onDismiss: () -> Unit,
    onSave: (MonthlyPaymentItem) -> Unit
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current

    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var amountStr by remember { mutableStateOf(initialItem?.amount?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
    var dueDayStr by remember { mutableStateOf(initialItem?.dueDay?.toString() ?: "1") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Subscription") }
    var autoPay by remember { mutableStateOf(initialItem?.autoPay ?: false) }
    var alarmEnabled by remember { mutableStateOf(initialItem?.alarmEnabled ?: true) }
    var alarmTime by remember { mutableStateOf(initialItem?.alarmTime ?: "09:00") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    val billCategories = listOf("Rent", "Subscription", "EMI / Loan", "Utility", "Insurance", "Education", "Other")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = theme.background,
        modifier = Modifier.testTag("sheet_add_bill")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialItem != null) "Edit Recurring Bill" else "Add Monthly Bill / Dues",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Bill / Subscription Name") },
                placeholder = { Text("e.g. Car Loan EMI, Netflix, Rent, Wifi Fiber") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("input_bill_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Monthly Amount ($)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("input_bill_amount")
                )

                OutlinedTextField(
                    value = dueDayStr,
                    onValueChange = { dueDayStr = it },
                    label = { Text("Due Day (1-31)") },
                    placeholder = { Text("15") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(130.dp).testTag("input_bill_dueday")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                billCategories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AutoPay Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { autoPay = !autoPay },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AutoPay Activated", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    Text("Direct debit / automated card charge", fontSize = 11.sp, color = theme.textMuted)
                }
                Switch(
                    checked = autoPay,
                    onCheckedChange = { autoPay = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = SafeGreen)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alarm Reminder Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { alarmEnabled = !alarmEnabled },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Loud Due Date Alarm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    Text("Fires loud sound reminder on the due day", fontSize = 11.sp, color = theme.textMuted)
                }
                Switch(
                    checked = alarmEnabled,
                    onCheckedChange = { alarmEnabled = it }
                )
            }

            if (alarmEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = alarmTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Alarm Time") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val parts = alarmTime.split(":")
                            val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            TimePickerDialog(context, { _, hour, min ->
                                alarmTime = String.format(Locale.US, "%02d:%02d", hour, min)
                            }, h, m, true).show()
                        }) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Pick Time", tint = theme.primary)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Account Details") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val dDay = (dueDayStr.toIntOrNull() ?: 1).coerceIn(1, 31)
                    val bill = MonthlyPaymentItem(
                        id = initialItem?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title.ifBlank { "Monthly Bill" },
                        amount = amt,
                        dueDay = dDay,
                        category = category,
                        autoPay = autoPay,
                        notes = notes,
                        alarmEnabled = alarmEnabled,
                        alarmTime = alarmTime,
                        paidMonths = initialItem?.paidMonths ?: emptyList()
                    )
                    onSave(bill)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_monthly_bill")
            ) {
                Text("Save Recurring Bill", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// DIALOG: RECEIPT VIEWER
// -------------------------------------------------------------
@Composable
fun ReceiptViewerDialog(
    item: TransactionItem,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Receipt Proof", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surfaceVariant)
                        .border(1.dp, theme.borderSubtle, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.receiptName.ifBlank { "Receipt Proof Document" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        if (item.receiptUri != null) {
                            Text(
                                text = "Verified Vault Attachment",
                                fontSize = 10.sp,
                                color = SafeGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, theme.borderSubtle, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Merchant / Item", fontSize = 11.sp, color = theme.textMuted)
                            Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", fontSize = 11.sp, color = theme.textMuted)
                            Text("$${String.format(Locale.US, "%.2f", item.amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UrgentRed)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date", fontSize = 11.sp, color = theme.textMuted)
                            Text(item.date, fontSize = 12.sp, color = theme.textPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method", fontSize = 11.sp, color = theme.textMuted)
                            Text(item.paymentMethod, fontSize = 12.sp, color = theme.textPrimary)
                        }
                        if (item.isTaxDeductible) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tax Status", fontSize = 11.sp, color = theme.textMuted)
                                Text("✅ Eligible Tax Deduction", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                            }
                        }
                        if (item.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Notes: ${item.notes}", fontSize = 11.sp, color = theme.textSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareText = "Receipt: ${item.title} - $${String.format(Locale.US, "%.2f", item.amount)} on ${item.date} (${item.category})"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt Details"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Details", fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
