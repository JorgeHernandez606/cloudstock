package com.jorgeHernandez.mycloudstock.domain
object AuthValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Email is required"
            !EMAIL_REGEX.matches(trimmed) -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? = when {
        password.isEmpty() -> "Password is required"
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Please confirm your password"
        password != confirm -> "Passwords do not match"
        else -> null
    }
}