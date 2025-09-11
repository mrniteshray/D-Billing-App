package com.niteshray.xapps.billingpro.features.ProductManagement.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.niteshray.xapps.billingpro.data.entity.Product
import com.niteshray.xapps.billingpro.ui.theme.*
import com.niteshray.xapps.billingpro.utils.ProductUtils
import com.niteshray.xapps.billingpro.features.ProductManagement.ui.viewmodel.ProductViewModel
import com.niteshray.xapps.billingpro.ui.screens.ManualProductEntryDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    productViewModel: ProductViewModel = viewModel()
) {
    // Simple individual states instead of complex UiState
    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()
    val searchQuery by productViewModel.searchQuery.collectAsState()
    
    // Context for file operations
    val context = LocalContext.current
    
    // Dialog states
    var showAddMethodDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf(false) }
    var showManualInventoryDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var scannedProductId by remember { mutableStateOf<String?>(null) }
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { productViewModel.importProductsFromJson(context, it) }
    }
    
    // Share launcher for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle export result if needed
    }
    
    // Check if user is authenticated
    if (!productViewModel.isUserAuthenticated()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Please sign in to access your inventory",
                fontSize = 16.sp,
                color = TextSecondary
            )
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Inventory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    // Import Button
                    IconButton(
                        onClick = { importLauncher.launch("application/json") }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileDownload,
                            contentDescription = "Import Products",
                            tint = Color.White
                        )
                    }
                    
                    // Export Button
                    IconButton(
                        onClick = {
                            val uri = productViewModel.exportProductsToJson(context)
                            uri?.let {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM, it)
                                    type = "application/json"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                val chooser = Intent.createChooser(shareIntent, "Export Products")
                                context.startActivity(chooser)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileUpload,
                            contentDescription = "Export Products",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMethodDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Product"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar (keeping this as you liked it)
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { productViewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Error Message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = ErrorRed,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Content based on loading state
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading your inventory...",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (products.isEmpty()) {
                // Empty state in center
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (searchQuery.isNotEmpty()) {
                        SearchEmptyStateCard(
                            searchQuery = searchQuery,
                            onClearSearch = { productViewModel.updateSearchQuery("") }
                        )
                    } else {
                        EmptyStateCard(
                            onAddProduct = { showAddMethodDialog = true }
                        )
                    }
                }
            } else {
                // Search Results Header (if searching)
                if (searchQuery.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PrimaryBlue.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Found ${products.size} product${if (products.size != 1) "s" else ""} for \"$searchQuery\"",
                                fontSize = 14.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Products List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)// Space for FAB
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { 
                                productToEdit = product
                                showEditProductDialog = true
                            },
                            onDelete = { 
                                productViewModel.deleteProduct(product.productId)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add Method Selection Dialog
    if (showAddMethodDialog) {
        AddProductMethodDialog(
            onDismiss = { showAddMethodDialog = false },
            onScannerSelected = {
                showAddMethodDialog = false
                showBarcodeScanner = true
            },
            onManualSelected = {
                showAddMethodDialog = false
                showAddProductDialog = true
            },
            onManualInventorySelected = {
                showAddMethodDialog = false
                showManualInventoryDialog = true
            }
        )
    }
    
    // Barcode Scanner
    if (showBarcodeScanner) {
        BarcodeScannerScreen(
            onBarcodeScanned = { barcodeValue ->
                scannedProductId = barcodeValue
                showBarcodeScanner = false
                showAddProductDialog = true
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }
    
    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { 
                showAddProductDialog = false
                scannedProductId = null
            },
            onAddProduct = { name, price, quantity, customProductId ->
                val finalProductId = customProductId ?: scannedProductId
                productViewModel.addProduct(name, price, quantity, finalProductId)
                showAddProductDialog = false
                scannedProductId = null
            },
            initialProductId = scannedProductId
        )
    }
    
    // Edit Product Dialog
    if (showEditProductDialog && productToEdit != null) {
        EditProductDialog(
            product = productToEdit!!,
            onDismiss = { 
                showEditProductDialog = false
                productToEdit = null
            },
            onUpdateProduct = { updatedProduct ->
                productViewModel.updateProduct(updatedProduct)
                showEditProductDialog = false
                productToEdit = null
            }
        )
    }
    
    // Manual Inventory Entry Dialog
    if (showManualInventoryDialog) {
        ManualProductEntryDialog(
            onDismiss = { showManualInventoryDialog = false },
            productViewModel = productViewModel,
            isInventoryDialog = true, // This is for inventory management
            onAddProduct = { manualProduct ->
                // Create inventory product directly (force inventory save)
                val productId = "BULK_${System.currentTimeMillis()}_${manualProduct.name.hashCode()}"
                val productName = "${manualProduct.name} (per ${manualProduct.unit})"
                val productPrice = manualProduct.price
                val productQuantity = if (manualProduct.saveToInventory) manualProduct.inventoryStock.toInt() else manualProduct.quantity.toInt()
                
                productViewModel.addProduct(productName, productPrice, productQuantity, productId)
                showManualInventoryDialog = false
            }
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Product Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "ID: ${product.productId.take(8)}...",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Stock Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (product.quantity <= 10) WarningOrange.copy(alpha = 0.2f) else SecondaryTeal.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (product.quantity <= 10) "Low Stock" else "In Stock",
                        fontSize = 12.sp,
                        color = if (product.quantity <= 10) WarningOrange else SecondaryTeal,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Product Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Price
                Column {
                    Text(
                        text = "Price",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = ProductUtils.formatPrice(product.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                
                // Quantity
                Column {
                    Text(
                        text = "Quantity",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${product.quantity} units",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                // Total Value
                Column {
                    Text(
                        text = "Total Value",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = ProductUtils.formatPrice(product.price * product.quantity),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    onAddProduct: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Inventory,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Products Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = "Start building your inventory by adding your first product",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddProduct,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Product")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onUpdateProduct: (Product) -> Unit
) {
    var productName by remember { mutableStateOf(product.name) }
    var productPrice by remember { mutableStateOf(product.price.toString()) }
    var productQuantity by remember { mutableStateOf(product.quantity.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    text = "Edit Product",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Product Name Field
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name") },
                    placeholder = { Text("Enter product name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Product Price Field
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = { Text("Price") },
                    placeholder = { Text("Enter price") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Product Quantity Field
                OutlinedTextField(
                    value = productQuantity,
                    onValueChange = { productQuantity = it },
                    label = { Text("Quantity") },
                    placeholder = { Text("Enter quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            when {
                                productName.isBlank() -> {
                                    errorMessage = "Product name is required"
                                }
                                else -> {
                                    val price = productPrice.toDoubleOrNull()
                                    val quantity = productQuantity.toIntOrNull()
                                    
                                    when {
                                        price == null || !ProductUtils.isValidPrice(price) -> {
                                            errorMessage = "Please enter a valid price greater than 0"
                                        }
                                        quantity == null || !ProductUtils.isValidQuantity(quantity) -> {
                                            errorMessage = "Please enter a valid quantity (0 or greater)"
                                        }
                                        else -> {
                                            val updatedProduct = product.copy(
                                                name = productName,
                                                price = price,
                                                quantity = quantity,
                                                updatedAt = System.currentTimeMillis()
                                            )
                                            onUpdateProduct(updatedProduct)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Text("Update Product")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchEmptyStateCard(
    searchQuery: String,
    onClearSearch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Products Found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = "No products match \"$searchQuery\"",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = onClearSearch,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Search")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isActive by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search products...",
                    color = Color.Gray.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = if (isActive || searchQuery.isNotEmpty()) PrimaryBlue else Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear search",
                            tint = Color.Gray.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue.copy(alpha = 0.4f),
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.15f),
                cursorColor = PrimaryBlue,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.87f)
            ),
            interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        when (interaction) {
                            is FocusInteraction.Focus -> isActive = true
                            is FocusInteraction.Unfocus -> isActive = false
                        }
                    }
                }
            }
        )
    }
}
