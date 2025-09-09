package com.niteshray.xapps.billingpro.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.niteshray.xapps.billingpro.data.entity.Product
import com.niteshray.xapps.billingpro.ui.theme.*
import com.niteshray.xapps.billingpro.utils.ProductUtils
import com.niteshray.xapps.billingpro.utils.PdfGenerator
import com.niteshray.xapps.billingpro.features.ProductManagement.ui.viewmodel.ProductViewModel
import com.niteshray.xapps.billingpro.features.billing.ui.BillViewModel
import com.niteshray.xapps.billingpro.data.database.BillingProDatabase
import com.google.firebase.auth.FirebaseAuth

data class CartItem(
    val product: Product,
    val quantity: Int = 1
)

// Track inventory selections for proper inventory updates
data class InventoryUpdate(
    val originalProductId: String,
    val requestedQuantity: Double,
    val cartItemId: String // To link with cart items
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    customerName: String,
    customerPhone: String,
    onBack: () -> Unit = {},
    productViewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val database = BillingProDatabase.getDatabase(context)
    val billViewModel = BillViewModel(database.billDao())
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: "unknown"
    val coroutineScope = rememberCoroutineScope()
    
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var inventoryUpdates by remember { mutableStateOf<List<InventoryUpdate>>(emptyList()) } // Track inventory to update
    var isProcessing by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var showInventorySelectionDialog by remember { mutableStateOf(false) }
    var shareToWhatsApp by remember { mutableStateOf(false) } // Track if user wants WhatsApp sharing
    
    // Collect products from viewmodel
    val allProducts by productViewModel.products.collectAsState()
    
    // Load products when screen opens
    LaunchedEffect(Unit) {
        if (productViewModel.isUserAuthenticated()) {
            productViewModel.onUserLoggedIn()
        }
    }
    
    // Calculate total
    val totalAmount = cartItems.sumOf { it.product.price * it.quantity }
    
    // Function to handle bill generation
    fun generateBill() {
        // Prepare cart data for bill saving
        val cartItemsData = cartItems.map { it.product.productId to it.quantity }
        val productDetails = cartItems.associate {
            it.product.productId to (it.product.name to it.product.price)
        }

        // Save bill to database
        billViewModel.saveBill(
            userId = userId,
            customerName = customerName,
            customerPhone = customerPhone,
            cartItems = cartItemsData,
            productDetails = productDetails
        ) { billId ->
            // Update inventory quantities based on tracked inventory updates
            inventoryUpdates.forEach { update ->
                coroutineScope.launch {
                    // Get current product from inventory
                    val currentProduct = productViewModel.getProductById(update.originalProductId)
                    if (currentProduct != null) {
                        val newQuantity = (currentProduct.quantity - update.requestedQuantity).toInt()
                        if (newQuantity >= 0) {
                            productViewModel.updateProductQuantity(
                                update.originalProductId,
                                newQuantity
                            )
                        }
                    }
                }
            }

            // Also handle regular barcode scanned items (existing logic)
            cartItems.forEach { cartItem ->
                // Only update if it's a regular product (not virtual cart item)
                if (!cartItem.product.productId.startsWith("CART_") &&
                    !cartItem.product.productId.startsWith("MANUAL_")) {
                    val newQuantity = cartItem.product.quantity - cartItem.quantity
                    if (newQuantity >= 0) {
                        productViewModel.updateProductQuantity(
                            cartItem.product.productId,
                            newQuantity
                        )
                    }
                }
            }

            // Generate PDF using coroutine
            coroutineScope.launch {
                try {
                    val bill = billViewModel.getBillById(billId)
                    val billItems = billViewModel.getBillItems(billId)

                    if (bill != null && billItems.isNotEmpty()) {
                        PdfGenerator.generateBillPdf(
                            context = context,
                            bill = bill,
                            billItems = billItems,
                            onPdfGenerated = { file ->
                                isProcessing = false
                                PdfGenerator.openPdf(context, file)

//                                if (shareToWhatsApp && customerPhone.isNotBlank()) {
//                                    // Share to WhatsApp
//
//                                    Toast.makeText(
//                                        context,
//                                        "Bill generated and WhatsApp opened for sharing!",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                } else {
//                                    // Just open PDF normally
//                                    PdfGenerator.openPdf(context, file)
//
//                                    Toast.makeText(
//                                        context,
//                                        "Bill saved and PDF generated! File: ${file.name}",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }

                                // Reset and go back
                                shareToWhatsApp = false
                                cartItems = emptyList()
                                inventoryUpdates = emptyList()
                                onBack()
                            },
                            onError = { error ->
                                isProcessing = false
                                Toast.makeText(
                                    context,
                                    "Bill saved but PDF generation failed: $error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } else {
                        isProcessing = false
                        Toast.makeText(
                            context,
                            "Bill saved but failed to retrieve bill data for PDF",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    isProcessing = false
                    Toast.makeText(
                        context,
                        "Error generating PDF: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Billing - $customerName",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (customerPhone.isNotBlank()) {
                            Text(
                                text = customerPhone,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Clear Cart
                    IconButton(
                        onClick = { cartItems = emptyList() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear Cart",
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Manual Entry FAB - Now opens inventory selection
                FloatingActionButton(
                    onClick = { showInventorySelectionDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Select from Inventory"
                    )
                }
                
                // Barcode Scanner FAB
                ExtendedFloatingActionButton(
                    onClick = { showBarcodeScanner = true },
                    containerColor = SecondaryTeal,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan Barcode"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Product")
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
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
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = ProductUtils.formatPrice(totalAmount),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryTeal
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Receipt,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate Bill",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
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
            if (cartItems.isEmpty()) {
                // Empty Cart State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = TextSecondary.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Cart is Empty",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Text(
                            text = "Scan barcodes or add items manually",
                            fontSize = 16.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showBarcodeScanner = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecondaryTeal
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Barcode")
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Cart Items (${cartItems.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp) // Space for bottom bar
                ) {
                    items(cartItems) { cartItem ->
                        CartItemCard(
                            cartItem = cartItem,
                            onQuantityChange = { newQuantity ->
                                // Validate quantity against available stock
                                val validQuantity = if (newQuantity > cartItem.product.quantity) {
                                    Toast.makeText(context, "Not enough stock available!", Toast.LENGTH_SHORT).show()
                                    cartItem.quantity // Keep current quantity
                                } else {
                                    newQuantity
                                }
                                
                                cartItems = cartItems.map { item ->
                                    if (item.product.productId == cartItem.product.productId) {
                                        item.copy(quantity = validQuantity)
                                    } else {
                                        item
                                    }
                                }.filter { it.quantity > 0 }
                            },
                            onRemove = {
                                // Remove corresponding inventory update if it exists
                                inventoryUpdates = inventoryUpdates.filterNot { 
                                    it.cartItemId == cartItem.product.productId 
                                }
                                
                                // Remove from cart
                                cartItems = cartItems.filter { 
                                    it.product.productId != cartItem.product.productId 
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Barcode Scanner
    if (showBarcodeScanner) {
        ContinuousBarcodeScannerScreen(
            onBarcodeScanned = { barcodeValue ->
                if (!isProcessing) {
                    isProcessing = true
                    
                    // Find product by productId (which is the scanned barcode)
                    val scannedProduct = allProducts.find { 
                        it.productId == barcodeValue
                    }
                    
                    if (scannedProduct != null) {
                        // Check if product has enough stock
                        val existingItem = cartItems.find { 
                            it.product.productId == scannedProduct.productId 
                        }
                        val currentCartQuantity = existingItem?.quantity ?: 0
                        
                        if (currentCartQuantity < scannedProduct.quantity) {
                            // Add to cart
                            cartItems = if (existingItem != null) {
                                cartItems.map { item ->
                                    if (item.product.productId == scannedProduct.productId) {
                                        item.copy(quantity = item.quantity + 1)
                                    } else {
                                        item
                                    }
                                }
                            } else {
                                cartItems + CartItem(scannedProduct, 1)
                            }
                            
                            // Show success toast
                            Toast.makeText(
                                context,
                                "Product added to cart!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Not enough stock
                            Toast.makeText(
                                context,
                                "Not enough stock available!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        
                        // Reset processing after delay using coroutine
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            delay(2000)
                            isProcessing = false
                        }
                    } else {
                        // Product not found
                        Toast.makeText(
                            context,
                            "Product not found in inventory!",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Reset processing immediately for not found
                        isProcessing = false
                    }
                }
            },
            onDismiss = { showBarcodeScanner = false }
        )
    }
    
    // Confirm Bill Dialog
    if (showConfirmDialog) {
        BillConfirmationDialog(
            customerName = customerName,
            customerPhone = customerPhone,
            cartItems = cartItems,
            totalAmount = totalAmount,
            isProcessing = isProcessing,
            onConfirm = {
                showConfirmDialog = false
                shareToWhatsApp = false // Regular bill generation
                isProcessing = true
                generateBill()
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Inventory Selection Dialog (for existing products)
    if (showInventorySelectionDialog) {
        InventorySelectionDialog(
            onDismiss = { showInventorySelectionDialog = false },
            productViewModel = productViewModel,
            onSelectProduct = { inventorySelection ->
                // Generate a unique cart item ID that includes the original product ID
                val cartItemId = "CART_${inventorySelection.product.productId}_${System.currentTimeMillis()}"
                
                // Add selected product to cart
                val cartItem = CartItem(
                    product = Product(
                        productId = cartItemId,
                        name = "${inventorySelection.product.name} (${inventorySelection.requestedQuantity} ${inventorySelection.unit})",
                        price = inventorySelection.product.price * inventorySelection.requestedQuantity, // Total price for this quantity
                        quantity = 1, // Always 1 since we calculated total price above
                        userId = userId
                    ),
                    quantity = 1
                )
                
                // Track inventory update for later with cart item reference
                val inventoryUpdate = InventoryUpdate(
                    originalProductId = inventorySelection.product.productId,
                    requestedQuantity = inventorySelection.requestedQuantity,
                    cartItemId = cartItemId
                )
                
                // Add to cart and inventory tracking
                cartItems = cartItems + cartItem
                inventoryUpdates = inventoryUpdates + inventoryUpdate
                showInventorySelectionDialog = false
                
                Toast.makeText(
                    context,
                    "${inventorySelection.product.name} (${inventorySelection.requestedQuantity} ${inventorySelection.unit}) added to cart!",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Note: Inventory will be updated when bill is generated
            }
        )
    }
    
    // Manual Product Entry Dialog (for creating new products)
    if (showManualEntryDialog) {
        ManualProductEntryDialog(
            onDismiss = { showManualEntryDialog = false },
            productViewModel = productViewModel,
            isInventoryDialog = false, // This is for billing, not inventory
            onAddProduct = { manualProduct ->
                // Save to inventory if requested
                // Create inventory product directly (force inventory save)
                val productId = "BULK_${System.currentTimeMillis()}_${manualProduct.name.hashCode()}"
                val productName = "${manualProduct.name} (per ${manualProduct.unit})"
                val productPrice = manualProduct.price
                val productQuantity = if (manualProduct.saveToInventory) manualProduct.inventoryStock.toInt() else manualProduct.quantity.toInt()

                productViewModel.addProduct(productName, productPrice, productQuantity, productId)
                    
                    Toast.makeText(
                        context,
                        "${manualProduct.name} added to inventory!",
                        Toast.LENGTH_SHORT
                    ).show()
                
                // Create a virtual CartItem for manual product
                val manualCartItem = CartItem(
                    product = Product(
                        productId = "MANUAL_${System.currentTimeMillis()}", // Unique ID
                        name = "${manualProduct.name} (${manualProduct.quantity} ${manualProduct.unit})",
                        price = manualProduct.price * manualProduct.quantity, // Total price
                        quantity = 1, // Always 1 for manual items since we calculate total
                        userId = userId
                    ),
                    quantity = 1 // Always 1 since price is already calculated
                )
                
                // Add to cart
                cartItems = cartItems + manualCartItem
                showManualEntryDialog = false
                
                Toast.makeText(
                    context, 
                    "${manualProduct.name} added to cart!", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = cartItem.product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = ProductUtils.formatPrice(cartItem.product.price),
                        fontSize = 14.sp,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease",
                            tint = PrimaryBlue
                        )
                    }
                    
                    Text(
                        text = cartItem.quantity.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase",
                            tint = PrimaryBlue
                        )
                    }
                }
                
                // Total Price
                Text(
                    text = ProductUtils.formatPrice(cartItem.product.price * cartItem.quantity),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryTeal
                )
            }
        }
    }
}

@Composable
fun BillConfirmationDialog(
    customerName: String,
    customerPhone: String,
    cartItems: List<CartItem>,
    totalAmount: Double,
    isProcessing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (isProcessing) { {} } else onDismiss,
        title = {
            Text(
                text = if (isProcessing) "Processing Bill..." else "Confirm Bill",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Saving bill and generating PDF...",
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Text(
                    text = "Customer: $customerName",
                    color = TextPrimary
                )
                if (customerPhone.isNotBlank()) {
                    Text(
                        text = "Phone: $customerPhone",
                        color = TextSecondary
                    )
                }
                Text(
                    text = "Items: ${cartItems.size}",
                    color = TextSecondary
                )
                Text(
                    text = "Total: ${ProductUtils.formatPrice(totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = SecondaryTeal
                )
            }
        },
        confirmButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Regular Generate Button
                Button(
                    onClick = onConfirm,
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Bill Only")
                    }
                }
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    )
}
