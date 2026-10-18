package com.example.bambupos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = BambuPosColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BambuPosMainApp()
                }
            }
        }
    }
}

@Composable
fun BambuPosMainApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Memuat data produk, transaksi, dan pengeluaran operasional dari penyimpanan lokal
    val products = remember {
        mutableStateListOf<PosProduct>().apply { addAll(PosStorage.loadProducts(context)) }
    }
    val transactions = remember {
        mutableStateListOf<PosTransaction>().apply { addAll(PosStorage.loadTransactions(context)) }
    }
    val expenses = remember {
        mutableStateListOf<StoreExpense>().apply { addAll(PosStorage.loadExpenses(context)) }
    }
    val cart = remember { mutableStateListOf<CartItem>() }
    val savedBills = remember { mutableStateListOf<SavedOrderBill>() }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(NavyDongker, NavySurface)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // KOTAK LOGO BAMBUPOS BERLATAR CREAM (#FFFBEB) & BINGKAI ORANYE
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = WarmCreamBg, // #FFFBEB
                            border = BorderStroke(2.dp, VibrantOrange),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_bambupos),
                                    contentDescription = "Logo BambuPOS",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BambuPOS",
                                    color = PureWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = VibrantOrange,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SUPER UMKM",
                                        color = PureWhite,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${PosStorage.getShopName(context)} • ${PosStorage.getCashierName(context)}",
                                color = SoftCreamCard,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Indikator Jumlah Nota Transaksi
                    Surface(
                        color = NavySurface,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, VibrantOrange)
                    ) {
                        Text(
                            text = "${transactions.size} Nota",
                            color = SoftCreamCard,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                // Garis Aksen Oranye di Bawah Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(VibrantOrange)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 10.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Kasir") },
                    label = { Text("Kasir (${cart.sumOf { it.qty }})", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = DeepTerracotta,
                        indicatorColor = VibrantOrange
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Gudang & Rak") },
                    label = { Text("Gudang & Rak", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = NavyDongker,
                        indicatorColor = NavyDongker
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Laba & Biaya") },
                    label = { Text("Laba & Biaya", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PureWhite,
                        selectedTextColor = DeepTerracotta,
                        indicatorColor = VibrantOrange
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> PosCashierScreen(
                    products = products,
                    cart = cart,
                    savedBills = savedBills,
                    onCheckoutSuccess = { newTrx, soldItems ->
                        // Kurangi stok barang otomatis setelah terjual
                        soldItems.forEach { cartItem ->
                            val idx = products.indexOfFirst { it.id == cartItem.product.id }
                            if (idx != -1) {
                                val old = products[idx]
                                products[idx] = old.copy(stock = (old.stock - cartItem.qty).coerceAtLeast(0))
                            }
                        }
                        PosStorage.saveProducts(context, products)
                        transactions.add(0, newTrx)
                        PosStorage.saveTransactions(context, transactions)
                    }
                )
                1 -> PosInventoryScreen(
                    products = products,
                    onProductsChanged = { PosStorage.saveProducts(context, products) }
                )
                2 -> PosReportAndDebtScreen(
                    transactions = transactions,
                    expenses = expenses,
                    onTransactionsChanged = { PosStorage.saveTransactions(context, transactions) },
                    onExpensesChanged = { PosStorage.saveExpenses(context, expenses) }
                )
            }
        }
    }
}