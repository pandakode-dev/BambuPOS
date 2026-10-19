

<div align="center">
<img src="app/src/main/res/drawable/logo_bambupos.png" alt="Logo BambuPOS" width="140" />

# 🐼 BambuPOS — Kasir, Struk Digital & Manajemen UMKM Pintar
### **Native Android POS (Point of Sale) Ecosystem by PandaKode**

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-0F172A?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Barcode Scanner](https://img.shields.io/badge/AI-Barcode%20Scan%20(ML%20Kit)-F97316?style=for-the-badge&logo=google&logoColor=white)
![Business](https://img.shields.io/badge/Business-Multi--Tier%20Wholesale%20%26%20Debt-EA580C?style=for-the-badge&logo=shopware&logoColor=white)

**BambuPOS** adalah aplikasi kasir wirausaha (*Point of Sale*) dan manajemen keuangan UMKM pintar berbasis **Native Android (Kotlin & Jetpack Compose)** dengan identitas visual *Sunset Terracotta, Navy Dongker, & Warm Cream*. Aplikasi ini dirancang khusus untuk mengatasi keterbatasan kasir konvensional dengan mengintegrasikan pencatatan penjualan cepat, scan barcode kamera, parkir transaksi (hold bill), manajemen grosir, buku biaya operasional, serta pembuatan struk digital dan stiker label harga rak secara offline-first.

</div>

---

## 📸 Tampilan Aplikasi (Screenshots)

| Layar Kasir Pintar | Parkir Pesanan Meja | Konfirmasi Bayar & Diskon | Pratinjau Struk Digital | Stok, Grosir, & Label Rak | Laporan Laba & Buku Biaya |
| :---: | :---: | :---: | :---: | :---: | :---: |
|<img width="216" height="480" alt="Screenshot_20261019_061516" src="https://github.com/user-attachments/assets/feb6be26-01e7-4f44-a021-e8ad57abf313" /> | <img width="1080" height="2400" alt="Screenshot_20261019_170921" src="https://github.com/user-attachments/assets/1804f0d2-395a-4b04-9298-6421e7314e5a" /> | <img width="216" height="480" alt="Screenshot_20261019_061636" src="https://github.com/user-attachments/assets/464e7d11-9e15-4042-ab64-dc1e322e2495" /> | <img width="216" height="480" alt="Screenshot_20261019_061516" src="https://github.com/user-attachments/assets/feb6be26-01e7-4f44-a021-e8ad57abf313" /> | <img width="216" height="480" alt="Screenshot_20261019_061523" src="https://github.com/user-attachments/assets/993c29cf-1360-424d-a908-09edad3bd6df" /> | <img width="216" height="480" alt="Screenshot_20261019_061541" src="https://github.com/user-attachments/assets/0f800749-112f-419b-b567-867295e975fc" /> |






---

## ✨ Fitur Unggulan "Super UMKM" (Core Features)

### 1. ⚡ Layar Kasir Cepat & Scan Barcode (ML Kit)
* **Scan Barcode Kilat:** Menggunakan kamera ponsel dengan teknologi *Google ML Kit Barcode Scanning* untuk memindai barcode produk secara instan dan otomatis memasukkannya ke keranjang belanja.
* **Kategori & Pencarian Cepat:** Akses menu interaktif dengan filter kategori yang dapat digeser serta kolom pencarian serbaguna.
* **Kalkulator Nominal Bebas:** Input barang atau jasa kustom secara cepat tanpa harus mendaftarkan barang terlebih dahulu di gudang.

### 2. ⏸️ Hold Bill & Parkir Pesanan (Open Bill Meja Resto/Kafe)
* **Parkir Pesanan:** Kemampuan untuk menunda (parkir) pesanan pelanggan sementara waktu (misal: pesanan berdasarkan nomor meja kafe/restoran) dan melayani pelanggan lain.
* **Panggil Kembali Pesanan:** Kasir dapat memanggil kembali pesanan yang ditunda kapan saja dari menu "Meja Aktif" untuk diproses ke pembayaran.

### 3. 🍽️ Variasi Tipe Pesanan & Catatan Khusus (Dapur/Pelanggan)
* Mendukung penentuan jenis pesanan seperti **Dine-In (Meja 01-04), Bungkus/Takeaway, atau Delivery**.
* Kasir dapat menambahkan catatan khusus untuk setiap barang di keranjang belanja (misal: *"Es sedikit, pedas manis"*), yang nantinya akan tercetak rapi di struk digital.

### 4. ⚖️ Tiering Harga Grosir Otomatis (Wholesale Tier Pricing)
* Mengakomodasi kebutuhan grosir atau warung kelontong. Harga satuan barang otomatis akan berubah menjadi harga grosir yang lebih murah jika jumlah pembelian di keranjang mencapai batas minimum grosir yang ditentukan.

### 5. 🧾 Generator Struk & Label Rak Toko (Grafis Berwarna)
* **Penerbitan Struk Digital:** Menghasilkan gambar struk belanja (PNG) bernuansa *Navy-Orange-Cream* yang profesional, lengkap dengan informasi diskon, pajak, nominal diterima, kembalian, tipe pesanan, dan QR Code verifikasi.
* **Bagikan ke WhatsApp:** Kirim struk secara instan ke WhatsApp pelanggan tanpa perlu mencetak kertas fisik (hemat kertas thermal!).
* **Stiker Label Rak:** Desain dan bagikan/cetak label harga rak produk lengkap dengan Code-128 barcode digitalnya.

### 6. 📒 Piutang Buku Kasbon (Debt Book) & Tagih WhatsApp
* Mencatat transaksi dengan metode pembayaran **Kasbon** yang belum lunas.
* **Tombol Kirim Tagihan (WA):** Membuka chat WhatsApp pelanggan dengan pesan pengingat yang sopan secara otomatis, lengkap dengan detail nomor nota dan nominal piutang.
* **Manajemen Pelunasan:** Melacak dan mengubah status transaksi dari "KASBON" menjadi "LUNAS" dengan sekali ketukan.

### 7. 💸 Buku Biaya Operasional & Laporan Laba Bersih Murni
* **Buku Biaya Operasional:** Mencatat segala pengeluaran usaha (misal: listrik, air, es batu, kemasan, gaji karyawan).
* **Laba Bersih Murni (Net Profit Engine):** Menampilkan laba bersih riil secara akurat dengan rumus `Laba Kotor - Biaya Operasional`, bukan sekadar omzet atau pendapatan kotor.
* **Export CSV (Excel):** Mengunduh seluruh riwayat transaksi dan buku biaya harian menjadi berkas `.CSV` yang siap diolah di Microsoft Excel atau Google Sheets.

---

## 🛠️ Tech Stack & Arsitektur

* **UI/UX**: Jetpack Compose (Material Design 3) dengan arsitektur responsif dan palet Sunset Terracotta, Navy Dongker, & Warm Cream.
* **Database & Penyimpanan**: Lokal via `SharedPreferences` terenkripsi dengan konversi data berbasis `JSON` (Offline-First).
* **Pemindai Barcode**: `com.google.android.gms:play-services-code-scanner`.
* **Generator Grafis & Struk**: Android Graphics `Canvas`, `Bitmap`, `Paint`, dan `Typeface` API.
* **QR & Barcode Engine**: ZXing (`com.google.zxing:core`) untuk encoding QR Code dan Code-128.
* **Arsitektur Kode**: Arsitektur 5-File Terpisah yang memudahkan pemeliharaan kode dan pelacakan bug.

---

## 👨‍💻 Pengembang (Author)

Dikembangkan oleh **Muhammad Saputra**  
🏢 **Founder of PandaKode** *(Software House — Android Apps, Web Systems, IoT Solutions, Desktop Apps & Games)*  
📍 Pekanbaru, Riau, Indonesia
