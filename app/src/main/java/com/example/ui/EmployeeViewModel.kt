package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiApiClient
import com.example.api.GenerateContentRequest
import com.example.api.Content
import com.example.api.Part
import com.example.data.Employee
import com.example.data.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ActiveScreen {
    DASHBOARD,
    LIST,
    DETAIL,
    ADD_EDIT
}

class EmployeeViewModel(private val repository: EmployeeRepository) : ViewModel() {

    // Initial state setup
    val currentScreen = MutableStateFlow(ActiveScreen.DASHBOARD)
    val selectedEmployeeId = MutableStateFlow<Int?>(null)
    
    // Form Edit state (Null means Add screen, Non-null means Edit screen)
    val employeeBeingEdited = MutableStateFlow<Employee?>(null)

    // Filter and search states
    val searchQuery = MutableStateFlow("")
    val selectedDepartment = MutableStateFlow("Semua")
    val selectedStatus = MutableStateFlow("Semua")

    // AI States
    val dashboardAiInsight = MutableStateFlow("")
    val isDashboardAiLoading = MutableStateFlow(false)
    val employeeAiAnalysis = MutableStateFlow("")
    val isEmployeeAiLoading = MutableStateFlow(false)

    // Retrieve and combine for filtered flow
    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredEmployees: StateFlow<List<Employee>> = combine(
        allEmployees,
        searchQuery,
        selectedDepartment,
        selectedStatus
    ) { employees, query, dept, status ->
        employees.filter { emp ->
            val matchQuery = query.isEmpty() || 
                    emp.namaLengkap.contains(query, ignoreCase = true) || 
                    emp.nip.contains(query, ignoreCase = true) || 
                    emp.jabatan.contains(query, ignoreCase = true) ||
                    emp.nik.contains(query, ignoreCase = true)
            
            val matchDept = dept == "Semua" || emp.departemen == dept
            val matchStatus = status == "Semua" || emp.statusPegawai == status
            
            matchQuery && matchDept && matchStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // If the database is completely empty on startup, pre-populate with rich, professional Indonesian biodata samples
        viewModelScope.launch {
            repository.allEmployees.collect { list ->
                if (list.isEmpty()) {
                    insertInitialDummyData()
                }
            }
        }
    }

    private suspend fun insertInitialDummyData() {
        val sampleEmployees = listOf(
            Employee(
                nip = "PEG-199105-021",
                nik = "3273012205910002",
                namaLengkap = "Budi Santoso",
                gelar = "S.Kom., M.T.",
                email = "budi.santoso@perusahaan.com",
                noHp = "081234567890",
                jabatan = "Kepala Divisi IT",
                departemen = "Teknologi Informasi",
                statusPegawai = "Tetap",
                tanggalMasuk = "2018-05-12",
                tempatLahir = "Malang",
                tanggalLahir = "1991-05-22",
                jenisKelamin = "Laki-laki",
                agama = "Islam",
                golonganDarah = "A",
                statusPernikahan = "Menikah",
                pendidikanTerakhir = "S2",
                alamatLengkap = "Jl. Danau Toba No. 12, Sawojajar, Kota Malang, Jawa Timur",
                catatanInternal = "Sertifikasi AWS Solutions Architect & Scrum Master. Memimpin 15 insinyur perangkat lunak.",
                avatarColorIndex = 0
            ),
            Employee(
                nip = "PEG-199408-112",
                nik = "3174025208940003",
                namaLengkap = "Siti Rahmawati",
                gelar = "S.Psi., M.M.",
                email = "siti.rahma@perusahaan.com",
                noHp = "085678901234",
                jabatan = "HR Manajer",
                departemen = "Human Resources",
                statusPegawai = "Tetap",
                tanggalMasuk = "2020-08-18",
                tempatLahir = "Bandung",
                tanggalLahir = "1994-08-12",
                jenisKelamin = "Perempuan",
                agama = "Islam",
                golonganDarah = "AB",
                statusPernikahan = "Menikah",
                pendidikanTerakhir = "S2",
                alamatLengkap = "Perumahan Hijau Asri Blok C-4, Buah Batu, Kota Bandung, Jawa Barat",
                catatanInternal = "Menguasai HR Analytics, rekrutmen talenta teknologi, dan penyelesaian konflik karyawan.",
                avatarColorIndex = 1
            ),
            Employee(
                nip = "PEG-199602-234",
                nik = "3578011502960001",
                namaLengkap = "Ahmad Hidayat",
                gelar = "S.E.",
                email = "ahmad.hidayat@perusahaan.com",
                noHp = "087712345678",
                jabatan = "Auditor Keuangan Utama",
                departemen = "Keuangan",
                statusPegawai = "Tetap",
                tanggalMasuk = "2021-02-01",
                tempatLahir = "Surabaya",
                tanggalLahir = "1996-02-15",
                jenisKelamin = "Laki-laki",
                agama = "Islam",
                golonganDarah = "O",
                statusPernikahan = "Lajang",
                pendidikanTerakhir = "S1",
                alamatLengkap = "Apartemen Gunawangsa Tower B No. 405, Sukolilo, Surabaya, Jawa Timur",
                catatanInternal = "Memiliki sertifikasi Akuntan Publik (CPA). Bertanggung jawab terhadap sirkulasi anggaran internal.",
                avatarColorIndex = 2
            ),
            Employee(
                nip = "PEG-200111-402",
                nik = "3171034411010006",
                namaLengkap = "Diana Lestari",
                gelar = "S.I.Kom.",
                email = "diana.lestari@perusahaan.com",
                noHp = "082198765432",
                jabatan = "Spesialis Humas & Pemasaran",
                departemen = "Pemasaran",
                statusPegawai = "Kontrak",
                tanggalMasuk = "2023-11-15",
                tempatLahir = "Jakarta",
                tanggalLahir = "2001-11-24",
                jenisKelamin = "Perempuan",
                agama = "Kristen",
                golonganDarah = "B",
                statusPernikahan = "Lajang",
                pendidikanTerakhir = "S1",
                alamatLengkap = "Jl. Kemang Timur IX No. 42B, Mampang Prapatan, Jakarta Selatan",
                catatanInternal = "Mengelola media sosial korporat. Membantu memperkuat brand reputasi umum digital.",
                avatarColorIndex = 3
            ),
            Employee(
                nip = "PEG-200304-511",
                nik = "5171021404030002",
                namaLengkap = "I Wayan Rian Wijaya",
                gelar = "A.Md.Kom.",
                email = "rian.wijaya@perusahaan.com",
                noHp = "081919283746",
                jabatan = "Teknisi Operasional Lapangan",
                departemen = "Operasional",
                statusPegawai = "Magang",
                tanggalMasuk = "2024-04-01",
                tempatLahir = "Denpasar",
                tanggalLahir = "2003-04-14",
                jenisKelamin = "Laki-laki",
                agama = "Hindu",
                golonganDarah = "O",
                statusPernikahan = "Lajang",
                pendidikanTerakhir = "D3",
                alamatLengkap = "Jl. Sunset Road Gg. Merpati No. 3, Seminyak, Badung, Bali",
                catatanInternal = "Mahasiswa berprestasi magang dari Politeknik Negeri Bali. Mahir instalasi jaringan fisik.",
                avatarColorIndex = 4
            )
        )
        for (emp in sampleEmployees) {
            repository.insert(emp)
        }
    }

    fun navigateTo(screen: ActiveScreen) {
        currentScreen.value = screen
    }

    fun selectEmployee(id: Int) {
        selectedEmployeeId.value = id
        employeeAiAnalysis.value = "" //Reset AI feedback for clean loading
        currentScreen.value = ActiveScreen.DETAIL
    }

    fun startAddEmployee() {
        employeeBeingEdited.value = null
        currentScreen.value = ActiveScreen.ADD_EDIT
    }

    fun startEditEmployee(employee: Employee) {
        employeeBeingEdited.value = employee
        currentScreen.value = ActiveScreen.ADD_EDIT
    }

    fun saveEmployee(employee: Employee) {
        viewModelScope.launch {
            if (employee.id == 0) {
                // Fresh addition
                repository.insert(employee)
            } else {
                // Editing existing
                repository.update(employee)
            }
            // Navigate back to listing or detail
            if (employee.id == 0) {
                currentScreen.value = ActiveScreen.LIST
            } else {
                selectedEmployeeId.value = employee.id
                currentScreen.value = ActiveScreen.DETAIL
            }
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.delete(employee)
            selectedEmployeeId.value = null
            currentScreen.value = ActiveScreen.LIST
        }
    }

    // --- AI GENERATION WITH GEMINI API ---

    fun generateDashboardInsight() {
        viewModelScope.launch {
            isDashboardAiLoading.value = true
            dashboardAiInsight.value = ""
            
            val employees = allEmployees.value
            if (employees.isEmpty()) {
                dashboardAiInsight.value = "Tidak ada data pegawai yang tersedia untuk dianalisis."
                isDashboardAiLoading.value = false
                return@launch
            }

            // Prepare summary text for the AI prompt
            val total = employees.size
            val depts = employees.groupBy { it.departemen }.mapValues { it.value.size }
            val status = employees.groupBy { it.statusPegawai }.mapValues { it.value.size }
            val gender = employees.groupBy { it.jenisKelamin }.mapValues { it.value.size }

            val prompt = """
                Anda adalah seorang konsultan HR profesional senior di Indonesia.
                Berikut adalah rekap ringkas data statistik karyawan di perusahaan saat ini:
                - Total Karyawan: $total orang
                - Distribusi per Departemen: $depts
                - Distribusi Status Kepegawaian: $status
                - Distribusi Gender: $gender

                Tuliskan analisis singkat (maksimal 3 paragraf pendek, profesional, ramah dan terstruktur menggunakan bullet points jika diperlukan) dalam Bahasa Indonesia mengenai:
                1. Keseimbangan kuantitas divisi/departemen (apakah ada kompetensi yang terlalu menumpuk atau kurang).
                2. Review status kepegawaian (apakah perbandingan pegawai tetap, kontrak, dan magang sudah sehat).
                3. Saran aksi operasional strategis yang bisa diambil oleh manajemen HRD.
                Gunakan formatting markdown yang indah, tebalkan kata-kata kunci penting. Jangan terlalu panjang, padatkan wawasan terpenting saja.
            """.trimIndent()

            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
                // Graceful fallback with static simulated professional insight
                dashboardAiInsight.value = """
                    **[Mode Simulasi - API Key Belum Dikonfigurasi]**
                    
                    Berikut adalah analisis simulasi berdasarkan komposisi data saat ini:
                    
                    *   **Analisis Departemen**: Distribusi karyawan saat ini menunjukkan departemen **${depts.keys.firstOrNull() ?: "Teknologi Informasi"}** paling banyak diisi, diikuti oleh departemen pendukung. Penting untuk mengawasi rasio beban kerja agar tidak terjadi *burnout* di departemen yang sibuk.
                    *   **Analisis Status**: Rasio Pegawai Tetap yang mendominasi memberikan stabilitas bisnis jangka panjang yang baik, sementara adanya pegawai Kontrak dan Magang memberikan fleksibilitas operasional yang sehat untuk proyek-proyek taktis berkala.
                    *   **Saran Tindakan**: Direkomendasikan melakukan audit beban kerja (*workload audit*) tahunan dan mengatur rencana pelatihan rotasi keahlian silang (*cross-training*) demi memperkaya keterampilan kepemimpinan staf sekunder.
                """.trimIndent()
                isDashboardAiLoading.value = false
                return@launch
            }

            try {
                val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val aiText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                dashboardAiInsight.value = aiText ?: "Tidak dapat memuat riset analisis dari AI saat ini."
            } catch (e: Exception) {
                dashboardAiInsight.value = "Koneksi gagal saat menghubungi AI: ${e.message ?: "Kesalahan Tidak Diketahui"}"
            } finally {
                isDashboardAiLoading.value = false
            }
        }
    }

    fun generateEmployeeAnalysis(emp: Employee) {
        viewModelScope.launch {
            isEmployeeAiLoading.value = true
            employeeAiAnalysis.value = ""

            val prompt = """
                Anda adalah seorang Psikolog Industri Organisasi sekaligus Penasihat Karir berpengalaman di Indonesia.
                Berikut adalah biodata lengkap seorang pegawai perusahaan kami:
                
                - Nama: ${emp.namaLengkap} ${emp.gelar}
                - NIP: ${emp.nip}
                - NIK: ${emp.nik}
                - Jabatan: ${emp.jabatan}
                - Departemen: ${emp.departemen}
                - Status Kerja: ${emp.statusPegawai} (Tanggal Masuk: ${emp.tanggalMasuk})
                - Jenis Kelamin: ${emp.jenisKelamin}
                - Tempat, Tgl Lahir: ${emp.tempatLahir}, ${emp.tanggalLahir}
                - Pendidikan Terakhir: ${emp.pendidikanTerakhir}
                - Golongan Darah: ${emp.golonganDarah} | Status Pernikahan: ${emp.statusPernikahan}
                - Catatan HRD tambahan: ${emp.catatanInternal}

                Analisis dan buat laporan ulasan singkat berbahasa Indonesia mengenai:
                1. **Kekuatan Utama**: Analisis latar belakang, usia produktif, pendidikan, dan catatan kerjanya.
                2. **Saran Jalur Karir & Pengembangan**: Rekomendasi pelatihan/sertifikasi spesifik atau promosi karir yang tepat di masa depan untuk mendongkrak performanya.
                3. **Pendekatan Personal HR**: Rekomendasi psikologis pendek untuk atasan dalam memotivasi dan memimpin pegawai ini secara harmonis.
                
                Gunakan bahasa yang hangat, positif, taktis, bermartabat tinggi, dan dipisahkan dengan sekat sub-heading menarik serta bullet points. Maksimal 3 paragraf pendek berkualitas tinggi.
            """.trimIndent()

            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
                // Graceful fallback with static simulated professional insight for this employee
                val statusHint = if (emp.statusPegawai == "Tetap") "stabilitas karir jangka panjang" else "peningkatan kinerja untuk pertimbangan kontrak/promosi tetap"
                employeeAiAnalysis.value = """
                    **[Mode Simulasi - API Key Belum Dikonfigurasi]**
                    
                    *   **Analisis Kekuatan**: Sdr. ${emp.namaLengkap} memiliki latar belakang pendidikan formal **${emp.pendidikanTerakhir}** yang relevan dengan perannya sebagai **${emp.jabatan}**. Kombinasi riwayat tempat lahir **${emp.tempatLahir}** dan status **${emp.statusPegawai}** memberikan landasan yang kokoh untuk kontribusi produktif berkesinambungan.
                    *   **Arah Pengembangan Karir**: Direkomendasikan untuk mengikuti sertifikasi kompetensi keahlian lanjutan yang cocok dengan kesibukan **${emp.departemen}**, guna mempersiapkan diri memimpin tim kecil serta mempersiapkan transisi kepemimpinan jangka menengah.
                    *   **Gaya Kepemimpinan**: Berikan otonomi terarah disertai sesi bimbingan mentoring santai tiap kuartal guna mengoptimalkan potensi serta menjaga tingkat loyalitasnya terhadap ekosistem organisasi.
                """.trimIndent()
                isEmployeeAiLoading.value = false
                return@launch
            }

            try {
                val request = GenerateContentRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val aiText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                employeeAiAnalysis.value = aiText ?: "Gagal memformulasikan saran profil untuk pegawai saat ini."
            } catch (e: Exception) {
                employeeAiAnalysis.value = "Koneksi analisis gagal dilakukan: ${e.message}"
            } finally {
                isEmployeeAiLoading.value = false
            }
        }
    }
}

class EmployeeViewModelFactory(private val repository: EmployeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmployeeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
