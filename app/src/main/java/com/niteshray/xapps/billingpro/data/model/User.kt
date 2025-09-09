package com.niteshray.xapps.billingpro.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val storeName: String = "",
    val gstNumber: String = "",
    val isUnlocked: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val profileSetupCompleted: Boolean = false
) {
    // Empty constructor for Firestore
    constructor() : this("")
}
