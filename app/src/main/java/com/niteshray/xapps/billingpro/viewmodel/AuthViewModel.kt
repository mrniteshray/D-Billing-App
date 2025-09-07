package com.niteshray.xapps.billingpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niteshray.xapps.billingpro.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signUp(email: String, password: String, fullName: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            
            // Validate inputs
            if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                _authState.value = AuthState(errorMessage = "Please fill in all required fields")
                return@launch
            }

            if (password.length < 6) {
                _authState.value = AuthState(errorMessage = "Password must be at least 6 characters")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState(errorMessage = "Please enter a valid email address")
                return@launch
            }

            val result = authRepository.signUpWithEmailAndPassword(email, password)
            
            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { exception ->
                    _authState.value = AuthState(errorMessage = getErrorMessage(exception))
                }
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState(errorMessage = "Please enter email and password")
                return@launch
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState(errorMessage = "Please enter a valid email address")
                return@launch
            }

            val result = authRepository.signInWithEmailAndPassword(email, password)
            
            result.fold(
                onSuccess = {
                    _authState.value = AuthState(isSuccess = true)
                },
                onFailure = { exception ->
                    _authState.value = AuthState(errorMessage = getErrorMessage(exception))
                }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState()
    }

    fun isUserSignedIn(): Boolean {
        return authRepository.isUserSignedIn()
    }

    fun signOut() {
        authRepository.signOut()
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when (exception.message) {
            "The email address is already in use by another account." -> "This email is already registered. Please sign in instead."
            "The password is invalid or the user does not have a password." -> "Invalid email or password. Please try again."
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "No account found with this email. Please sign up first."
            "The email address is badly formatted." -> "Please enter a valid email address."
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Network error. Please check your connection and try again."
            else -> exception.message ?: "An unexpected error occurred. Please try again."
        }
    }
}
