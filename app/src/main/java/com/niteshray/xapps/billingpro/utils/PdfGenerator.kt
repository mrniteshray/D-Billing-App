package com.niteshray.xapps.billingpro.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.niteshray.xapps.billingpro.data.entity.Bill
import com.niteshray.xapps.billingpro.data.entity.BillItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator {
    
    companion object {
        
        fun generateBillPdf(
            context: Context,
            bill: Bill,
            billItems: List<BillItem>,
            onPdfGenerated: (File) -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
                val page = pdfDocument.startPage(pageInfo)
                
                var canvas = page.canvas
                val paint = Paint()
                
                var yPosition = 50f
                val leftMargin = 50f
                val rightMargin = 545f
                
                // Title
                paint.textSize = 24f
                paint.isFakeBoldText = true
                canvas.drawText("BILLING PRO", leftMargin, yPosition, paint)
                yPosition += 40f
                
                // Horizontal line
                paint.strokeWidth = 2f
                canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
                yPosition += 30f
                
                // Bill details
                paint.textSize = 16f
                paint.isFakeBoldText = false
                
                canvas.drawText("Bill ID: ${bill.billId.take(8)}", leftMargin, yPosition, paint)
                yPosition += 25f
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                canvas.drawText("Date: ${dateFormat.format(Date(bill.createdAt))}", leftMargin, yPosition, paint)
                yPosition += 25f
                
                canvas.drawText("Customer: ${bill.customerName}", leftMargin, yPosition, paint)
                yPosition += 25f
                
                if (bill.customerPhone.isNotEmpty()) {
                    canvas.drawText("Phone: ${bill.customerPhone}", leftMargin, yPosition, paint)
                    yPosition += 25f
                }
                
                yPosition += 20f
                
                // Table header
                paint.isFakeBoldText = true
                canvas.drawText("Item", leftMargin, yPosition, paint)
                canvas.drawText("Price", 250f, yPosition, paint)
                canvas.drawText("Qty", 350f, yPosition, paint)
                canvas.drawText("Total", 450f, yPosition, paint)
                yPosition += 5f
                
                // Header line
                canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
                yPosition += 20f
                
                // Bill items
                paint.isFakeBoldText = false
                var totalAmount = 0.0
                
                for (item in billItems) {
                    if (yPosition > 750f) { // Start new page if needed
                        pdfDocument.finishPage(page)
                        val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                        val newPage = pdfDocument.startPage(newPageInfo)
                        canvas = newPage.canvas
                        yPosition = 50f
                    }
                    
                    val itemName = if (item.productName.length > 20) {
                        item.productName.take(17) + "..."
                    } else {
                        item.productName
                    }
                    
                    canvas.drawText(itemName, leftMargin, yPosition, paint)
                    canvas.drawText("₹${String.format("%.2f", item.productPrice)}", 250f, yPosition, paint)
                    canvas.drawText("${item.quantity}", 350f, yPosition, paint)
                    canvas.drawText("₹${String.format("%.2f", item.totalPrice)}", 450f, yPosition, paint)
                    
                    totalAmount += item.totalPrice
                    yPosition += 25f
                }
                
                yPosition += 10f
                
                // Total line
                paint.strokeWidth = 1f
                canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
                yPosition += 20f
                
                // Total amount
                paint.textSize = 18f
                paint.isFakeBoldText = true
                canvas.drawText("Total Items: ${bill.totalItems}", leftMargin, yPosition, paint)
                canvas.drawText("Total Amount: ₹${String.format("%.2f", bill.totalAmount)}", 300f, yPosition, paint)
                yPosition += 40f
                
                // Footer
                paint.textSize = 12f
                paint.isFakeBoldText = false
                canvas.drawText("Thank you for your business!", leftMargin, yPosition, paint)
                yPosition += 20f
                canvas.drawText("Generated by Billing Pro App", leftMargin, yPosition, paint)
                
                pdfDocument.finishPage(page)
                
                // Save PDF
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                
                val fileName = "Bill_${bill.billId.take(8)}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
                val file = File(downloadsDir, fileName)
                
                val fileOutputStream = FileOutputStream(file)
                pdfDocument.writeTo(fileOutputStream)
                fileOutputStream.close()
                pdfDocument.close()
                
                onPdfGenerated(file)
                
            } catch (e: Exception) {
                onError("Failed to generate PDF: ${e.message}")
            }
        }
        
        fun sharePdf(context: Context, file: File) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Bill from Billing Pro")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(Intent.createChooser(intent, "Share Bill PDF"))
            } catch (e: Exception) {
                // Handle error
            }
        }
        
        fun openPdf(context: Context, file: File) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle error - PDF viewer not available
            }
        }
    }
}
