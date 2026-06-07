package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nip: String,                     // Nomor Induk Pegawai
    val nik: String,                     // Nomor Induk Kependudukan (16 digit)
    val namaLengkap: String,             // Nama Lengkap
    val gelar: String = "",              // Gelar Akademik/Sertifikasi
    val email: String,                   // Alamat Email
    val noHp: String,                    // No Handphone
    val jabatan: String,                 // Jabatan Kerja
    val departemen: String,              // Departemen/Divisi
    val statusPegawai: String,           // Tetap, Kontrak, Magang
    val tanggalMasuk: String,            // Tanggal Bergabung
    val tempatLahir: String,             // Tempat Lahir
    val tanggalLahir: String,            // Tanggal Lahir
    val jenisKelamin: String,            // Laki-laki / Perempuan
    val agama: String,                   // Agama
    val golonganDarah: String = "-",     // Golongan Darah
    val statusPernikahan: String,         // Lajang, Menikah, Cerai
    val pendidikanTerakhir: String,      // Pendidikan Terakhir (SMA, S1, S2, dsb)
    val alamatLengkap: String,           // Alamat Domisili
    val catatanInternal: String = "",    // Catatan HR/Internal tambahan
    val avatarColorIndex: Int = 0        // Menyimpan warna avatar pegawai
)
