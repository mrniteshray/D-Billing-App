package com.niteshray.xapps.billingpro.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class WhatsAppShareHelper {

    companion object {

        /**
         * Direct WhatsApp file sharing with specific contact
         */
        fun shareToWhatsApp(
            context: Context,
            file: File,
            phoneNumber: String = "",
            message: String = "Hi! Here's your bill from our store. Thank you for shopping with us!",
            onSuccess: () -> Unit = {},
            onError: (String) -> Unit = {}
        ) {
            try {
                // Get file URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val cleanPhone = cleanPhoneNumber(phoneNumber)

                if (cleanPhone.isNotEmpty()) {
                    // Try direct WhatsApp intent with contact
                    shareToSpecificContact(context, uri, file, cleanPhone, message, onSuccess, onError)
                } else {
                    // General WhatsApp sharing
                    shareToWhatsAppGeneral(context, uri, file, message, onSuccess, onError)
                }

            } catch (e: Exception) {
                onError("Error sharing to WhatsApp: ${e.message}")
            }
        }

        /**
         * Share directly to specific WhatsApp contact
         */
        private fun shareToSpecificContact(
            context: Context,
            uri: Uri,
            file: File,
            phoneNumber: String,
            message: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                // Method 1: Try WhatsApp intent with jid
                val whatsAppIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = getFileType(file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra("jid", "${phoneNumber}@s.whatsapp.net") // WhatsApp specific
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Check if WhatsApp can handle this
                val resolveInfo = context.packageManager.resolveActivity(
                    whatsAppIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

                if (resolveInfo != null) {
                    context.startActivity(whatsAppIntent)
                    onSuccess()
                    return
                }

                // Method 2: Alternative WhatsApp sharing with phone number
                val alternativeIntent = Intent().apply {
                    action = "android.intent.action.MAIN"
                    setPackage("com.whatsapp")
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra("android.intent.extra.STREAM", uri)
                    putExtra("jid", PhoneNumberUtils.stripSeparators("$phoneNumber@s.whatsapp.net"))
                    type = getFileType(file)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(alternativeIntent)
                onSuccess()

            } catch (e: Exception) {
                // Fallback: Show user instructions
                showWhatsAppInstructions(context, uri, file, phoneNumber, onSuccess, onError)
            }
        }

        /**
         * Fallback method - Opens WhatsApp with instructions
         */
        private fun showWhatsAppInstructions(
            context: Context,
            uri: Uri,
            file: File,
            phoneNumber: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                // Step 1: Copy file to easy location or show share dialog
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = getFileType(file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Bill PDF is attached")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Filter to show only WhatsApp
                val packageManager = context.packageManager
                val activities = packageManager.queryIntentActivities(shareIntent, 0)
                val whatsAppActivities = activities.filter {
                    it.activityInfo.packageName.contains("whatsapp")
                }

                if (whatsAppActivities.isNotEmpty()) {
                    // Create custom chooser showing only WhatsApp options
                    val targetedIntents = whatsAppActivities.map { activity ->
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            type = getFileType(file)
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "Bill PDF - Please find attached")
                            setPackage(activity.activityInfo.packageName)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }.toTypedArray()

                    val chooser = Intent.createChooser(targetedIntents[0], "Share Bill PDF")
                    if (targetedIntents.size > 1) {
                        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntents.sliceArray(1 until targetedIntents.size))
                    }

                    context.startActivity(chooser)
                    onSuccess()

                    // Also show instructions
                    Toast.makeText(
                        context,
                        "Select contact: +$phoneNumber to send the bill",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    onError("WhatsApp not found")
                }

            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }

        /**
         * General WhatsApp sharing (most reliable)
         */
        private fun shareToWhatsAppGeneral(
            context: Context,
            uri: Uri,
            file: File,
            message: String,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                val whatsAppIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = getFileType(file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(whatsAppIntent)
                onSuccess()

            } catch (e: Exception) {
                onError("Unable to open WhatsApp: ${e.message}")
            }
        }

        /**
         * Most reliable method - Direct WhatsApp with file
         */
        fun directWhatsAppShare(
            context: Context,
            file: File,
            phoneNumber: String = "",
            onSuccess: () -> Unit = {},
            onError: (String) -> Unit = {}
        ) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                // Create intent for WhatsApp
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = getFileType(file)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Check if WhatsApp is available
                val packageManager = context.packageManager
                val activities = packageManager.queryIntentActivities(intent, 0)

                val whatsAppPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
                val availableWhatsApp = activities.find {
                    whatsAppPackages.contains(it.activityInfo.packageName)
                }

                if (availableWhatsApp != null) {
                    intent.setPackage(availableWhatsApp.activityInfo.packageName)
                    context.startActivity(intent)
                    onSuccess()

                    // Show helpful message
                    val cleanPhone = cleanPhoneNumber(phoneNumber)
                    if (cleanPhone.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            "Select contact: +$cleanPhone to send the bill",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    onError("WhatsApp is not installed")
                }

            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }

        // Helper Functions
        private fun cleanPhoneNumber(phone: String): String {
            return phone.replace(Regex("[^0-9]"), "").let {
                when {
                    it.startsWith("91") && it.length == 12 -> it
                    it.length == 10 -> "91$it"
                    it.length > 10 && it.startsWith("0") -> "91${it.substring(1)}"
                    else -> it
                }
            }
        }

        private fun getFileType(file: File): String {
            return when (file.extension.lowercase()) {
                "pdf" -> "application/pdf"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "txt" -> "text/plain"
                "doc", "docx" -> "application/msword"
                else -> "application/*"
            }
        }
    }
}


