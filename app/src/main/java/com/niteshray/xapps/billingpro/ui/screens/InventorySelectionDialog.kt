package com.niteshray.xapps.billingpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.niteshray.xapps.billingpro.ui.theme.*
import com.niteshray.xapps.billingpro.utils.ProductUtils
import com.niteshray.xapps.billingpro.data.entity.Product
import com.niteshray.xapps.billingpro.features.ProductManagement.ui.viewmodel.ProductViewModel

data class InventorySelection(
    val product: Product,
    val requestedQuantity: Double,
    val unit: String // Extracted from product name if it contains unit info
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventorySelectionDialog(
    onDismiss: () -> Unit,
    onSelectProduct: (InventorySelection) -> Unit,
    productViewModel: ProductViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var requestedQuantity by remember { mutableStateOf("") }
    var showProductList by remember { mutableStateOf(true) }
    
    // Get all products from inventory
    val allProducts by productViewModel.products.collectAsState()
    
    // Filter products based on search query
    val filteredProducts = remember(allProducts, searchQuery) {
        if (searchQuery.isBlank()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedProduct == null) "Select from Inventory" else "Enter Quantity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
                
                if (selectedProduct == null) {
                    // Product Search and Selection Phase
                    
                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            showProductList = true
                        },
                        label = { Text("Search Products") },
                        placeholder = { Text("Type product name (e.g., Sugar, Oil)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        }
                    )
                    
                    // Products List
                    if (showProductList && searchQuery.isNotEmpty()) {
                        if (filteredProducts.isEmpty()) {
                            // No products found
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.SearchOff,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No products found",
                                            fontSize = 16.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Try a different search term",
                                            fontSize = 14.sp,
                                            color = TextSecondary.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            // Products found - show in a scrollable list
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredProducts) { product ->
                                    ProductSelectionCard(
                                        product = product,
                                        onSelect = {
                                            selectedProduct = product
                                            showProductList = false
                                        }
                                    )
                                }
                            }
                        }
                    } else if (searchQuery.isEmpty()) {
                        // Show instructions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Inventory,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Select from Your Inventory",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PrimaryBlue,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Type a product name to search your inventory",
                                        fontSize = 14.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Quantity Entry Phase
                    
                    // Selected Product Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = selectedProduct!!.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Price: ${ProductUtils.formatPrice(selectedProduct!!.price)} • Available: ${selectedProduct!!.quantity}",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            IconButton(
                                onClick = { 
                                    selectedProduct = null 
                                    showProductList = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Change Product",
                                    tint = SecondaryTeal
                                )
                            }
                        }
                    }
                    
                    // Extract unit from product name if available
                    val extractedUnit = remember(selectedProduct) {
                        selectedProduct?.name?.let { name ->
                            when {
                                name.contains("(per kg)", ignoreCase = true) -> "kg"
                                name.contains("(per piece)", ignoreCase = true) -> "piece" 
                                name.contains("(per liter)", ignoreCase = true) -> "liter"
                                name.contains("(per gram)", ignoreCase = true) -> "gram"
                                name.contains("(per packet)", ignoreCase = true) -> "packet"
                                name.contains("(per box)", ignoreCase = true) -> "box"
                                name.contains("(per bottle)", ignoreCase = true) -> "bottle"
                                else -> "piece" // Default unit
                            }
                        } ?: "piece"
                    }
                    
                    // Quantity Input
                    OutlinedTextField(
                        value = requestedQuantity,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                requestedQuantity = it
                            }
                        },
                        label = { Text("Quantity ($extractedUnit)") },
                        placeholder = { Text("e.g., 2.5, 10, 0.5") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Numbers,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    )
                    
                    // Total calculation preview
                    if (requestedQuantity.isNotEmpty() && selectedProduct != null) {
                        val quantity = requestedQuantity.toDoubleOrNull() ?: 0.0
                        val total = selectedProduct!!.price * quantity
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Amount:",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${ProductUtils.formatPrice(selectedProduct!!.price)} × $quantity $extractedUnit",
                                        fontSize = 12.sp,
                                        color = TextSecondary.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = ProductUtils.formatPrice(total),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                    
                    // Stock Warning
                    if (requestedQuantity.isNotEmpty() && selectedProduct != null) {
                        val quantity = requestedQuantity.toDoubleOrNull() ?: 0.0
                        if (quantity > selectedProduct!!.quantity) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
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
                                        text = "Insufficient stock! Available: ${selectedProduct!!.quantity} $extractedUnit",
                                        fontSize = 14.sp,
                                        color = ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    if (selectedProduct != null) {
                        Button(
                            onClick = {
                                val quantity = requestedQuantity.toDoubleOrNull()
                                if (quantity != null && quantity > 0 && quantity <= selectedProduct!!.quantity) {
                                    val extractedUnit = selectedProduct!!.name.let { name ->
                                        when {
                                            name.contains("(per kg)", ignoreCase = true) -> "kg"
                                            name.contains("(per piece)", ignoreCase = true) -> "piece" 
                                            name.contains("(per liter)", ignoreCase = true) -> "liter"
                                            name.contains("(per gram)", ignoreCase = true) -> "gram"
                                            name.contains("(per packet)", ignoreCase = true) -> "packet"
                                            name.contains("(per box)", ignoreCase = true) -> "box"
                                            name.contains("(per bottle)", ignoreCase = true) -> "bottle"
                                            else -> "piece"
                                        }
                                    }
                                    
                                    onSelectProduct(
                                        InventorySelection(
                                            product = selectedProduct!!,
                                            requestedQuantity = quantity,
                                            unit = extractedUnit
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = {
                                val quantity = requestedQuantity.toDoubleOrNull()
                                quantity != null && quantity > 0 && selectedProduct != null && quantity <= selectedProduct!!.quantity
                            }()
                        ) {
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductSelectionCard(
    product: Product,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Inventory,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = ProductUtils.formatPrice(product.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Stock: ${product.quantity}",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Select Icon
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Select",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
