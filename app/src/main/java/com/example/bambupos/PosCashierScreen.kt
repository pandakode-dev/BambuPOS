package com.example.bambupos

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PosCashierScreen(
    products: List<PosProduct>,
    cart: MutableList<CartItem>,
    savedBills: MutableList<SavedOrderBill>,
    onCheckoutSuccess: (PosTransaction, List<CartItem>) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedOrderType by remember { mutableStateOf("Bungkus") } // Bungkus, Meja 01, Meja 02, Delivery

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showSavedBillsDialog by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var itemForNoteDialog by remember { mutableStateOf<CartItem?>(null) }
    var generatedReceiptPreview by remember { mutableStateOf<Pair<Bitmap, String>?>(null) }

    val orderTypes = listOf("Bungkus", "Meja 01", "Meja 02", "Meja 03", "Meja 04", "Delivery")
    val categories = remember(products.size) {
        listOf("Semua") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchCat = selectedCategory == "Semua" || p.category.equals(selectedCategory, true)
            val matchQuery = p.name.contains(searchQuery, true) || p.barcode.contains(searchQuery, true)
            matchCat && matchQuery
        }
    }

    fun addToCart(product: PosProduct) {
        val idx = cart.indexOfFirst { it.product.id == product.id }
        if (idx != -1) {
            val current = cart[idx]
            cart[idx] = current.copy(qty = current.qty + 1)
        } else {
            cart.add(CartItem(product, 1))
        }
    }

    val subtotal = cart.sumOf { it.product.getEffectivePrice(it.qty) * it.qty }
    val totalQty = cart.sumOf { it.qty }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // BARIS 1: TIPE PESANAN (MEJA / BUNGKUS) & TOMBOL DAFTAR MEJA DIPARKIR (HOLD BILL)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(orderTypes) { type ->
                    val isSelected = selectedOrderType == type
                    Surface(
                        color = if (isSelected) VibrantOrange else PureWhite,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) NavyDongker else CreamBorder),
                        modifier = Modifier.clickable { selectedOrderType = type }
                    ) {
                        Text(
                            text = type,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) PureWhite else NavyDongker,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tombol Buka Pesanan Diparkir (Open Bill)
            Surface(
                color = NavyDongker,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, VibrantOrange),
                modifier = Modifier.clickable { showSavedBillsDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PauseCircle, contentDescription = null, tint = VibrantOrange, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Meja Aktif (${savedBills.size})",
                        color = SoftCreamCard,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // BARIS 2: PENCARIAN, NOMINAL MANUAL & SCAN BARCODE
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari barang / scan...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantOrange) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite
                )
            )

            // Tombol Input Barang Cepat / Nominal Bebas (Kalkulator Kasir)
            FilledTonalIconButton(
                onClick = { showCustomItemDialog = true },
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = SoftCreamCard),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = "Input Manual", tint = DeepTerracotta)
            }

            Button(
                onClick = {
                    val options = GmsBarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .enableAutoZoom()
                        .build()
                    GmsBarcodeScanning.getClient(context, options)
                        .startScan()
                        .addOnSuccessListener { barcode ->
                            val code = barcode.rawValue ?: ""
                            val found = products.find { it.barcode == code }
                            if (found != null) {
                                addToCart(found)
                                Toast.makeText(context, "+1 ${found.name} masuk keranjang!", Toast.LENGTH_SHORT).show()
                            } else {
                                searchQuery = code
                            }
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyDongker),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = VibrantOrange)
                Spacer(modifier = Modifier.width(4.dp))
                Text("SCAN", color = PureWhite, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }

        // BARIS 3: KATEGORI MENU
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyDongker,
                        selectedLabelColor = PureWhite,
                        containerColor = PureWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) VibrantOrange else CreamBorder
                    )
                )
            }
        }

        // DAFTAR KATALOG PRODUK (DENGAN BADGE HARGA GROSIR OTOMATIS)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                val cartItem = cart.find { it.product.id == product.id }
                val inCartQty = cartItem?.qty ?: 0
                val activePrice = product.getEffectivePrice(inCartQty.coerceAtLeast(1))
                val isWholesaleActive = inCartQty >= product.wholesaleMinQty && product.wholesaleMinQty > 0

                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (inCartQty > 0) 2.dp else 1.dp,
                        color = if (inCartQty > 0) VibrantOrange else CreamBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { addToCart(product) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = SoftCreamCard, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = product.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepTerracotta,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Stok: ${product.stock}",
                                        fontSize = 11.sp,
                                        color = if (product.stock <= 5) CrimsonAlert else Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (product.wholesaleMinQty > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (isWholesaleActive) EmeraldProfit else SoftCreamCard,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Grosir ≥${product.wholesaleMinQty}: ${formatRupiah(product.wholesalePrice)}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isWholesaleActive) PureWhite else NavyDongker,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = NavyDongker,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatRupiah(activePrice),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isWholesaleActive) EmeraldProfit else DeepTerracotta
                                )
                            }

                            if (inCartQty > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Tombol Catatan Dapur / Pesanan
                                    IconButton(
                                        onClick = { itemForNoteDialog = cartItem },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(SoftCreamCard)
                                    ) {
                                        Icon(Icons.Default.EditNote, contentDescription = "Catatan", tint = DeepTerracotta, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            val idx = cart.indexOfFirst { it.product.id == product.id }
                                            if (idx != -1) {
                                                if (cart[idx].qty > 1) cart[idx] = cart[idx].copy(qty = cart[idx].qty - 1)
                                                else cart.removeAt(idx)
                                            }
                                        },
                                        modifier = Modifier.size(30.dp).clip(CircleShape).background(SoftCreamCard)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = NavyDongker, modifier = Modifier.size(16.dp))
                                    }

                                    Text("$inCartQty", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NavyDongker)

                                    IconButton(
                                        onClick = { addToCart(product) },
                                        modifier = Modifier.size(30.dp).clip(CircleShape).background(VibrantOrange)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                Surface(color = NavyDongker, shape = RoundedCornerShape(10.dp)) {
                                    Text(
                                        text = "+ Pilih",
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        if (!cartItem?.note.isNullOrBlank()) {
                            Text(
                                text = "📝 Catatan: ${cartItem?.note}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepTerracotta,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // FLOATING BAR CHECKOUT + TOMBOL PARKIR PESANAN (HOLD BILL)
        if (cart.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyDongker),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(2.dp, VibrantOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$selectedOrderType • $totalQty Item",
                            color = SoftCreamCard,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatRupiah(subtotal),
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Tombol Parkir / Simpan Pesanan Meja Sementara
                        OutlinedButton(
                            onClick = {
                                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                savedBills.add(
                                    0,
                                    SavedOrderBill(
                                        tableOrCustomer = selectedOrderType,
                                        orderType = selectedOrderType,
                                        items = cart.toList(),
                                        savedTime = time
                                    )
                                )
                                cart.clear()
                                Toast.makeText(context, "Pesanan $selectedOrderType berhasil diparkir!", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, SoftCreamCard),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = SoftCreamCard, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan Meja", color = SoftCreamCard, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showCheckoutDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BAYAR", color = PureWhite, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // DIALOG CATATAN PESANAN / DAPUR PER ITEM
    itemForNoteDialog?.let { currentItem ->
        var noteInput by remember { mutableStateOf(currentItem.note) }
        AlertDialog(
            onDismissRequest = { itemForNoteDialog = null },
            containerColor = PureWhite,
            title = { Text("Catatan Pesanan: ${currentItem.product.name}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NavyDongker) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Misal: Sedikit es, Pedas manis, Bungkus pisah") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Less Sugar", "Pedas", "Tanpa Es", "Bungkus").forEach { quickNote ->
                            SuggestionChip(
                                onClick = { noteInput = if (noteInput.isBlank()) quickNote else "$noteInput, $quickNote" },
                                label = { Text(quickNote, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = cart.indexOfFirst { it.product.id == currentItem.product.id }
                        if (idx != -1) cart[idx] = cart[idx].copy(note = noteInput)
                        itemForNoteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Text("Simpan Catatan", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForNoteDialog = null }) { Text("Batal") }
            }
        )
    }

    // DIALOG INPUT BARANG MANUAL / KALKULATOR CEPAT
    if (showCustomItemDialog) {
        var customName by remember { mutableStateOf("Item Tambahan / Jasa") }
        var customPrice by remember { mutableStateOf("10000") }
        AlertDialog(
            onDismissRequest = { showCustomItemDialog = false },
            containerColor = PureWhite,
            title = { Text("Input Barang / Nominal Bebas", fontWeight = FontWeight.ExtraBold, color = NavyDongker) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = customName, onValueChange = { customName = it }, label = { Text("Nama Barang / Keterangan") }, singleLine = true)
                    OutlinedTextField(value = customPrice, onValueChange = { customPrice = it.filter { c -> c.isDigit() } }, label = { Text("Harga (Rp)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = customPrice.toLongOrNull() ?: 0L
                        if (priceVal > 0) {
                            addToCart(PosProduct(name = customName.ifBlank { "Item Manual" }, category = "Lainnya", barcode = "-", costPrice = 0, sellPrice = priceVal, stock = 99))
                            showCustomItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Text("+ Masuk Keranjang", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomItemDialog = false }) { Text("Batal") }
            }
        )
    }

    // DIALOG DAFTAR MEJA / PESANAN YANG DIPARKIR (OPEN BILLS)
    if (showSavedBillsDialog) {
        AlertDialog(
            onDismissRequest = { showSavedBillsDialog = false },
            containerColor = PureWhite,
            title = { Text("⏸️ Daftar Pesanan Meja Aktif (${savedBills.size})", fontWeight = FontWeight.ExtraBold, color = NavyDongker) },
            text = {
                if (savedBills.isEmpty()) {
                    Text("Belum ada pesanan meja yang diparkir.", fontSize = 13.sp, color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(260.dp)) {
                        items(savedBills, key = { it.id }) { bill ->
                            val billTotal = bill.items.sumOf { it.product.getEffectivePrice(it.qty) * it.qty }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = WarmCreamBg),
                                border = BorderStroke(1.dp, VibrantOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${bill.tableOrCustomer} (Jam ${bill.savedTime})", fontWeight = FontWeight.ExtraBold, color = NavyDongker)
                                        Text("${bill.items.sumOf { it.qty }} Item • ${formatRupiah(billTotal)}", fontSize = 12.sp, color = DeepTerracotta, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            cart.clear()
                                            cart.addAll(bill.items)
                                            selectedOrderType = bill.orderType
                                            savedBills.remove(bill)
                                            showSavedBillsDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyDongker)
                                    ) {
                                        Text("Buka & Bayar", color = PureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSavedBillsDialog = false }) { Text("Tutup", color = NavyDongker, fontWeight = FontWeight.Bold) }
            }
        )
    }

    // DIALOG PEMBAYARAN KASIR LENGKAP
    if (showCheckoutDialog) {
        var discountInput by remember { mutableStateOf("0") }
        var includeTax10 by remember { mutableStateOf(false) }
        var paymentMethod by remember { mutableStateOf("Tunai") }
        var cashReceivedInput by remember { mutableStateOf(subtotal.toString()) }
        var customerName by remember { mutableStateOf("") }
        var customerPhone by remember { mutableStateOf("08") }

        val discountVal = discountInput.toLongOrNull() ?: 0L
        val afterDiscount = (subtotal - discountVal).coerceAtLeast(0L)
        val taxVal = if (includeTax10) (afterDiscount * 10) / 100 else 0L
        val grandTotal = afterDiscount + taxVal
        val paidVal = if (paymentMethod == "Kasbon") 0L else (cashReceivedInput.toLongOrNull() ?: grandTotal)
        val changeVal = (paidVal - grandTotal).coerceAtLeast(0L)

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            containerColor = PureWhite,
            title = { Text("Pembayaran ($selectedOrderType)", fontWeight = FontWeight.ExtraBold, color = NavyDongker, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = SoftCreamCard, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Tagihan Akhir:", fontSize = 11.sp, color = NavyDongker)
                            Text(formatRupiah(grandTotal), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DeepTerracotta)
                            if (paymentMethod == "Tunai") {
                                Text("Kembalian: ${formatRupiah(changeVal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldProfit)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Tunai", "QRIS", "Transfer", "Kasbon").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyDongker, selectedLabelColor = PureWhite)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Diskon (Rp)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        if (paymentMethod == "Tunai") {
                            OutlinedTextField(
                                value = cashReceivedInput,
                                onValueChange = { cashReceivedInput = it.filter { c -> c.isDigit() } },
                                label = { Text("Diterima (Rp)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    if (paymentMethod == "Tunai") {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(grandTotal to "Uang Pas", 50000L to "50 Rb", 100000L to "100 Rb").forEach { (nom, lbl) ->
                                OutlinedButton(
                                    onClick = { cashReceivedInput = nom.toString() },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDongker)
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Tambah Pajak / Layanan (10%)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Switch(checked = includeTax10, onCheckedChange = { includeTax10 = it })
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(if (paymentMethod == "Kasbon") "Nama Pelanggan (Wajib)" else "Nama Pelanggan (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (paymentMethod == "Kasbon") {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Nomor WA Pelanggan (Untuk Tagih WA)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val invNo = "INV-${System.currentTimeMillis() % 100000}"
                        val timeStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
                        val summary = cart.joinToString(", ") {
                            val notePart = if (it.note.isNotBlank()) " [${it.note}]" else ""
                            "${it.product.name} (x${it.qty})$notePart"
                        }
                        val rawProfit = cart.sumOf { (it.product.getEffectivePrice(it.qty) - it.product.costPrice) * it.qty } - discountVal

                        val receiptBtm = generateReceiptBitmap(
                            shopName = PosStorage.getShopName(context),
                            shopAddress = PosStorage.getShopAddress(context),
                            cashierName = PosStorage.getCashierName(context),
                            invoiceNo = invNo,
                            orderType = selectedOrderType,
                            timestamp = timeStr,
                            cartItems = cart.toList(),
                            discountAmount = discountVal,
                            taxAmount = taxVal,
                            grandTotal = grandTotal,
                            paidAmount = paidVal,
                            changeAmount = changeVal,
                            paymentMethod = paymentMethod,
                            customerName = customerName
                        )

                        val trx = PosTransaction(
                            invoiceNo = invNo,
                            orderType = selectedOrderType,
                            itemsSummary = summary,
                            totalAmount = grandTotal,
                            totalProfit = rawProfit.coerceAtLeast(0L),
                            paymentMethod = paymentMethod,
                            customerName = customerName.ifBlank { "Pelanggan Umum" },
                            customerPhone = customerPhone,
                            isPaid = paymentMethod != "Kasbon",
                            timestamp = timeStr
                        )

                        onCheckoutSuccess(trx, cart.toList())
                        cart.clear()
                        showCheckoutDialog = false
                        generatedReceiptPreview = Pair(receiptBtm, invNo)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Text("CETAK STRUK", color = PureWhite, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) { Text("Batal", color = NavyDongker) }
            }
        )
    }

    generatedReceiptPreview?.let { (btm, invNo) ->
        AlertDialog(
            onDismissRequest = { generatedReceiptPreview = null },
            containerColor = PureWhite,
            title = { Text("🧾 Struk Digital Siap Dikirim", fontWeight = FontWeight.ExtraBold, color = NavyDongker) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = btm.asImageBitmap(),
                        contentDescription = "Struk Belanja",
                        modifier = Modifier.fillMaxWidth().height(370.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { shareReceiptImage(context, btm, invNo) },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim ke WhatsApp", color = PureWhite, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { generatedReceiptPreview = null }) { Text("Tutup", color = NavyDongker, fontWeight = FontWeight.Bold) }
            }
        )
    }
}