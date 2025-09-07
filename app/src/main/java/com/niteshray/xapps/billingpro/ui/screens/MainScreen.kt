package com.niteshray.xapps.billingpro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.niteshray.xapps.billingpro.ui.theme.*
import com.niteshray.xapps.billingpro.utils.ProductUtils
import com.niteshray.xapps.billingpro.features.ProductManagement.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToProducts: () -> Unit = {},
    onLogout: () -> Unit = {},
    productViewModel: ProductViewModel = viewModel()
) {
    // Get current user name
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.email?.substringBefore("@") ?: "User"
    
    // Get inventory data
    val productCount by productViewModel.productCount.collectAsState()
    val totalInventoryValue by productViewModel.totalInventoryValue.collectAsState()
    val lowStockProducts by productViewModel.lowStockProducts.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    
    // Menu state
    var showMenu by remember { mutableStateOf(false) }

    // Refresh data when screen loads
    LaunchedEffect(Unit) {
        if (productViewModel.isUserAuthenticated()) {
            productViewModel.onUserLoggedIn()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "BillingPro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    // Three dots menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        
                        // Dropdown menu
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ExitToApp,
                                            contentDescription = null,
                                            tint = ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Logout",
                                            color = ErrorRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    // Sign out from Firebase
                                    FirebaseAuth.getInstance().signOut()
                                    // Clear user data
                                    productViewModel.onUserLoggedOut()
                                    // Navigate to sign in
                                    onLogout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)

        ) {
            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(6.dp)
            )

            // Inventory Overview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Inventory Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Total Products
                            InventoryStatItem(
                                icon = Icons.Filled.Inventory,
                                label = "Total Products",
                                value = productCount.toString(),
                                color = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Total Value
                            InventoryStatItem(
                                icon = Icons.Filled.CurrencyRupee,
                                label = "Total Value",
                                value = ProductUtils.formatPrice(totalInventoryValue),
                                color = SecondaryTeal,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Low Stock
                            InventoryStatItem(
                                icon = Icons.Filled.TrendingDown,
                                label = "Low Stock",
                                value = lowStockProducts.size.toString(),
                                color = if (lowStockProducts.isNotEmpty()) WarningOrange else SecondaryTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Add Products Button
                        ElevatedButton(
                            onClick = onNavigateToProducts,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Manage Products",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Recent Bills",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(12.dp)
            )

        }
    }
}

@Composable
fun InventoryStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(32.dp)
                .padding(bottom = 8.dp)
        )
        
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}