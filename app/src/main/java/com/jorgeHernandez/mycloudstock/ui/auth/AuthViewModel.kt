package com.jorgeHernandez.mycloudstock.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorgeHernandez.mycloudstock.data.AuthRepository
import com.jorgeHernandez.mycloudstock.data.FirebaseAuthRepository
import com.jorgeHernandez.mycloudstock.domain.AuthValidator
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.currentUserEmail != null
    )

    val userEmail: String
        get() = repository.currentUserEmail ?: "Unknown user"

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }
    }

    fun clearErrors() {
        _uiState.update {
            it.copy(emailError = null, passwordError = null, confirmPasswordError = null, generalError = null)
        }
    }

    fun login() {
        val state = _uiState.value
        val emailErr = AuthValidator.validateEmail(state.email)
        val passErr = AuthValidator.validatePassword(state.password)

        if (emailErr != null || passErr != null) {
            _uiState.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            repository.login(state.email, state.password)
                .onSuccess { _uiState.value = AuthUiState() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, generalError = mapFirebaseError(e)) }
                }
        }
    }

    fun register() {
        val state = _uiState.value
        val emailErr = AuthValidator.validateEmail(state.email)
        val passErr = AuthValidator.validatePassword(state.password)
        val confirmErr = AuthValidator.validatePasswordConfirmation(state.password, state.confirmPassword)

        if (emailErr != null || passErr != null || confirmErr != null) {
            _uiState.update {
                it.copy(emailError = emailErr, passwordError = passErr, confirmPasswordError = confirmErr)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            repository.register(state.email, state.password)
                .onSuccess { _uiState.value = AuthUiState() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, generalError = mapFirebaseError(e)) }
                }
        }
    }

    fun logout() {
        repository.logout()
    }

    private fun mapFirebaseError(throwable: Throwable): String = when (throwable) {
        is FirebaseAuthUserCollisionException -> "An account with this email already exists"
        is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password"
        is FirebaseAuthInvalidUserException -> "No account found with this email"
        is FirebaseNetworkException -> "Network error. Check your internet connection"
        else -> "Authentication failed. Please try again"
    }
}