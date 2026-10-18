package com.example.bambupos

import android.content.Context
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

// ============================================================================
// 1. PALET WARNA: NAVY DONGKER, VIBRANT ORANGE & WARM CREAM
// ============================================================================
val NavyDongker = Color(0xFF0F172A)
val NavySurface = Color(0xFF1E293B)
val VibrantOrange = Color(0xFFF97316)
val DeepTerracotta = Color(0xFFEA580C)
val WarmCreamBg = Color(0xFFFFFBEB)
val SoftCreamCard = Color(0xFFFEF3C7)
val CreamBorder = Color(0xFFFDE68A)
val PureWhite = Color(0xFFFFFFFF)
val EmeraldProfit = Color(0xFF10B981)
val CrimsonAlert = Color(0xFFEF4444)

val BambuPosColorScheme = lightColorScheme(
    primary = VibrantOrange,
    onPrimary = PureWhite,
    primaryContainer = SoftCreamCard,
    onPrimaryContainer = NavyDongker,
    secondary = NavyDongker,
    onSecondary = PureWhite,
    secondaryContainer = SoftCreamCard,
    onSecondaryContainer = NavyDongker,
    background = WarmCreamBg,
    onBackground = NavyDongker,
    surface = PureWhite,
    onSurface = NavyDongker
)

// ============================================================================
// 2. MODEL DATA LENGKAP (PRODUK GROSIR, KERANJANG CATATAN, PARKIR MEJA, BIAYA)
// ============================================================================
data class PosProduct(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val category: String,
    val barcode: String,
    val costPrice: Long,          // Harga Modal (HPP)
    val sellPrice: Long,          // Harga Jual Eceran
    val stock: Int,
    val wholesaleMinQty: Int = 0, // Minimal beli untuk dapat harga grosir (0 = tidak aktif)
    val wholesalePrice: Long = 0L // Harga satuan grosir
) {
    fun getEffectivePrice(qty: Int): Long {
        return if (wholesaleMinQty > 0 && qty >= wholesaleMinQty && wholesalePrice > 0) {
            wholesalePrice
        } else {
            sellPrice
        }
    }
}

data class CartItem(
    val product: PosProduct,
    val qty: Int,
    val note: String = "" // Catatan khusus: "Less sugar", "Pedas", dll.
)

// Model Pesanan Diparkir / Open Bill Meja
data class SavedOrderBill(
    val id: Long = System.currentTimeMillis(),
    val tableOrCustomer: String,
    val orderType: String,
    val items: List<CartItem>,
    val savedTime: String
)

data class PosTransaction(
    val id: Long = System.currentTimeMillis(),
    val invoiceNo: String,
    val orderType: String, // Dine-In Meja, Bungkus, Delivery
    val itemsSummary: String,
    val totalAmount: Long,
    val totalProfit: Long,
    val paymentMethod: String,
    val customerName: String,
    val customerPhone: String,
    val isPaid: Boolean,
    val timestamp: String
)

// Model Pengeluaran Operasional Toko (Listrik, Es Batu, Plastik, Gaji, dll.)
data class StoreExpense(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val category: String,
    val amount: Long,
    val timestamp: String
)

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(amount)}"
}

// ============================================================================
// 3. PENYIMPANAN PERMANEN LOKAL (SHAREDPREFERENCES JSON)
// ============================================================================
object PosStorage {
    private const val PREF_NAME = "bambupos_super_storage"

    fun saveShopInfo(context: Context, name: String, address: String, cashierName: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putString("shop_name", name)
            .putString("shop_address", address)
            .putString("cashier_name", cashierName)
            .apply()
    }

    fun getShopName(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("shop_name", "PANDAKODE MART & CAFE") ?: "PANDAKODE MART & CAFE"

    fun getShopAddress(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("shop_address", "Jl. Umban Sari, Pekanbaru • WA: 0812-xxxx") ?: ""

    fun getCashierName(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("cashier_name", "Kasir Utama") ?: "Kasir Utama"

    fun saveProducts(context: Context, list: List<PosProduct>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("category", p.category)
                put("barcode", p.barcode)
                put("costPrice", p.costPrice)
                put("sellPrice", p.sellPrice)
                put("stock", p.stock)
                put("wholesaleMinQty", p.wholesaleMinQty)
                put("wholesalePrice", p.wholesalePrice)
            })
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString("products_json", arr.toString()).apply()
    }

    fun loadProducts(context: Context): List<PosProduct> {
        val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("products_json", null)
        if (raw.isNullOrEmpty()) {
            return listOf(
                PosProduct(1, "Kopi Susu Aren Panda", "Minuman", "8991001", 8000, 15000, 45, 5, 13000),
                PosProduct(2, "Roti Bakar Coklat Keju", "Makanan", "8991002", 7000, 14000, 30, 5, 12500),
                PosProduct(3, "Matcha Latte Dingin", "Minuman", "8991003", 9000, 18000, 25, 0, 0),
                PosProduct(4, "Kentang Goreng Crispy", "Snack", "8991004", 6000, 12000, 4, 0, 0),
                PosProduct(5, "Kabel Data Type-C Fast", "Aksesoris", "8991005", 12000, 25000, 15, 3, 20000),
                PosProduct(6, "Jasa Instalasi / Servis PC", "Jasa", "8991006", 0, 50000, 999, 0, 0)
            )
        }
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                PosProduct(
                    id = o.optLong("id"),
                    name = o.optString("name"),
                    category = o.optString("category", "Umum"),
                    barcode = o.optString("barcode", ""),
                    costPrice = o.optLong("costPrice", 0),
                    sellPrice = o.optLong("sellPrice", 0),
                    stock = o.optInt("stock", 0),
                    wholesaleMinQty = o.optInt("wholesaleMinQty", 0),
                    wholesalePrice = o.optLong("wholesalePrice", 0L)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveTransactions(context: Context, list: List<PosTransaction>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("invoiceNo", t.invoiceNo)
                put("orderType", t.orderType)
                put("itemsSummary", t.itemsSummary)
                put("totalAmount", t.totalAmount)
                put("totalProfit", t.totalProfit)
                put("paymentMethod", t.paymentMethod)
                put("customerName", t.customerName)
                put("customerPhone", t.customerPhone)
                put("isPaid", t.isPaid)
                put("timestamp", t.timestamp)
            })
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString("transactions_json", arr.toString()).apply()
    }

    fun loadTransactions(context: Context): List<PosTransaction> {
        val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("transactions_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                PosTransaction(
                    id = o.optLong("id"),
                    invoiceNo = o.optString("invoiceNo"),
                    orderType = o.optString("orderType", "Bungkus"),
                    itemsSummary = o.optString("itemsSummary"),
                    totalAmount = o.optLong("totalAmount"),
                    totalProfit = o.optLong("totalProfit"),
                    paymentMethod = o.optString("paymentMethod"),
                    customerName = o.optString("customerName"),
                    customerPhone = o.optString("customerPhone"),
                    isPaid = o.optBoolean("isPaid", true),
                    timestamp = o.optString("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveExpenses(context: Context, list: List<StoreExpense>) {
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("category", e.category)
                put("amount", e.amount)
                put("timestamp", e.timestamp)
            })
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString("expenses_json", arr.toString()).apply()
    }

    fun loadExpenses(context: Context): List<StoreExpense> {
        val raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString("expenses_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                StoreExpense(
                    id = o.optLong("id"),
                    title = o.optString("title"),
                    category = o.optString("category", "Operasional"),
                    amount = o.optLong("amount"),
                    timestamp = o.optString("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}