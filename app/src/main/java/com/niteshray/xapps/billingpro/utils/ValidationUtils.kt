package com.niteshray.xapps.billingpro.utils

import java.util.regex.Pattern

object ValidationUtils {
    
    fun isValidMobileNumber(mobileNumber: String): Boolean {
        // Indian mobile number validation (10 digits starting with 6, 7, 8, or 9)
        val pattern = Pattern.compile("^[6-9]\\d{9}$")
        return pattern.matcher(mobileNumber.trim()).matches()
    }
    
    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2 && name.trim().all { it.isLetter() || it.isWhitespace() }
    }
    
    fun isValidStoreName(storeName: String): Boolean {
        return storeName.trim().length >= 2
    }
    
    fun formatMobileNumber(mobileNumber: String): String {
        val cleaned = mobileNumber.replace(Regex("[^\\d]"), "")
        return if (cleaned.length == 10) cleaned else mobileNumber
    }
    
    fun formatGSTNumber(gstNumber: String): String {
        return gstNumber.trim().uppercase()
    }
}
