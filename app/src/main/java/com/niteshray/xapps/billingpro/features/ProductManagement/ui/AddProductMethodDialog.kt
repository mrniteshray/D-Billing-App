package com.niteshray.xapps.billingpro.features.ProductManagement.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.niteshray.xapps.billingpro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductMethodDialog(
    onDismiss: () -> Unit,
    onScannerSelected: () -> Unit,
    onManualSelected: () -> Unit
) {
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add New Product",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Scanner Option
                AddMethodCard(
                    icon = Icons.Filled.CameraAlt,
                    title = "Scan Barcode",
                    description = "Use camera to scan product barcode",
                    onClick = {
                        onScannerSelected()
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Manual Option
                AddMethodCard(
                    icon = Icons.Filled.Edit,
                    title = "Manual Entry",
                    description = "Enter product details manually",
                    onClick = {
                        onManualSelected()
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Cancel Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun AddMethodCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Arrow Icon
//            Icon(
//                imageVector = Icons.Filled.ArrowForward,
//                contentDescription = null,
//                tint = TextSecondary,
//                modifier = Modifier.size(20.dp)
//            )
        }
    }
}