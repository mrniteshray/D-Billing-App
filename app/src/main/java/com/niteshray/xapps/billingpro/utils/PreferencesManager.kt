package com.niteshray.xapps.billingpro.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "billing_pro_prefs"
        private const val KEY_PROFILE_SETUP_COMPLETED = "profile_setup_completed"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_STORE_NAME = "store_name"
        private const val KEY_IS_UNLOCKED = "is_unlocked"
    }
    
    fun setProfileSetupCompleted(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_PROFILE_SETUP_COMPLETED, completed)
            .apply()
    }
    
    fun isProfileSetupCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_PROFILE_SETUP_COMPLETED, false)
    }
    
    fun setUserName(name: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_NAME, name)
            .apply()
    }
    
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    }
    
    fun setStoreName(storeName: String) {
        sharedPreferences.edit()
            .putString(KEY_STORE_NAME, storeName)
            .apply()
    }
    
    fun getStoreName(): String {
        return sharedPreferences.getString(KEY_STORE_NAME, "") ?: ""
    }
    
    fun setIsUnlocked(unlocked: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_UNLOCKED, unlocked)
            .apply()
    }
    
    fun isUnlocked(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_UNLOCKED, true)
    }
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
