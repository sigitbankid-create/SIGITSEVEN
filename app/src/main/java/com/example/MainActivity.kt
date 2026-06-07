package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.Employee
import com.example.data.EmployeeRepository
import com.example.ui.ActiveScreen
import com.example.ui.EmployeeViewModel
import com.example.ui.EmployeeViewModelFactory
import com.example.ui.theme.MyApplicationTheme

// Standard HR Corporate Avatar Pastel Colors
val AvatarColors = listOf(
    Color(0xFF3F51B5), // Deep Indigo
    Color(0xFF009688), // Solid Teal
    Color(0xFF4CAF50), // Balanced Green
    Color(0xFFFF9800), // Vibrant Amber
    Color(0xFF9C27B0), // Creative Purple
    Color(0xFFE91E63), // Energetic Pink
    Color(0xFF607D8B), // Slate Blue-Grey
    Color(0xFF03A9F4)  // Refreshing Aqua
)

// List of Standard Departments
val DepartmentsList = listOf(
    "Teknologi Informasi",
    "Human Resources",
    "Keuangan",
    "Pemasaran",
    "Operasional",
    "Administrasi"
)

// List of Statuses
val StatusList = listOf("Tetap", "Kontrak", "Magang")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local DB and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EmployeeRepository(database.employeeDao())

        // Setup ViewModel
        val viewModel: EmployeeViewModel by viewModels {
            EmployeeViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: EmployeeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val filteredEmployees by viewModel.filteredEmployees.collectAsStateWithLifecycle()
    val selectedEmployeeId by viewModel.selectedEmployeeId.collectAsStateWithLifecycle()
    val employeeBeingEdited by viewModel.employeeBeingEdited.collectAsStateWithLifecycle()

    val selectedEmployee = remember(selectedEmployeeId, allEmployees) {
        allEmployees.find { it.id == selectedEmployeeId }
    }

    // Capture system hardware back clicks for seamless navigation flow
    BackHandler(enabled = currentScreen != ActiveScreen.DASHBOARD) {
        when (currentScreen) {
            ActiveScreen.LIST -> viewModel.navigateTo(ActiveScreen.DASHBOARD)
            ActiveScreen.DETAIL -> viewModel.navigateTo(ActiveScreen.LIST)
            ActiveScreen.ADD_EDIT -> {
                if (employeeBeingEdited != null) {
                    viewModel.navigateTo(ActiveScreen.DETAIL)
                } else {
                    viewModel.navigateTo(ActiveScreen.LIST)
                }
            }
            ActiveScreen.DASHBOARD -> { /* Exit */ }
        }
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            ActiveScreen.DASHBOARD -> "Dashboard Kepegawaian"
                            ActiveScreen.LIST -> "Daftar Pegawai"
                            ActiveScreen.DETAIL -> selectedEmployee?.namaLengkap ?: "Biodata Pegawai"
                            ActiveScreen.ADD_EDIT -> if (employeeBeingEdited != null) "Ubah Biodata" else "Tambah Pegawai"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    if (currentScreen != ActiveScreen.DASHBOARD) {
                        IconButton(onClick = {
                            when (currentScreen) {
                                ActiveScreen.LIST -> viewModel.navigateTo(ActiveScreen.DASHBOARD)
                                ActiveScreen.DETAIL -> viewModel.navigateTo(ActiveScreen.LIST)
                                ActiveScreen.ADD_EDIT -> {
                                    if (employeeBeingEdited != null) {
                                        viewModel.navigateTo(ActiveScreen.DETAIL)
                                    } else {
                                        viewModel.navigateTo(ActiveScreen.LIST)
                                    }
                                }
                                else -> viewModel.navigateTo(ActiveScreen.DASHBOARD)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = "Logo Kepegawaian",
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Quick Action Shortcut at TopBar
                    if (currentScreen == ActiveScreen.DASHBOARD || currentScreen == ActiveScreen.LIST) {
                        IconButton(onClick = { viewModel.startAddEmployee() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Pegawai Baru",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentScreen == ActiveScreen.LIST) {
                FloatingActionButton(
                    onClick = { viewModel.startAddEmployee() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Tambah")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Pegawai", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        bottomBar = {
            if (currentScreen == ActiveScreen.DASHBOARD || currentScreen == ActiveScreen.LIST) {
                Column {
                    Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.2f), thickness = 1.dp)
                    NavigationBar(
                        containerColor = Color(0xFFF3EDF7),
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(72.dp)
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == ActiveScreen.DASHBOARD,
                            onClick = { viewModel.navigateTo(ActiveScreen.DASHBOARD) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == ActiveScreen.DASHBOARD) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = "Beranda"
                                )
                            },
                            label = { Text("Beranda", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF21005D),
                                selectedTextColor = Color(0xFF21005D),
                                indicatorColor = Color(0xFFE8DEF8),
                                unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f)
                            )
                        )

                        NavigationBarItem(
                            selected = currentScreen == ActiveScreen.LIST,
                            onClick = { viewModel.navigateTo(ActiveScreen.LIST) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == ActiveScreen.LIST) Icons.Filled.Badge else Icons.Outlined.Badge,
                                    contentDescription = "Data Diri"
                                )
                            },
                            label = { Text("Data Diri", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF21005D),
                                selectedTextColor = Color(0xFF21005D),
                                indicatorColor = Color(0xFFE8DEF8),
                                unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f)
                            )
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                android.widget.Toast.makeText(context, "Sistem Payroll & Gaji sedang disiapkan", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Payments,
                                    contentDescription = "Gaji"
                                )
                            },
                            label = { Text("Gaji", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF21005D),
                                selectedTextColor = Color(0xFF21005D),
                                indicatorColor = Color(0xFFE8DEF8),
                                unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f)
                            )
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = {
                                android.widget.Toast.makeText(context, "Modul Pengaturan akan segera hadir", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Pengaturan"
                                )
                            },
                            label = { Text("Pengaturan", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF21005D),
                                selectedTextColor = Color(0xFF21005D),
                                indicatorColor = Color(0xFFE8DEF8),
                                unselectedIconColor = Color(0xFF49454F).copy(alpha = 0.6f),
                                unselectedTextColor = Color(0xFF49454F).copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Use ambient polished background #F7F2FA
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally(animationSpec = tween(250)) { width -> if (targetState.ordinal > initialState.ordinal) width else -width } togetherWith
                            slideOutHorizontally(animationSpec = tween(250)) { width -> if (targetState.ordinal > initialState.ordinal) -width else width }
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    ActiveScreen.DASHBOARD -> DashboardScreen(viewModel, allEmployees)
                    ActiveScreen.LIST -> ListScreen(viewModel, filteredEmployees)
                    ActiveScreen.DETAIL -> selectedEmployee?.let { DetailScreen(viewModel, it) } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Data pegawai tidak ditemukan.")
                    }
                    ActiveScreen.ADD_EDIT -> AddEditScreen(viewModel, employeeBeingEdited)
                }
            }
        }
    }
}

// ==================== DASHBOARD SCREEN ====================

@Composable
fun DashboardScreen(viewModel: EmployeeViewModel, allEmployees: List<Employee>) {
    val aiInsight by viewModel.dashboardAiInsight.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isDashboardAiLoading.collectAsStateWithLifecycle()

    val totalCount = allEmployees.size
    val tetapCount = allEmployees.count { it.statusPegawai == "Tetap" }
    val kontrakCount = allEmployees.count { it.statusPegawai == "Kontrak" }
    val magangCount = allEmployees.count { it.statusPegawai == "Magang" }

    // Grouping for department stats
    val departmentCounts = remember(allEmployees) {
        allEmployees.groupBy { it.departemen }.mapValues { it.value.size }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Selamat Datang!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sistem Manajemen Kepegawaian dan Biodata Lengkap. Kelola data administrasi staf Anda lebih tertata dan terpusat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Outlined.Dashboard,
                        contentDescription = "Dashboard",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Summary Statistics row
        item {
            Column {
                Text(
                    text = "Statistik Kerja Staf",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1D1B20), // Professional Polish text title color
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Total Pegawai",
                        value = totalCount.toString(),
                        icon = Icons.Default.People,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Pegawai Tetap",
                        value = tetapCount.toString(),
                        icon = Icons.Default.VerifiedUser,
                        color = Color(0xFF10B981), // Emerald
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMetricCard(
                        title = "Kontrak",
                        value = kontrakCount.toString(),
                        icon = Icons.Default.Assignment,
                        color = Color(0xFFF59E0B), // Amber
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Intern / Magang",
                        value = magangCount.toString(),
                        icon = Icons.Default.School,
                        color = Color(0xFF3B82F6), // Blue
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Department Distribution Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Distribusi per Departemen",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (departmentCounts.isEmpty()) {
                        Text(
                            "Belum ada sebaran tim. Tambahkan beberapa data pegawai baru.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        departmentCounts.forEach { (dept, count) ->
                            val rRatio = if (totalCount > 0) count.toFloat() / totalCount else 0f
                            
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dept,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF49454F)
                                    )
                                    Text(
                                        text = "$count Pegawai (${(rRatio * 100).toInt()}%)",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF1D1B20)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { rRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color(0xFFF1F5F9)
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI WORKFORCE INSIGHTS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)), // Soft purple tint
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color(0xFFE9D5FF).copy(alpha = 0.8f)), // Purple borders
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = Color(0xFF8B5CF6) // Beautiful Indigo-Purple AI Icon
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Analisis Kepegawaian",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF6B21A8)
                            )
                        }

                        // Trigger button
                        Button(
                            onClick = { viewModel.generateDashboardInsight() },
                            enabled = !isAiLoading && totalCount > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = if (aiInsight.isEmpty()) "Mulai Analisis" else "Refresh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isAiLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF8B5CF6))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Menganalisis komposisi tim Anda...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else if (aiInsight.isNotEmpty()) {
                        Text(
                            text = aiInsight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A044E),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, Color(0xFFF3E8FF)), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        )
                    } else {
                        Text(
                            text = "Gunakan asisten AI untuk memindai kesehatan operasional departemen Anda beserta penyeimbangan status kerja karyawan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF581C87)
                        )
                    }
                }
            }
        }

        // Action Directives Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(ActiveScreen.LIST) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = "Daftar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kelola Pegawai", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.startAddEmployee() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.PersonAddAlt, contentDescription = "Tambah")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pegawai Baru", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)), // Professional Polish borders
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF49454F).copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1D1B20) // High contrast professional deep text
            )
        }
    }
}

// ==================== LIST SCREEN ====================

@Composable
fun ListScreen(viewModel: EmployeeViewModel, filteredEmployees: List<Employee>) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDept by viewModel.selectedDepartment.collectAsStateWithLifecycle()
    val selectedStat by viewModel.selectedStatus.collectAsStateWithLifecycle()

    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Cari NIP, Nama, Jabatan, NIK...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Bersihkan")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    IconButton(
                        onClick = { showFilters = !showFilters },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (showFilters || selectedDept != "Semua" || selectedStat != "Semua") MaterialTheme.colorScheme.primaryContainer else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Kepegawaian",
                            tint = if (showFilters || selectedDept != "Semua" || selectedStat != "Semua") MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF334155)
                        )
                    }
                }

                // Advanced Filtering Drawer/Section
                AnimatedVisibility(
                    visible = showFilters || selectedDept != "Semua" || selectedStat != "Semua",
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(bottom = 12.dp))
                        
                        // Department selector chip list
                        Text(
                            text = "Filter Departemen:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val allDeptsOpts = listOf("Semua") + DepartmentsList
                            allDeptsOpts.forEach { dept ->
                                FilterChip(
                                    selected = selectedDept == dept,
                                    onClick = { viewModel.selectedDepartment.value = dept },
                                    label = { Text(dept, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status selector chips
                        Text(
                            text = "Status Pegawai:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val allStatsOpts = listOf("Semua") + StatusList
                            allStatsOpts.forEach { stat ->
                                FilterChip(
                                    selected = selectedStat == stat,
                                    onClick = { viewModel.selectedStatus.value = stat },
                                    label = { Text(stat, fontSize = 12.sp) }
                                )
                            }
                        }

                        // Clear Button
                        if (selectedDept != "Semua" || selectedStat != "Semua") {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    viewModel.selectedDepartment.value = "Semua"
                                    viewModel.selectedStatus.value = "Semua"
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Filter", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // List contents
        if (filteredEmployees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Data Pegawai Kosong",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tidak ada kecocokan pencarian atau filter data yang Anda pilih.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEmployees, key = { it.id }) { emp ->
                    EmployeeCardElement(emp = emp, onClick = { viewModel.selectEmployee(emp.id) })
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Extra layout padding for floating action button
                }
            }
        }
    }
}

@Composable
fun EmployeeCardElement(emp: Employee, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)), // Modern sleek borders
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Pastel Initials Monogram
            val avColor = AvatarColors[emp.avatarColorIndex % AvatarColors.size]
            val init = emp.namaLengkap.take(2).uppercase()
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(avColor.copy(alpha = 0.12f), CircleShape)
                    .border(BorderStroke(1.5.dp, avColor.copy(alpha = 0.35f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = init,
                    color = avColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name & Academic Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = emp.namaLengkap,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1D1B20), // High contrast deep text
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (emp.gelar.isNotEmpty()) {
                        Text(
                            text = ", ${emp.gelar}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF49454F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                // NIP Identifier code
                Text(
                    text = "NIP: ${emp.nip}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF49454F).copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Position Department Row
                Text(
                    text = "${emp.jabatan} • ${emp.departemen}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF49454F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Beautiful interactive tag badge showing employment status as pill
            Box(
                modifier = Modifier
                    .clip(CircleShape) // Rounded pill shape
                    .background(
                        when (emp.statusPegawai) {
                            "Tetap" -> Color(0xFFD1FAE5)   // Emerald green tone
                            "Kontrak" -> Color(0xFFFEF3C7) // Amber orange tone
                            else -> Color(0xFFE0F2FE)      // Sky blue tone
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = emp.statusPegawai,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (emp.statusPegawai) {
                        "Tetap" -> Color(0xFF065F46)
                        "Kontrak" -> Color(0xFF92400E)
                        else -> Color(0xFF075985)
                    }
                )
            }
        }
    }
}

// ==================== DETAIL / BIODATA LENGKAP SCREEN ====================

@Composable
fun DetailScreen(viewModel: EmployeeViewModel, emp: Employee) {
    val aiBrief by viewModel.employeeAiAnalysis.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isEmployeeAiLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Dynamic service period calculation base
    val yearsOfService = remember(emp.tanggalMasuk) {
        try {
            val parts = emp.tanggalMasuk.split("-")
            if (parts.isNotEmpty()) {
                val joinYear = parts[0].toIntOrNull() ?: 2024
                val joinMonth = if (parts.size > 1) parts[1].toIntOrNull() ?: 1 else 1
                // Reference Year is 2026, Reference Month is 6 (June)
                val diffYears = 2026 - joinYear
                val diffMonths = 6 - joinMonth
                var finalYears = diffYears
                var finalMonths = diffMonths
                if (diffMonths < 0) {
                    finalYears -= 1
                    finalMonths += 12
                }
                if (finalYears < 0) {
                    "Baru Masuk"
                } else {
                    "${finalYears} Thn ${finalMonths} Bln"
                }
            } else {
                "Baru Masuk"
            }
        } catch(e: Exception) {
            "Baru Masuk"
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Main Display header Card (Redesigned with verified badge & NIP/Status pills)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Monogram large circle avatar representation with verified badge overlap
                        val avColor = AvatarColors[emp.avatarColorIndex % AvatarColors.size]
                        Box(modifier = Modifier.size(96.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(avColor.copy(alpha = 0.12f), CircleShape)
                                    .border(BorderStroke(3.dp, avColor.copy(alpha = 0.4f)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emp.namaLengkap.take(2).uppercase(),
                                    color = avColor,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            // Verified badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .background(Color(0xFF6750A4), CircleShape)
                                    .border(BorderStroke(2.dp, Color.White), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Terverifikasi",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Full academic titled name
                        Text(
                            text = emp.namaLengkap + if (emp.gelar.isNotEmpty()) ", ${emp.gelar}" else "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D1B20),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Position / Dept text
                        Text(
                            text = "${emp.jabatan.uppercase()} • ${emp.departemen.uppercase()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4), // Primary Purple Brand
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )

                        // NIP & STATUS Pills Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFE8DEF8), RoundedCornerShape(16.dp))
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "NIP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F).copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = emp.nip,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF1D192B)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFF3EDF7), RoundedCornerShape(16.dp))
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "STATUS KEPEGAWAIAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F).copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pegawai ${emp.statusPegawai}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D192B)
                                )
                            }
                        }
                    }
                }
            }

            // REDESIGNED 2x2 CATEGORICAL GRID INFO
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 1: Kontak
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFD0BCFF).copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContactMail,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "KONTAK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F).copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = emp.email.ifEmpty { "-" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = emp.noHp.ifEmpty { "-" },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF49454F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card 3: Unit Kerja
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFD0BCFF).copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BusinessCenter,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "UNIT KERJA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F).copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = emp.departemen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = emp.jabatan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF49454F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card 2: Domisili
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFD0BCFF).copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "DOMISILI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F).copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = emp.tempatLahir.ifEmpty { "-" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = emp.alamatLengkap.ifEmpty { "-" },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF49454F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Card 4: Masa Kerja
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFD0BCFF).copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "MASA KERJA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F).copy(alpha = 0.6f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = yearsOfService,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Masuk: ${emp.tanggalMasuk}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF49454F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // PRIMARY DIGITAL DOWNLOAD PORTABLE ACTION BUTTON
            item {
                Button(
                    onClick = {
                        android.widget.Toast.makeText(
                            context,
                            "Dokumen CV Lengkap Sdr(i). ${emp.namaLengkap} berhasil diexport ke PDF!",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = CircleShape,
                     elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unduh CV Lengkap", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // GEMINI AI INDIVIDUAL BIODATA ASSESSMENT CARD (Redesigned with larger rounded corner)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)), // Soft purple assess tint
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, Color(0xFFE9D5FF).copy(alpha = 0.8f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Penilai & Konsultan Karir",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF6B21A8)
                                )
                            }

                            Button(
                                onClick = { viewModel.generateEmployeeAnalysis(emp) },
                                enabled = !isAiLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8B5CF6),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                                    .align(Alignment.CenterVertically),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (aiBrief.isEmpty()) "Analisis Biodata" else "Ulang Analisis",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isAiLoading) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Color(0xFF8B5CF6))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Asisten AI sedang menyusun ulasan potensi...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        } else if (aiBrief.isNotEmpty()) {
                            Text(
                                text = aiBrief,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4A044E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(BorderStroke(1.dp, Color(0xFFF3E8FF)), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            )
                        } else {
                            Text(
                                text = "Minta rekomendasi jalur promosi karir spesifik, audit kompetensi, dan gaya kepemimpinan personal terbaik untuk memotivasi Sdr(i). ${emp.namaLengkap}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF581C87)
                            )
                        }
                    }
                }
            }

            // 1. DATA ADMINISTRASI / KARIR KEPEGAWAIAN (Styled as 26dp rounded)
            item {
                DetailsGroupCard(title = "Data Administrasi & Karir", icon = Icons.Outlined.WorkOutline) {
                    DetailRow(label = "Nomor Induk Pegawai (NIP)", value = emp.nip, icon = Icons.Outlined.Badge)
                    DetailRow(label = "Jabatan Kerja", value = emp.jabatan, icon = Icons.Outlined.Work)
                    DetailRow(label = "Departemen / Divisi", value = emp.departemen, icon = Icons.Outlined.CorporateFare)
                    DetailRow(label = "Pendidikan Terakhir", value = emp.pendidikanTerakhir, icon = Icons.Outlined.School)
                    DetailRow(label = "Tanggal Bergabung", value = emp.tanggalMasuk, icon = Icons.Outlined.CalendarToday)
                }
            }

            // 2. BIODATA PRIBADI LENGKAP (Styled as 26dp rounded)
            item {
                DetailsGroupCard(title = "Biodata Pribadi Lengkap", icon = Icons.Outlined.Person) {
                    DetailRow(label = "Nomor Induk Kependudukan (NIK)", value = emp.nik, icon = Icons.Outlined.HomeMini)
                    DetailRow(label = "Tempat Lahir", value = emp.tempatLahir, icon = Icons.Outlined.LocationCity)
                    DetailRow(label = "Tanggal Lahir", value = emp.tanggalLahir, icon = Icons.Outlined.CalendarMonth)
                    DetailRow(label = "Jenis Kelamin", value = emp.jenisKelamin, icon = Icons.Outlined.Transgender)
                    DetailRow(label = "Agama", value = emp.agama, icon = Icons.Outlined.StarRate)
                    DetailRow(label = "Golongan Darah", value = emp.golonganDarah, icon = Icons.Outlined.Bloodtype)
                    DetailRow(label = "Status Pernikahan", value = emp.statusPernikahan, icon = Icons.Outlined.FavoriteBorder)
                }
            }

            // 3. KONTAK & ALAMAT (Styled as 26dp rounded)
            item {
                DetailsGroupCard(title = "Hubungan & Kontak", icon = Icons.Outlined.ContactMail) {
                    DetailRow(label = "Nomor Handphone (WhatsApp)", value = emp.noHp, icon = Icons.Outlined.Phone)
                    DetailRow(label = "Alamat Email", value = emp.email, icon = Icons.Outlined.Email)
                    DetailRow(label = "Alamat Domisili Lengkap", value = emp.alamatLengkap, icon = Icons.Outlined.Map)
                }
            }

            // 4. CATATAN INTERNAL HR
            if (emp.catatanInternal.isNotEmpty()) {
                item {
                    DetailsGroupCard(title = "Catatan Strategis HRD", icon = Icons.Outlined.Note) {
                        Text(
                            text = emp.catatanInternal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1D1B20),
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }

        // Action Toolbar footer at base
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(), // Clean bottom Android key pill padding
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Button: Delete
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Pegawai", fontWeight = FontWeight.Bold)
                }

                // Primary Button: Edit
                Button(
                    onClick = { viewModel.startEditEmployee(emp) },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ubah Biodata", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Interactive custom delete trigger validation dialog modal
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = MaterialTheme.colorScheme.error) },
            title = { Text("Konfirmasi Hapus Pegawai", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus seluruh data kepegawaian beserta biodata lengkap Sdr(i). ${emp.namaLengkap}? Tindakan ini bersifat permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteEmployee(emp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Permanen", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun DetailsGroupCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifEmpty { "-" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155)
            )
        }
    }
}

// ==================== ADD / EDIT CREATION STATE FORM ====================

@Composable
fun AddEditScreen(viewModel: EmployeeViewModel, empToEdit: Employee?) {
    val context = LocalContext.current

    // Fields mapping
    var nip by remember { mutableStateOf(empToEdit?.nip ?: "") }
    var nik by remember { mutableStateOf(empToEdit?.nik ?: "") }
    var namaLengkap by remember { mutableStateOf(empToEdit?.namaLengkap ?: "") }
    var gelar by remember { mutableStateOf(empToEdit?.gelar ?: "") }
    var email by remember { mutableStateOf(empToEdit?.email ?: "") }
    var noHp by remember { mutableStateOf(empToEdit?.noHp ?: "") }
    var jabatan by remember { mutableStateOf(empToEdit?.jabatan ?: "") }
    var departemen by remember { mutableStateOf(empToEdit?.departemen ?: DepartmentsList.first()) }
    var statusPegawai by remember { mutableStateOf(empToEdit?.statusPegawai ?: StatusList.first()) }
    var tanggalMasuk by remember { mutableStateOf(empToEdit?.tanggalMasuk ?: "2026-06-07") }
    var tempatLahir by remember { mutableStateOf(empToEdit?.tempatLahir ?: "") }
    var tanggalLahir by remember { mutableStateOf(empToEdit?.tanggalLahir ?: "1994-01-01") }
    var jenisKelamin by remember { mutableStateOf(empToEdit?.jenisKelamin ?: "Laki-laki") }
    var agama by remember { mutableStateOf(empToEdit?.agama ?: "Islam") }
    var golonganDarah by remember { mutableStateOf(empToEdit?.golonganDarah ?: "-") }
    var statusPernikahan by remember { mutableStateOf(empToEdit?.statusPernikahan ?: "Lajang") }
    var pendidikanTerakhir by remember { mutableStateOf(empToEdit?.pendidikanTerakhir ?: "S1") }
    var alamatLengkap by remember { mutableStateOf(empToEdit?.alamatLengkap ?: "") }
    var catatanInternal by remember { mutableStateOf(empToEdit?.catatanInternal ?: "") }

    // Validation alerts state
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }

    // Multi-tab sub-selector inside form to keep layout extremely clean, avoiding scrolling fatigue
    var activeFormTab by remember { mutableStateOf(0) } // 0: Jabatan/Karir, 1: Biodata Diri, 2: Kontak & Alamat

    Column(modifier = Modifier.fillMaxSize()) {
        
        // Multi-tab switcher
        TabRow(
            selectedTabIndex = activeFormTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = activeFormTab == 0, onClick = { activeFormTab = 0 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("1. Karir", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Tab(selected = activeFormTab == 1, onClick = { activeFormTab = 1 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("2. Biodata", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Tab(selected = activeFormTab == 2, onClick = { activeFormTab = 2 }) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text("3. Kontak & Alamat", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Tab 1: DATA KARIR / PEKERJAAN
            if (activeFormTab == 0) {
                item {
                    Text(
                        text = "Informasi Kedudukan & Karir",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1E293B)
                    )
                }

                item {
                    FormInputRow(
                        label = "Nomor Induk Pegawai (NIP)*",
                        value = nip,
                        onValueChange = { nip = it },
                        placeholder = "e.g. PEG-199512-045",
                        infoHint = "Kode registrasi unik kepegawaian perusahaan."
                    )
                }

                item {
                    FormInputRow(
                        label = "Jabatan Kerja*",
                        value = jabatan,
                        onValueChange = { jabatan = it },
                        placeholder = "e.g. Senior Software Engineer"
                    )
                }

                // Custom Dropdown Selection for Departemen
                item {
                    FormSpinnerSelector(
                        label = "Departemen / Divisi",
                        options = DepartmentsList,
                        selectedOption = departemen,
                        onOptionSelected = { departemen = it }
                    )
                }

                // Custom Dropdown Selection for Status
                item {
                    FormSpinnerSelector(
                        label = "Status Pekerjaan",
                        options = StatusList,
                        selectedOption = statusPegawai,
                        onOptionSelected = { statusPegawai = it }
                    )
                }

                item {
                    FormSpinnerSelector(
                        label = "Pendidikan Terakhir",
                        options = listOf("SMA", "D3", "S1", "S2", "S3"),
                        selectedOption = pendidikanTerakhir,
                        onOptionSelected = { pendidikanTerakhir = it }
                    )
                }

                item {
                    FormInputRow(
                        label = "Tanggal Bergabung (YYYY-MM-DD)*",
                        value = tanggalMasuk,
                        onValueChange = { tanggalMasuk = it },
                        placeholder = "e.g. 2024-03-31",
                        infoHint = "Ketik dalam format ISO YYYY-MM-DD."
                    )
                }
            }

            // Tab 2: BIODATA DETIL PRIBADI
            if (activeFormTab == 1) {
                item {
                    Text(
                        text = "Biodata Diri Karyawan",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1E293B)
                    )
                }

                item {
                    FormInputRow(
                        label = "Nama Lengkap Karyawan*",
                        value = namaLengkap,
                        onValueChange = { namaLengkap = it },
                        placeholder = "e.g. Dian Pratama"
                    )
                }

                item {
                    FormInputRow(
                        label = "Gelar Belakang (Opsional)",
                        value = gelar,
                        onValueChange = { gelar = it },
                        placeholder = "e.g. S.Kom., M.B.A."
                    )
                }

                item {
                    FormInputRow(
                        label = "Nomor Induk Kependudukan (NIK)*",
                        value = nik,
                        onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 16) nik = it },
                        placeholder = "Ketik 16 Digit angka KTP",
                        keyboardType = KeyboardType.Number,
                        infoHint = "Wajib berupa 16 digit angka kependudukan nasional."
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormInputRow(
                                label = "Tempat Lahir*",
                                value = tempatLahir,
                                onValueChange = { tempatLahir = it },
                                placeholder = "e.g. Samarinda"
                            )
                        }
                        Box(modifier = Modifier.weight(1.2f)) {
                            FormInputRow(
                                label = "Tgl Lahir (YYYY-MM-DD)*",
                                value = tanggalLahir,
                                onValueChange = { tanggalLahir = it },
                                placeholder = "e.g. 1995-10-18"
                            )
                        }
                    }
                }

                item {
                    FormSpinnerSelector(
                        label = "Jenis Kelamin",
                        options = listOf("Laki-laki", "Perempuan"),
                        selectedOption = jenisKelamin,
                        onOptionSelected = { jenisKelamin = it }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1.2f)) {
                            FormSpinnerSelector(
                                label = "Agama",
                                options = listOf("Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Khonghucu"),
                                selectedOption = agama,
                                onOptionSelected = { agama = it }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormSpinnerSelector(
                                label = "Gol. Darah",
                                options = listOf("-", "A", "B", "AB", "O"),
                                selectedOption = golonganDarah,
                                onOptionSelected = { golonganDarah = it }
                            )
                        }
                    }
                }

                item {
                    FormSpinnerSelector(
                        label = "Status Pernikahan",
                        options = listOf("Lajang", "Menikah", "Cerai"),
                        selectedOption = statusPernikahan,
                        onOptionSelected = { statusPernikahan = it }
                    )
                }
            }

            // Tab 3: KONTAK & ALAMAT
            if (activeFormTab == 2) {
                item {
                    Text(
                        text = "Kontak Hubungan & Alamat",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1E293B)
                    )
                }

                item {
                    FormInputRow(
                        label = "Nomor Handphone (WhatsApp)*",
                        value = noHp,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '+' }) noHp = it },
                        placeholder = "e.g. 08123456789",
                        keyboardType = KeyboardType.Phone
                    )
                }

                item {
                    FormInputRow(
                        label = "Alamat Email*",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "e.g. dian.pratama@perusahaan.com",
                        keyboardType = KeyboardType.Email
                    )
                }

                item {
                    FormInputRow(
                        label = "Alamat Domisili Lengkap*",
                        value = alamatLengkap,
                        onValueChange = { alamatLengkap = it },
                        placeholder = "Tulis nama jalan, RT/RW, kelurahan, kecamatan, kota, provinsi...",
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )
                }

                item {
                    FormInputRow(
                        label = "Catatan Tambahan Kepersonaliaan (Opsional)",
                        value = catatanInternal,
                        onValueChange = { catatanInternal = it },
                        placeholder = "Sertifikasi keahlian khusus, catatan performa kerja, kebiasaan positif...",
                        singleLine = false,
                        modifier = Modifier.height(100.dp)
                    )
                }
            }
        }

        // Bottom triggers bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Cancel Button
                OutlinedButton(
                    onClick = {
                        if (empToEdit != null) {
                            viewModel.navigateTo(ActiveScreen.DETAIL)
                        } else {
                            viewModel.navigateTo(ActiveScreen.LIST)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }

                // Primary Save Button
                Button(
                    onClick = {
                        // Complete Client-Side Validations
                        if (namaLengkap.trim().isEmpty()) {
                            validationErrorMessage = "Nama Lengkap wajib diisi."
                            showValidationError = true
                        } else if (nip.trim().isEmpty()) {
                            validationErrorMessage = "NIP Kedudukan wajib diisi."
                            showValidationError = true
                        } else if (nik.length != 16) {
                            validationErrorMessage = "NIK KTP wajib berupa 16 digit angka."
                            showValidationError = true
                        } else if (jabatan.trim().isEmpty()) {
                            validationErrorMessage = "Jabatan Kerja wajib diisi."
                            showValidationError = true
                        } else if (noHp.trim().length < 9) {
                            validationErrorMessage = "Nomor Handphone tidak valid."
                            showValidationError = true
                        } else if (!email.contains("@")) {
                            validationErrorMessage = "Alamat Email tidak valid."
                            showValidationError = true
                        } else if (alamatLengkap.trim().isEmpty()) {
                            validationErrorMessage = "Alamat Lengkap wajib diisi."
                            showValidationError = true
                        } else {
                            // Validation Passed! Prepare Entity object
                            val payload = Employee(
                                id = empToEdit?.id ?: 0,
                                nip = nip.trim(),
                                nik = nik.trim(),
                                namaLengkap = namaLengkap.trim(),
                                gelar = gelar.trim(),
                                email = email.trim(),
                                noHp = noHp.trim(),
                                jabatan = jabatan.trim(),
                                departemen = departemen,
                                statusPegawai = statusPegawai,
                                tanggalMasuk = tanggalMasuk.trim(),
                                tempatLahir = tempatLahir.trim(),
                                tanggalLahir = tanggalLahir.trim(),
                                jenisKelamin = jenisKelamin,
                                agama = agama,
                                golonganDarah = golonganDarah,
                                statusPernikahan = statusPernikahan,
                                pendidikanTerakhir = pendidikanTerakhir,
                                alamatLengkap = alamatLengkap.trim(),
                                catatanInternal = catatanInternal.trim(),
                                avatarColorIndex = empToEdit?.avatarColorIndex ?: (0..7).random()
                            )
                            viewModel.saveEmployee(payload)
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showValidationError) {
        AlertDialog(
            onDismissRequest = { showValidationError = false },
            icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Validasi Form Gagal", fontWeight = FontWeight.Bold) },
            text = { Text(validationErrorMessage) },
            confirmButton = {
                Button(onClick = { showValidationError = false }) {
                    Text("Mengerti")
                }
            }
        )
    }
}

// Custom Helper Components for Form layout

@Composable
fun FormInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    infoHint: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF49454F) // Aligned label color
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color(0xFF49454F).copy(alpha = 0.6f)) },
            singleLine = singleLine,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = RoundedCornerShape(14.dp), // More modern curvature
            modifier = modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF6750A4),
                unfocusedBorderColor = Color(0xFFCAC4D0).copy(alpha = 0.8f)
            )
        )
        if (infoHint != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = infoHint,
                fontSize = 11.sp,
                color = Color(0xFF49454F).copy(alpha = 0.7f),
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun FormSpinnerSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF49454F) // Aligned label color
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp), // Matching modern curvature
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)), // Material outline standards
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF1D1B20))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedOption,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Pilih",
                        tint = Color(0xFF49454F)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillModifierWidth()
                    .background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontWeight = FontWeight.SemiBold, color = Color(0xFF1D1B20)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Lightweight custom modifier helper logic to make Dropdown match input width easily
fun Modifier.fillModifierWidth(): Modifier = this.fillMaxWidth()
