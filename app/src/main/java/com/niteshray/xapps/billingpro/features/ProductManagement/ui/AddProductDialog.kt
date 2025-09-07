package com.niteshray.xapps.billingpro.features.ProductManagement.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAddProduct: (name: String, price: Double, quantity: Int, productId: String?) -> Unit,
    initialProductId: String? = null
) {
    var productId by remember { mutableStateOf(initialProductId ?: "") }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productQuantity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (initialProductId != null) "Add Scanned Product" else "Add New Product",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                // Show scanner indicator if product was scanned
                if (initialProductId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SecondaryTeal.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Product ID scanned",
                                fontSize = 12.sp,
                                color = SecondaryTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Product ID Field (Optional)
                OutlinedTextField(
                    value = productId,
                    onValueChange = { productId = it },
                    label = { 
                        Text(if (initialProductId != null) "Product ID (Scanned)" else "Product ID (Optional)") 
                    },
                    placeholder = { 
                        Text(if (initialProductId != null) "Scanned from barcode" else "Enter custom ID or leave blank for auto-generation") 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (initialProductId != null) SecondaryTeal else PrimaryBlue,
                        focusedLabelColor = if (initialProductId != null) SecondaryTeal else PrimaryBlue,
                        cursorColor = if (initialProductId != null) SecondaryTeal else PrimaryBlue
                    ),
                    singleLine = true,
                    readOnly = initialProductId != null, // Make read-only if scanned
                    leadingIcon = if (initialProductId != null) {
                        {
                            Icon(
                                imageVector = Icons.Filled.QrCode,
                                contentDescription = "Scanned",
                                tint = SecondaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else null
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                // Price Field
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = { Text("Price") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quantity Field
                OutlinedTextField(
                    value = productQuantity,
                    onValueChange = { productQuantity = it },
                    label = { Text("Available Stock") },
                    placeholder = { Text("0") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                // Error message
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            color = ErrorRed,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
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
                            // Validate inputs
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
                                            onAddProduct(productName, price, quantity, productId.takeIf { it.isNotBlank() })
                                            onDismiss()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue
                        )
                    ) {
                        Text("Add Product")
                    }
                }
            }
        }
    }
}
