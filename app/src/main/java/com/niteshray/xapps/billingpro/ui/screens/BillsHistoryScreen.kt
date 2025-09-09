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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.niteshray.xapps.billingpro.data.entity.Bill
import com.niteshray.xapps.billingpro.ui.theme.*
import com.niteshray.xapps.billingpro.utils.ProductUtils
import com.niteshray.xapps.billingpro.utils.PdfGenerator
import com.niteshray.xapps.billingpro.features.billing.ui.BillViewModel
import com.niteshray.xapps.billingpro.data.database.BillingProDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsHistoryScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = BillingProDatabase.getDatabase(context)
    val billViewModel = BillViewModel(database.billDao())
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: "unknown"
    
    val bills by billViewModel.getAllBills(userId).collectAsState(initial = emptyList())
    val billCount by billViewModel.getBillCount(userId).collectAsState(initial = 0)
    val totalSales by billViewModel.getTotalSales(userId).collectAsState(initial = 0.0)
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bills History",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Bills Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$billCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Total Bills",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Total Sales Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CurrencyRupee,
                            contentDescription = null,
                            tint = SecondaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ProductUtils.formatPrice(totalSales ?: 0.0),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Total Sales",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bills List
            if (bills.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Bills Yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Generated bills will appear here",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bills) { bill ->
                        BillHistoryCard(
                            bill = bill,
                            onViewPdf = { selectedBill ->
                                // Generate and open PDF using coroutine
                                scope.launch {
                                    try {
                                        val billItems = billViewModel.getBillItems(selectedBill.billId)
                                        PdfGenerator.generateBillPdf(
                                            context = context,
                                            bill = selectedBill,
                                            billItems = billItems,
                                            onPdfGenerated = { file ->
                                                PdfGenerator.openPdf(context, file)
                                            },
                                            onError = { error ->
                                                // Show error toast
                                            }
                                        )
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }
                            },
                            onDelete = { selectedBill ->
                                billViewModel.deleteBill(selectedBill.billId)
                                Toast.makeText(
                                    context,
                                    "Bill deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillHistoryCard(
    bill: Bill,
    onViewPdf: (Bill) -> Unit,
    onDelete: (Bill) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
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
            // Top row: Bill ID and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bill #${bill.billId.take(8)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Bill",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Middle row: Customer info and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side: Customer info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = bill.customerName,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (bill.customerPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = bill.customerPhone,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Right side: Amount info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = ProductUtils.formatPrice(bill.totalAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal
                    )
                    Text(
                        text = "${bill.totalItems} items",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom row: Date and View PDF Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(bill.createdAt),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                
                OutlinedButton(
                    onClick = { onViewPdf(bill) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View PDF", fontSize = 12.sp)
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Bill",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this bill? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(bill)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
