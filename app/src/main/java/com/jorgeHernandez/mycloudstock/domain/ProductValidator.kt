package com.jorgeHernandez.mycloudstock.domain

data class ProductValidationResult(
    val nameError: String? = null,
    val categoryError: String? = null,
    val stockError: String? = null,
    val maxCapacityError: String? = null
) {
    val isValid: Boolean
        get() = nameError == null &&
                categoryError == null &&
                stockError == null &&
                maxCapacityError == null
}

object ProductValidator {

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Product name is required"
            trimmed.length > 60 -> "Maximum 60 characters"
            else -> null
        }
    }

    fun validateCategory(categoryId: String): String? =
        if (categoryId.isBlank()) "Please select a category" else null

    fun validateMaxCapacity(maxCapacityText: String): String? {
        val value = maxCapacityText.trim().toIntOrNull()
        return when {
            maxCapacityText.trim().isEmpty() -> "Max capacity is required"
            value == null -> "Must be a valid whole number"
            value <= 0 -> "Max capacity must be greater than 0"
            else -> null
        }
    }

    fun validateStock(stockText: String, maxCapacityText: String): String? {
        val stock = stockText.trim().toIntOrNull()
        val maxCap = maxCapacityText.trim().toIntOrNull()
        return when {
            stockText.trim().isEmpty() -> "Stock is required"
            stock == null -> "Must be a valid whole number"
            stock < 0 -> "Stock cannot be negative"
            maxCap != null && maxCap > 0 && stock > maxCap -> "Stock cannot exceed max capacity ($maxCap)"
            else -> null
        }
    }

    fun validateAll(
        name: String,
        categoryId: String,
        stockText: String,
        maxCapacityText: String
    ): ProductValidationResult = ProductValidationResult(
        nameError = validateName(name),
        categoryError = validateCategory(categoryId),
        maxCapacityError = validateMaxCapacity(maxCapacityText),
        stockError = validateStock(stockText, maxCapacityText)
    )
}