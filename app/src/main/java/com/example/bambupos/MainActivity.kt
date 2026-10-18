package com.example.bambupos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
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
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = VibrantOrange,
                            border = BorderStroke(2.dp, SoftCreamCard),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🐼", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("BambuPOS", color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = VibrantOrange, shape = RoundedCornerShape(6.dp)) {
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
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(VibrantOrange))
            }
        },
        bottomBar = {
            NavigationBar(containerColor = PureWhite, tonalElevation = 10.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
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
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
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
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
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