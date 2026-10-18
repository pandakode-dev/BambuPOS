package com.example.bambupos

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// 1. LAYAR GUDANG, ATUR HARGA GROSIR & CETAK STIKER LABEL RAK TOKO
// ============================================================================
@Composable
fun PosInventoryScreen(
    products: MutableList<PosProduct>,
    onProductsChanged: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var labelPreviewBitmap by remember { mutableStateOf<Pair<Bitmap, String>?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Gudang, Harga Grosir & Label Rak", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NavyDongker)
                Text("Klik ikon Label 🏷️ untuk cetak stiker harga rak toko", fontSize = 11.sp, color = Color.Gray)
            }
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Produk Baru", color = PureWhite, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items(products, key = { it.id }) { item ->
                val marginUntung = item.sellPrice - item.costPrice
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (item.stock <= 5) CrimsonAlert else CreamBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = NavyDongker)
                            Text("Eceran: ${formatRupiah(item.sellPrice)} • Modal: ${formatRupiah(item.costPrice)}", fontSize = 12.sp, color = Color.DarkGray)
                            if (item.wholesaleMinQty > 0) {
                                Text("Grosir (Min. ${item.wholesaleMinQty}): ${formatRupiah(item.wholesalePrice)}/pcs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTerracotta)
                            }
                            Text("Untung: +${formatRupiah(marginUntung)} • Stok: ${item.stock}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldProfit)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Tombol Cetak Stiker Label Rak Toko
                            IconButton(
                                onClick = {
                                    val btm = generateShelfPriceTagBitmap(item, PosStorage.getShopName(context))
                                    if (btm != null) labelPreviewBitmap = Pair(btm, item.name)
                                }
                            ) {
                                Icon(Icons.Default.LocalOffer, contentDescription = "Cetak Label Rak", tint = VibrantOrange)
                            }

                            OutlinedButton(
                                onClick = {
                                    val idx = products.indexOfFirst { it.id == item.id }
                                    if (idx != -1) {
                                        products[idx] = item.copy(stock = item.stock + 10)
                                        onProductsChanged()
                                        Toast.makeText(context, "Stok ${item.name} +10!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+10 Stok", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDongker)
                            }

                            IconButton(onClick = { products.remove(item); onProductsChanged() }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = CrimsonAlert)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah Produk Lengkap dengan Opsi Harga Grosir
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Minuman") }
        var barcode by remember { mutableStateOf("899${System.currentTimeMillis() % 10000}") }
        var costStr by remember { mutableStateOf("8000") }
        var sellStr by remember { mutableStateOf("15000") }
        var stockStr by remember { mutableStateOf("50") }
        var wholesaleMinStr by remember { mutableStateOf("5") }
        var wholesalePriceStr by remember { mutableStateOf("13500") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = PureWhite,
            title = { Text("Tambah Produk & Atur Grosir", fontWeight = FontWeight.ExtraBold, color = NavyDongker) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Produk / Jasa") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategori") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = costStr, onValueChange = { costStr = it.filter { c -> c.isDigit() } }, label = { Text("Harga Modal") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = sellStr, onValueChange = { sellStr = it.filter { c -> c.isDigit() } }, label = { Text("Harga Eceran") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = wholesaleMinStr, onValueChange = { wholesaleMinStr = it.filter { c -> c.isDigit() } }, label = { Text("Min. Grosir (0=Off)") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = wholesalePriceStr, onValueChange = { wholesalePriceStr = it.filter { c -> c.isDigit() } }, label = { Text("Harga Grosir") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = stockStr, onValueChange = { stockStr = it.filter { c -> c.isDigit() } }, label = { Text("Stok Awal") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            products.add(
                                0,
                                PosProduct(
                                    name = name,
                                    category = category.ifBlank { "Umum" },
                                    barcode = barcode,
                                    costPrice = costStr.toLongOrNull() ?: 0L,
                                    sellPrice = sellStr.toLongOrNull() ?: 0L,
                                    stock = stockStr.toIntOrNull() ?: 10,
                                    wholesaleMinQty = wholesaleMinStr.toIntOrNull() ?: 0,
                                    wholesalePrice = wholesalePriceStr.toLongOrNull() ?: 0L
                                )
                            )
                            onProductsChanged()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Text("Simpan Produk", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }

    // Dialog Pratinjau Stiker Label Harga Rak Toko
    labelPreviewBitmap?.let { (btm, prodName) ->
        AlertDialog(
            onDismissRequest = { labelPreviewBitmap = null },
            containerColor = PureWhite,
            title = { Text("🏷️ Stiker Label Harga Rak Toko", fontWeight = FontWeight.ExtraBold, color = NavyDongker) },
            text = {
                Image(
                    bitmap = btm.asImageBitmap(),
                    contentDescription = "Label Rak",
                    modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                )
            },
            confirmButton = {
                Button(
                    onClick = { shareReceiptImage(context, btm, "Label_${prodName}") },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan / Cetak Label", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { labelPreviewBitmap = null }) { Text("Tutup") }
            }
        )
    }
}

// ============================================================================
// 2. LAYAR LAPORAN LABA BERSIH MURNI, BUKU BIAYA OPERASIONAL & EXPORT EXCEL
// ============================================================================
@Composable
fun PosReportAndDebtScreen(
    transactions: MutableList<PosTransaction>,
    expenses: MutableList<StoreExpense>,
    onTransactionsChanged: () -> Unit,
    onExpensesChanged: () -> Unit
) {
    val context = LocalContext.current
    var shopName by remember { mutableStateOf(PosStorage.getShopName(context)) }
    var shopAddress by remember { mutableStateOf(PosStorage.getShopAddress(context)) }
    var cashierName by remember { mutableStateOf(PosStorage.getCashierName(context)) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val totalOmzet = transactions.filter { it.isPaid }.sumOf { it.totalAmount }
    val totalLabaKotor = transactions.filter { it.isPaid }.sumOf { it.totalProfit }
    val totalBiayaOperasional = expenses.sumOf { it.amount }
    val labaBersihMurni = totalLabaKotor - totalBiayaOperasional
    val totalPiutangKasbon = transactions.filter { !it.isPaid }.sumOf { it.totalAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KARTU DASHBOARD KEUANGAN LENGKAP + TOMBOL EXPORT EXCEL CSV
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDongker),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, VibrantOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📊 Laporan Keuangan & Laba Bersih", color = SoftCreamCard, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { exportTransactionsToCsv(context, transactions, expenses) },
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.TableView, contentDescription = null, tint = PureWhite, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Excel (.CSV)", fontSize = 10.sp, color = PureWhite, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Omzet Lunas", color = Color.LightGray, fontSize = 11.sp)
                            Text(formatRupiah(totalOmzet), color = PureWhite, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Laba Kotor (Jual - HPP)", color = Color.LightGray, fontSize = 11.sp)
                            Text(formatRupiah(totalLabaKotor), color = SoftCreamCard, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    HorizontalDivider(color = NavySurface)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Biaya Operasional Toko", color = Color.LightGray, fontSize = 11.sp)
                            Text("- ${formatRupiah(totalBiayaOperasional)}", color = CrimsonAlert, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("LABA BERSIH MURNI", color = SoftCreamCard, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            Text(formatRupiah(labaBersihMurni), color = EmeraldProfit, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Piutang Kasbon Pelanggan:", color = SoftCreamCard, fontSize = 12.sp)
                        Text(formatRupiah(totalPiutangKasbon), color = VibrantOrange, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // BUKU PENGELUARAN OPERASIONAL TOKO (LISTRIK, ES BATU, PLASTIK, DLL.)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("💸 Buku Biaya Operasional Toko", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NavyDongker)
                            Text("Catat biaya harian agar hitungan laba bersih akurat", fontSize = 11.sp, color = Color.Gray)
                        }
                        OutlinedButton(
                            onClick = { showAddExpenseDialog = true },
                            border = BorderStroke(1.dp, VibrantOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("+ Catat Biaya", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTerracotta)
                        }
                    }

                    expenses.take(3).forEach { exp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${exp.title} (${exp.category})", fontSize = 12.sp, color = NavyDongker)
                            Text("- ${formatRupiah(exp.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonAlert)
                        }
                    }
                }
            }
        }

        // PENGATURAN PROFIL TOKO & NAMA KASIR SHIFT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏪 Identitas Struk Toko & Shift Kasir", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NavyDongker)
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it; PosStorage.saveShopInfo(context, shopName, shopAddress, cashierName) },
                        label = { Text("Nama Toko / Kafe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cashierName,
                            onValueChange = { cashierName = it; PosStorage.saveShopInfo(context, shopName, shopAddress, cashierName) },
                            label = { Text("Nama Kasir Bertugas") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = shopAddress,
                            onValueChange = { shopAddress = it; PosStorage.saveShopInfo(context, shopName, shopAddress, cashierName) },
                            label = { Text("Kontak / Alamat") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        item {
            Text("Riwayat Nota & Buku Kasbon (${transactions.size})", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = NavyDongker)
        }

        items(transactions, key = { it.id }) { trx ->
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, if (!trx.isPaid) VibrantOrange else CreamBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (trx.isPaid) SoftCreamCard else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (trx.isPaid) "LUNAS • ${trx.paymentMethod} (${trx.orderType})" else "KASBON BELUM LUNAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (trx.isPaid) NavyDongker else CrimsonAlert,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(trx.timestamp, fontSize = 11.sp, color = Color.Gray)
                    }

                    Text("${trx.invoiceNo} — ${trx.customerName}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = NavyDongker)
                    Text(trx.itemsSummary, fontSize = 12.sp, color = Color.DarkGray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(formatRupiah(trx.totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DeepTerracotta)
                            Text("Laba Transaksi: +${formatRupiah(trx.totalProfit)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldProfit)
                        }

                        if (!trx.isPaid) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { sendWhatsAppDebtReminder(context, trx.customerPhone, trx.customerName, trx.invoiceNo, trx.totalAmount) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = EmeraldProfit, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tagih WA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDongker)
                                }

                                Button(
                                    onClick = {
                                        val idx = transactions.indexOfFirst { it.id == trx.id }
                                        if (idx != -1) {
                                            transactions[idx] = trx.copy(isPaid = true, paymentMethod = "Dilunasi")
                                            onTransactionsChanged()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Lunasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        var expTitle by remember { mutableStateOf("") }
        var expCategory by remember { mutableStateOf("Bahan Baku / Es / Plastik") }
        var expAmount by remember { mutableStateOf("25000") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            containerColor = PureWhite,
            title = { Text("Catat Pengeluaran Toko", fontWeight = FontWeight.ExtraBold, color = NavyDongker) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = expTitle, onValueChange = { expTitle = it }, label = { Text("Keterangan (Misal: Beli Cup & Es Batu)") }, singleLine = true)
                    OutlinedTextField(value = expCategory, onValueChange = { expCategory = it }, label = { Text("Kategori Biaya") }, singleLine = true)
                    OutlinedTextField(value = expAmount, onValueChange = { expAmount = it.filter { c -> c.isDigit() } }, label = { Text("Nominal Biaya (Rp)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (expTitle.isNotBlank()) {
                            val time = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                            expenses.add(0, StoreExpense(title = expTitle, category = expCategory, amount = expAmount.toLongOrNull() ?: 0L, timestamp = time))
                            onExpensesChanged()
                            showAddExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Text("Simpan Biaya", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) { Text("Batal") }
            }
        )
    }
}