package com.niteshray.xapps.billingpro.features.profile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.niteshray.xapps.billingpro.data.model.User
import com.niteshray.xapps.billingpro.features.profile.domain.UserRepository
import com.niteshray.xapps.billingpro.utils.PreferencesManager
import com.niteshray.xapps.billingpro.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val preferencesManager = PreferencesManager(application)

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    fun setupProfile(
        name: String,
        mobileNumber: String,
        storeName: String,
        gstNumber: String
    ) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _profileState.value = ProfileState(errorMessage = "User not authenticated")
            return
        }

        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)

            // Validate inputs
            if (name.isBlank() || mobileNumber.isBlank() || storeName.isBlank()) {
                _profileState.value = ProfileState(errorMessage = "Please fill in all required fields")
                return@launch
            }

            if (!ValidationUtils.isValidName(name)) {
                _profileState.value = ProfileState(errorMessage = "Please enter a valid name")
                return@launch
            }

            if (!ValidationUtils.isValidMobileNumber(mobileNumber)) {
                _profileState.value = ProfileState(errorMessage = "Please enter a valid 10-digit mobile number")
                return@launch
            }

            if (!ValidationUtils.isValidStoreName(storeName)) {
                _profileState.value = ProfileState(errorMessage = "Please enter a valid store name")
                return@launch
            }

            if (gstNumber.isNotBlank()) {
                _profileState.value = ProfileState(errorMessage = "Please enter a valid GST number")
                return@launch
            }

            try {
                val user = User(
                    userId = currentUser.uid,
                    name = name.trim(),
                    email = currentUser.email ?: "",
                    mobileNumber = ValidationUtils.formatMobileNumber(mobileNumber),
                    storeName = storeName.trim(),
                    gstNumber = ValidationUtils.formatGSTNumber(gstNumber),
                    unlocked = true,
                    profileSetupCompleted = true
                )

                val result = userRepository.saveUser(user)
                
                result.fold(
                    onSuccess = {
                        _user.value = user
                        
                        // Save to local preferences as well
                        preferencesManager.setUserName(user.name)
                        preferencesManager.setStoreName(user.storeName)
                        preferencesManager.setIsUnlocked(user.unlocked)
                        preferencesManager.setProfileSetupCompleted(true)
                        
                        _profileState.value = ProfileState(isSuccess = true)
                    },
                    onFailure = { exception ->
                        _profileState.value = ProfileState(errorMessage = getErrorMessage(exception))
                    }
                )
            } catch (e: Exception) {
                _profileState.value = ProfileState(errorMessage = getErrorMessage(e))
            }
        }
    }

    fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _profileState.value = ProfileState(errorMessage = "User not authenticated")
            return
        }

        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)

            val result = userRepository.getUser(currentUser.uid)
            
            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _profileState.value = ProfileState()
                },
                onFailure = { exception ->
                    _profileState.value = ProfileState(errorMessage = getErrorMessage(exception))
                }
            )
        }
    }

    fun checkProfileSetupStatus(): Boolean {
        val currentUser = firebaseAuth.currentUser ?: return false
        
        viewModelScope.launch {
            val result = userRepository.isProfileSetupCompleted(currentUser.uid)
            result.fold(
                onSuccess = { isCompleted ->
                    // Handle the result if needed
                },
                onFailure = { 
                    // Handle error if needed
                }
            )
        }
        
        return false // Default return - actual check happens in coroutine
    }

    fun resetState() {
        _profileState.value = ProfileState()
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when (exception.message?.lowercase()) {
            "user not found" -> "User profile not found. Please try again."
            else -> exception.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
