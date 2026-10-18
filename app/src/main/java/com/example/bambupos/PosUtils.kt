package com.example.bambupos

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream

// ============================================================================
// 1. GENERATOR STRUK DIGITAL LENGKAP (DENGAN TIPE ORDER, CATATAN & GROSIR)
// ============================================================================
fun generateReceiptBitmap(
    shopName: String,
    shopAddress: String,
    cashierName: String,
    invoiceNo: String,
    orderType: String,
    timestamp: String,
    cartItems: List<CartItem>,
    discountAmount: Long,
    taxAmount: Long,
    grandTotal: Long,
    paidAmount: Long,
    changeAmount: Long,
    paymentMethod: String,
    customerName: String
): Bitmap {
    val width = 620
    val extraNotesHeight = cartItems.count { it.note.isNotBlank() } * 24
    val itemBlockHeight = (cartItems.size * 60) + extraNotesHeight
    val totalHeight = 730 + itemBlockHeight

    val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.parseColor("#FFFBEB"))

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Header Navy Dongker & Pita Oranye
    paint.color = Color.parseColor("#0F172A")
    canvas.drawRoundRect(RectF(20f, 20f, (width - 20).toFloat(), 150f), 20f, 20f, paint)
    paint.color = Color.parseColor("#F97316")
    canvas.drawRoundRect(RectF(40f, 140f, (width - 40).toFloat(), 158f), 8f, 8f, paint)

    paint.color = Color.WHITE
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 25f
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText(shopName.uppercase(), width / 2f, 70f, paint)

    paint.color = Color.parseColor("#FDE68A")
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textSize = 14f
    canvas.drawText(shopAddress, width / 2f, 100f, paint)
    canvas.drawText("Kasir: $cashierName • Mode: $orderType", width / 2f, 122f, paint)

    var y = 195f
    paint.color = Color.parseColor("#0F172A")
    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 15f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Nota: $invoiceNo", 40f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText(timestamp, (width - 40).toFloat(), y, paint)

    y += 24f
    paint.textAlign = Paint.Align.LEFT
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Pelanggan: ${customerName.ifBlank { "Umum" }}", 40f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Bayar: $paymentMethod", (width - 40).toFloat(), y, paint)

    y += 20f
    val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#94A3B8")
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
    }
    canvas.drawLine(40f, y, (width - 40).toFloat(), y, dashPaint)

    y += 30f
    cartItems.forEach { item ->
        val unitPrice = item.product.getEffectivePrice(item.qty)
        val isWholesale = unitPrice < item.product.sellPrice

        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16.5f
        paint.color = Color.parseColor("#0F172A")
        val titleText = if (isWholesale) "${item.product.name} (Grosir)" else item.product.name
        canvas.drawText(titleText, 40f, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14.5f
        paint.color = Color.parseColor("#475569")
        canvas.drawText("${item.qty} x ${formatRupiah(unitPrice)}", 40f, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText(formatRupiah(item.qty * unitPrice), (width - 40).toFloat(), y, paint)

        if (item.note.isNotBlank()) {
            y += 20f
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 13f
            paint.color = Color.parseColor("#EA580C")
            canvas.drawText("  *Catatan: ${item.note}", 40f, y, paint)
        }
        y += 30f
    }

    canvas.drawLine(40f, y, (width - 40).toFloat(), y, dashPaint)
    y += 26f

    if (discountAmount > 0) {
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 15f
        paint.color = Color.parseColor("#0F172A")
        canvas.drawText("Potongan Diskon", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("- ${formatRupiah(discountAmount)}", (width - 40).toFloat(), y, paint)
        y += 24f
    }
    if (taxAmount > 0) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 15f
        canvas.drawText("Pajak / Layanan (10%)", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("+ ${formatRupiah(taxAmount)}", (width - 40).toFloat(), y, paint)
        y += 24f
    }

    val totalBox = RectF(36f, y, (width - 36).toFloat(), y + 54f)
    paint.style = Paint.Style.FILL
    paint.color = Color.parseColor("#FEF3C7")
    canvas.drawRoundRect(totalBox, 12f, 12f, paint)
    paint.style = Paint.Style.STROKE
    paint.color = Color.parseColor("#F97316")
    paint.strokeWidth = 2.5f
    canvas.drawRoundRect(totalBox, 12f, 12f, paint)

    paint.style = Paint.Style.FILL
    paint.color = Color.parseColor("#0F172A")
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 19f
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("TOTAL TAGIHAN", 54f, y + 34f, paint)
    paint.textAlign = Paint.Align.RIGHT
    paint.color = Color.parseColor("#EA580C")
    canvas.drawText(formatRupiah(grandTotal), (width - 54).toFloat(), y + 34f, paint)

    y += 76f
    paint.color = Color.parseColor("#0F172A")
    paint.textSize = 15f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("Diterima ($paymentMethod)", 40f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText(formatRupiah(paidAmount), (width - 40).toFloat(), y, paint)

    y += 24f
    paint.textAlign = Paint.Align.LEFT
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Kembalian", 40f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText(formatRupiah(changeAmount), (width - 40).toFloat(), y, paint)

    y += 24f
    val qrBtm = generatePosQrBitmap("BAMBUPOS|$invoiceNo|TOTAL:$grandTotal|$timestamp")
    if (qrBtm != null) {
        val qrLeft = (width - 135) / 2f
        canvas.drawBitmap(qrBtm, null, RectF(qrLeft, y, qrLeft + 135f, y + 135f), null)
        y += 150f
    }

    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 14f
    paint.color = Color.parseColor("#475569")
    canvas.drawText("Terima Kasih Atas Kunjungan Anda!", width / 2f, y, paint)
    paint.textSize = 12f
    canvas.drawText("Powered by BambuPOS Super Suite • PandaKode", width / 2f, y + 20f, paint)

    return bitmap
}

// ============================================================================
// 2. FITUR BARU: CETAK STIKER LABEL HARGA RAK TOKO & BARCODE 1D
// ============================================================================
fun generateShelfPriceTagBitmap(product: PosProduct, shopName: String): Bitmap? {
    return try {
        val width = 550
        val height = 320
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#FFFBEB"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Bingkai Oranye & Header Navy Dongker
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#F97316")
        paint.strokeWidth = 8f
        canvas.drawRoundRect(RectF(8f, 8f, (width - 8).toFloat(), (height - 8).toFloat()), 20f, 20f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#0F172A")
        canvas.drawRoundRect(RectF(8f, 8f, (width - 8).toFloat(), 68f), 16f, 16f, paint)

        paint.color = Color.parseColor("#FDE68A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("$shopName • ${product.category.uppercase()}", width / 2f, 44f, paint)

        // Nama Barang & Harga Besar
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 24f
        canvas.drawText(product.name.take(24), width / 2f, 108f, paint)

        paint.color = Color.parseColor("#EA580C")
        paint.textSize = 36f
        canvas.drawText(formatRupiah(product.sellPrice), width / 2f, 155f, paint)

        if (product.wholesaleMinQty > 0 && product.wholesalePrice > 0) {
            paint.color = Color.parseColor("#10B981")
            paint.textSize = 15f
            canvas.drawText(
                "Grosir (Beli min. ${product.wholesaleMinQty}): ${formatRupiah(product.wholesalePrice)} /pcs",
                width / 2f,
                182f,
                paint
            )
        }

        // Barcode Garis Code-128 di Bawah Label Rak
        val codeText = product.barcode.ifBlank { "8990001" }
        val bitMatrix = MultiFormatWriter().encode(codeText, BarcodeFormat.CODE_128, 380, 65)
        val startX = (width - 380) / 2
        val startY = 198
        val fg = Color.parseColor("#0F172A")
        for (x in 0 until 380) {
            for (y in 0 until 65) {
                if (bitMatrix[x, y]) {
                    bitmap.setPixel(startX + x, startY + y, fg)
                }
            }
        }

        paint.color = fg
        paint.textSize = 15f
        canvas.drawText(codeText, width / 2f, 286f, paint)
        bitmap
    } catch (e: Exception) {
        null
    }
}

fun generatePosQrBitmap(content: String): Bitmap? {
    return try {
        val size = 220
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val btm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val fg = Color.parseColor("#0F172A")
        val bg = Color.parseColor("#FFFBEB")
        for (x in 0 until size) {
            for (y in 0 until size) {
                btm.setPixel(x, y, if (matrix[x, y]) fg else bg)
            }
        }
        btm
    } catch (e: Exception) {
        null
    }
}

fun shareReceiptImage(context: Context, bitmap: Bitmap, title: String) {
    try {
        val dir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val file = File(dir, "BambuPOS_${title}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Dicetak menggunakan BambuPOS Super Suite by PandaKode 🐼🧾")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Gambar ke..."))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// ============================================================================
// 3. FITUR BARU: EXPORT LAPORAN KEUANGAN KE FILE EXCEL (.CSV)
// ============================================================================
fun exportTransactionsToCsv(context: Context, transactions: List<PosTransaction>, expenses: List<StoreExpense>) {
    try {
        val dir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val csvFile = File(dir, "Laporan_BambuPOS_${System.currentTimeMillis() % 10000}.csv")
        val sb = StringBuilder()
        sb.append("No Nota,Waktu,Tipe Pesanan,Pelanggan,Metode Bayar,Status,Total Omzet (Rp),Laba Kotor (Rp),Rincian Barang\n")
        transactions.forEach { t ->
            val status = if (t.isPaid) "LUNAS" else "KASBON"
            val cleanItems = t.itemsSummary.replace(",", ";")
            sb.append("${t.invoiceNo},${t.timestamp},${t.orderType},${t.customerName},${t.paymentMethod},$status,${t.totalAmount},${t.totalProfit},\"$cleanItems\"\n")
        }
        sb.append("\nDAFTAR BIAYA OPERASIONAL TOKO\n")
        sb.append("Waktu,Kategori,Keterangan Biaya,Nominal (Rp)\n")
        expenses.forEach { e ->
            sb.append("${e.timestamp},${e.category},\"${e.title}\",${e.amount}\n")
        }

        csvFile.writeText(sb.toString())
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", csvFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan BambuPOS (Excel CSV)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Kirim File Excel (.CSV) ke..."))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal mengekspor CSV: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun sendWhatsAppDebtReminder(context: Context, phone: String, customerName: String, invoiceNo: String, amount: Long) {
    try {
        val cleanPhone = phone.replace("[^0-9]".toRegex(), "").let {
            if (it.startsWith("08")) "62" + it.substring(1) else it
        }
        val msg = "Halo Kak *$customerName* 🙏,\nSekadar mengingatkan dengan sopan untuk tagihan/kasbon belanja nomor nota *$invoiceNo* sebesar *${formatRupiah(amount)}*.\n\nTerima kasih banyak atas kerja samanya! 🐼✨"
        val url = "https://wa.me/$cleanPhone?text=${Uri.encode(msg)}"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
    }
}