package com.niteshray.xapps.billingpro.utils

object ProductUtils {
    
    /**
     * Validates price
     */
    fun isValidPrice(price: Double): Boolean {
        return price > 0
    }
    
    /**
     * Validates quantity
     */
    fun isValidQuantity(quantity: Int): Boolean {
        return quantity >= 0
    }
    
    /**
     * Formats price to display
     */
    fun formatPrice(price: Double): String {
        return String.format("₹%.2f", price)
    }

}
