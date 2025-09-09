package com.niteshray.xapps.billingpro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.niteshray.xapps.billingpro.ui.theme.*
import com.niteshray.xapps.billingpro.utils.ProductUtils
import com.niteshray.xapps.billingpro.data.entity.Product
import com.niteshray.xapps.billingpro.features.ProductManagement.ui.viewmodel.ProductViewModel

data class ManualProduct(
    val name: String,
    val price: Double,
    val unit: String, // "kg", "piece", "liter", etc.
    val quantity: Double,
    val saveToInventory: Boolean = true, // Always true for inventory
    val inventoryStock: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualProductEntryDialog(
    onDismiss: () -> Unit,
    onAddProduct: (ManualProduct) -> Unit,
    productViewModel: ProductViewModel? = null, // For saving to inventory
    isInventoryDialog: Boolean = false // New parameter to determine dialog type
) {
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("kg") }
    var expanded by remember { mutableStateOf(false) }
    var saveToInventory by remember { mutableStateOf(isInventoryDialog) } // Auto-set based on dialog type
    var inventoryStock by remember { mutableStateOf("") }
    
    val units = listOf("kg", "piece", "liter", "gram", "packet", "box", "bottle")
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Smaller height for inventory
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp), // Reduced padding
                verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced spacing
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isInventoryDialog) "Add Inventory Item" else "Add Manual Item",
                        fontSize = 18.sp, // Reduced font size
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
                
                // Product Name
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name") },
                    placeholder = { Text("e.g., Sugar, Maida, Oil") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Inventory,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )
                
                // Price per unit
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            productPrice = it
                        }
                    },
                    label = { Text("Price per $selectedUnit") },
                    placeholder = { Text("e.g., 45.50") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.CurrencyRupee,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )
                
                // Unit Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Unit") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Scale,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    selectedUnit = unit
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Quantity
                OutlinedTextField(
                    value = productQuantity,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            productQuantity = it
                        }
                    },
                    label = { Text("Quantity ($selectedUnit)") },
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
                if (productPrice.isNotEmpty() && productQuantity.isNotEmpty()) {
                    val price = productPrice.toDoubleOrNull() ?: 0.0
                    val quantity = productQuantity.toDoubleOrNull() ?: 0.0
                    val total = price * quantity
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount:",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = ProductUtils.formatPrice(total),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryTeal
                            )
                        }
                    }
                }
                
                // Inventory Management Section (only show for billing, not for inventory dialog)
                if (productViewModel != null && !isInventoryDialog) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp) // Reduced padding
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = saveToInventory,
                                    onCheckedChange = { saveToInventory = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryBlue
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Save to Inventory",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Add this item to your product inventory for future use",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            
                            // Inventory Stock Input (only show if save to inventory is checked)
                            if (saveToInventory) {
                                Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing
                                OutlinedTextField(
                                    value = inventoryStock,
                                    onValueChange = { 
                                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            inventoryStock = it
                                        }
                                    },
                                    label = { Text("Total Stock in Inventory ($selectedUnit)") },
                                    placeholder = { Text("e.g., 50, 100.5") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        focusedLabelColor = PrimaryBlue
                                    ),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Inventory2,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    }
                                )
                                
                                Text(
                                    text = "This will create a new product in your inventory that you can reuse for future billing.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                // For inventory dialog, show stock quantity field instead
                if (isInventoryDialog) {
                    OutlinedTextField(
                        value = inventoryStock,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                inventoryStock = it
                            }
                        },
                        label = { Text("Available Stock ($selectedUnit)") },
                        placeholder = { Text("e.g., 50, 100.5") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Inventory2,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    )
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
                    
                    Button(
                        onClick = {
                            val price = productPrice.toDoubleOrNull()
                            val quantity = productQuantity.toDoubleOrNull()
                            val stock = if (isInventoryDialog) inventoryStock.toDoubleOrNull() else (if (saveToInventory) inventoryStock.toDoubleOrNull() else quantity)
                            
                            // Validation
                            val isValidBasic = productName.isNotBlank() && price != null && quantity != null && 
                                             price > 0 && quantity > 0
                            val isValidInventory = if (isInventoryDialog) {
                                stock != null && stock > 0
                            } else {
                                !saveToInventory || (saveToInventory && stock != null && stock > 0)
                            }
                            
                            if (isValidBasic && isValidInventory) {
                                onAddProduct(
                                    ManualProduct(
                                        name = productName.trim(),
                                        price = price!!,
                                        unit = selectedUnit,
                                        quantity = quantity!!,
                                        saveToInventory = isInventoryDialog || saveToInventory,
                                        inventoryStock = stock ?: quantity!!
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
                            val basicValid = productName.isNotBlank() && 
                                           productPrice.toDoubleOrNull()?.let { it > 0 } == true &&
                                           productQuantity.toDoubleOrNull()?.let { it > 0 } == true
                            val inventoryValid = if (isInventoryDialog) {
                                inventoryStock.toDoubleOrNull()?.let { it > 0 } == true
                            } else {
                                !saveToInventory || (saveToInventory && inventoryStock.toDoubleOrNull()?.let { it > 0 } == true)
                            }
                            basicValid && inventoryValid
                        }()
                    ) {
                        Text(
                            if (isInventoryDialog) "Add to Inventory" 
                            else if (saveToInventory) "Add to Cart & Inventory" 
                            else "Add to Cart"
                        )
                    }
                }
            }
        }
    }
}
