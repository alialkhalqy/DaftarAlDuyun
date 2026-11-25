package com.daftar.alduyun

import android.os.Bundle
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.platform.LocalContext
import com.daftar.alduyun.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DaftarApp() }
    }
}

class DaftarViewModel(private val dao: DaftarDao) : ViewModel() {
    val transactions = dao.transactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val people = dao.people().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val labels = dao.labels().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(name: String, type: String, amount: Long, cost: Long, note: String,
                       personId: Long? = null, dueDate: Long? = null) {
        viewModelScope.launch {
            dao.addTransaction(TransactionEntity(
                personId = personId, personName = name, type = type,
                amount = amount, cost = cost, title = note, dueDate = dueDate
            ))
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { dao.updateTransaction(transaction) }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { dao.deleteTransaction(id) }
    }

    fun addPerson(name: String, phone: String, type: String, note: String) {
        viewModelScope.launch { dao.addPerson(PersonEntity(name = name, phone = phone, type = type, note = note)) }
    }

    fun rename(key: String, value: String) {
        viewModelScope.launch { dao.saveLabel(LabelEntity(key, value)) }
    }

    fun seedLabels() {
        viewModelScope.launch {
            defaultLabels.forEach { if (dao.getLabel(it.key) == null) dao.saveLabel(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarApp() {
    val context = LocalContext.current
    val dao = remember { DaftarDatabase.get(context).dao() }
    val vm = remember { DaftarViewModel(dao) }
    LaunchedEffect(Unit) { vm.seedLabels() }
    val tx by vm.transactions.collectAsState()
    val people by vm.people.collectAsState()
    val labels by vm.labels.collectAsState()
    var page by remember { mutableStateOf("الرئيسية") }
    var showAdd by remember { mutableStateOf(false) }
    val money = { n: Long -> NumberFormat.getNumberInstance(Locale("en")).format(n) + " ر.ي" }

    val cashSales = tx.filter { it.type == "SALE_CASH" }.sumOf { it.amount }
    val creditSales = tx.filter { it.type == "SALE_CREDIT" }.sumOf { it.amount }
    val customerPayments = tx.filter { it.type == "CUSTOMER_PAYMENT" }.sumOf { it.amount }
    val expenses = tx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val supplierDebt = tx.filter { it.type == "SUPPLIER_DEBT" }.sumOf { it.amount }
    val supplierPayments = tx.filter { it.type == "SUPPLIER_PAYMENT" }.sumOf { it.amount }
    val grossProfit = tx.filter { it.type == "SALE_CASH" || it.type == "SALE_CREDIT" }
        .sumOf { (it.amount - it.cost).coerceAtLeast(0) }
    val cashBalance = cashSales + customerPayments - expenses - supplierPayments

    MaterialTheme(colorScheme = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF16483E),
        secondary = androidx.compose.ui.graphics.Color(0xFFC69B48),
        background = androidx.compose.ui.graphics.Color(0xFFF7F8F5)
    )) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Column {
                        Text("دفتر حسابات", fontWeight = FontWeight.Bold)
                        Text("Accounts Book · إدارة حساباتك بسهولة", style = MaterialTheme.typography.labelSmall)
                    } },
                    actions = { IconButton(onClick = { page = "الإعدادات" }) { Icon(Icons.Default.Settings, null) } }
                )
            },
            bottomBar = {
                NavigationBar {
                    listOf("الرئيسية", "الزبائن", "المعاملات", "التقارير").forEach { item ->
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            icon = { Icon(when(item) {
                                "الرئيسية" -> Icons.Default.Home
                                "الزبائن" -> Icons.Default.People
                                "المعاملات" -> Icons.Default.ReceiptLong
                                else -> Icons.Default.BarChart
                            }, null) },
                            label = { Text(item) }
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة معاملة")
                }
            }
        ) { padding ->
            CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                when(page) {
                    "الرئيسية" -> HomePage(
                        Modifier.padding(padding), money(cashBalance), money(creditSales - customerPayments),
                        money(supplierDebt - supplierPayments), money(grossProfit - expenses), tx.size
                    )
                    "الزبائن" -> PeoplePage(Modifier.padding(padding), people, tx, vm, money)
                    "المعاملات" -> TransactionsPage(Modifier.padding(padding), tx, money, vm::deleteTransaction, vm::updateTransaction)
                    "التقارير" -> ReportsPage(Modifier.padding(padding), cashSales, creditSales, customerPayments, expenses, grossProfit, cashBalance, money)
                    else -> SettingsPage(Modifier.padding(padding), labels, vm)
                }
            }
        }
        if (showAdd) AddTransactionDialog(
            onDismiss = { showAdd = false },
            people = people,
            onSave = { name, type, amount, cost, note, personId, dueDate ->
                vm.addTransaction(name, type, amount, cost, note, personId, dueDate)
                showAdd = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier, cash: String, customerDebt: String, supplierDebt: String, profit: String, count: Int) {
    LazyColumn(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            // بطاقة الهوية البصرية للتطبيق في واجهة المقدمة
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF0F4EF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = "شعار دفتر حسابات",
                                tint = androidx.compose.ui.graphics.Color(0xFFFFD77A),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "دفتر حسابات",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Accounts Book",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            "برمجة علي حسين الخلقي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("رصيد الصندوق التقديري", color = MaterialTheme.colorScheme.onPrimary)
                    Text(cash, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    Text("عدد العمليات المسجلة: $count", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("ديون الزبائن", customerDebt, Modifier.weight(1f))
            MetricCard("ديون الموردين", supplierDebt, Modifier.weight(1f))
        } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("الربح التقديري", profit, Modifier.weight(1f))
            MetricCard("حالة التخزين", "محلي", Modifier.weight(1f))
        } }
        item { Text("ابدأ من زر + لإضافة عملية بيع أو تحصيل أو مصروف أو دين مورد.", style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(14.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    people: List<PersonEntity>,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Long, String, Long?, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("SALE_CASH") }
    var selectedPersonId by remember { mutableStateOf<Long?>(null) }
    var personExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    val types = listOf(
        "SALE_CASH" to "بيع نقدي", "SALE_CREDIT" to "بيع آجل",
        "CUSTOMER_PAYMENT" to "تحصيل من زبون", "SUPPLIER_DEBT" to "دين مورد",
        "SUPPLIER_PAYMENT" to "سداد مورد", "EXPENSE" to "مصروف",
        "INCOME" to "إيراد", "DHIMAR" to "ضمار"
    )
    val relevantPeople = when(type) {
        "SALE_CREDIT", "CUSTOMER_PAYMENT" -> people.filter { it.type == "CUSTOMER" }
        "SUPPLIER_DEBT", "SUPPLIER_PAYMENT" -> people.filter { it.type == "SUPPLIER" }
        else -> people
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة معاملة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(
                        value = types.first { it.first == type }.second,
                        onValueChange = {}, readOnly = true, label = { Text("نوع المعاملة") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        types.forEach { (key, label) -> DropdownMenuItem(text = { Text(label) }, onClick = {
                            type = key; selectedPersonId = null; name = ""; typeExpanded = false
                        }) }
                    }
                }
                if (type in listOf("SALE_CREDIT", "CUSTOMER_PAYMENT", "SUPPLIER_DEBT", "SUPPLIER_PAYMENT")) {
                    ExposedDropdownMenuBox(expanded = personExpanded, onExpandedChange = { personExpanded = !personExpanded }) {
                        OutlinedTextField(
                            value = relevantPeople.firstOrNull { it.id == selectedPersonId }?.name.orEmpty(),
                            onValueChange = {}, readOnly = true,
                            label = { Text(if (type.contains("SUPPLIER")) "اختر المورد" else "اختر الزبون") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = personExpanded, onDismissRequest = { personExpanded = false }) {
                            relevantPeople.forEach { person ->
                                DropdownMenuItem(text = { Text(person.name) }, onClick = {
                                    selectedPersonId = person.id; name = person.name; personExpanded = false
                                })
                            }
                        }
                    }
                    if (relevantPeople.isEmpty()) {
                        Text("أضف الزبون أو المورد أولاً من قسم الزبائن.", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    OutlinedTextField(name, { name = it }, label = { Text("الاسم / الجهة (اختياري)") }, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(amount, { amount = it }, label = { Text("المبلغ بالريال اليمني") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                if (type == "SALE_CASH" || type == "SALE_CREDIT") {
                    OutlinedTextField(cost, { cost = it }, label = { Text("تكلفة البضاعة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
                if (type == "SALE_CREDIT" || type == "SUPPLIER_DEBT") {
                    OutlinedTextField(dueDateText, { dueDateText = it },
                        label = { Text("موعد الاستحقاق YYYY-MM-DD (اختياري)") },
                        modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(note, { note = it }, label = { Text("ملاحظات") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = amount.toLongOrNull() ?: 0
                val parsedDue = try {
                    if (dueDateText.isBlank()) null
                    else java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply { isLenient = false }
                        .parse(dueDateText)?.time
                } catch (_: Exception) { null }
                val needsPerson = type in listOf("SALE_CREDIT", "CUSTOMER_PAYMENT", "SUPPLIER_DEBT", "SUPPLIER_PAYMENT")
                if (a > 0 && (!needsPerson || selectedPersonId != null) &&
                    (dueDateText.isBlank() || parsedDue != null)) {
                    onSave(name, type, a, cost.toLongOrNull() ?: 0, note, selectedPersonId, parsedDue)
                }
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun PeoplePage(
    modifier: Modifier,
    people: List<PersonEntity>,
    transactions: List<TransactionEntity>,
    vm: DaftarViewModel,
    money: (Long) -> String
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("CUSTOMER") }
    var selected by remember { mutableStateOf<PersonEntity?>(null) }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("إدارة الزبائن والموردين", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(name, { name = it }, label = { Text("اسم الشخص أو المورد") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, label = { Text("رقم الهاتف (اختياري)") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(selected = type == "CUSTOMER", onClick = { type = "CUSTOMER" }, label = { Text("زبون") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = type == "SUPPLIER", onClick = { type = "SUPPLIER" }, label = { Text("مورد") })
            Spacer(Modifier.weight(1f))
            Button(onClick = { if (name.isNotBlank()) { vm.addPerson(name.trim(), phone.trim(), type, ""); name = ""; phone = "" } }) { Text("إضافة") }
        }
        HorizontalDivider()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(people) { p ->
                val personTx = transactions.filter { it.personId == p.id }
                val balance = if (p.type == "CUSTOMER") {
                    personTx.filter { it.type == "SALE_CREDIT" }.sumOf { it.amount } -
                        personTx.filter { it.type == "CUSTOMER_PAYMENT" }.sumOf { it.amount }
                } else {
                    personTx.filter { it.type == "SUPPLIER_DEBT" }.sumOf { it.amount } -
                        personTx.filter { it.type == "SUPPLIER_PAYMENT" }.sumOf { it.amount }
                }
                Card(onClick = { selected = p }) {
                    ListItem(
                        headlineContent = { Text(p.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(if (p.type == "CUSTOMER") "زبون • اضغط لعرض كشف الحساب" else "مورد • اضغط لعرض كشف الحساب") },
                        leadingContent = { Icon(Icons.Default.Person, null) },
                        trailingContent = { Text(money(balance)
