package com.jorgeHernandez.mycloudstock.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorgeHernandez.mycloudstock.data.FirestoreProductRepository
import com.jorgeHernandez.mycloudstock.data.ProductInput
import com.jorgeHernandez.mycloudstock.data.ProductRepository
import com.jorgeHernandez.mycloudstock.data.model.Category
import com.jorgeHernandez.mycloudstock.domain.ProductValidationResult
import com.jorgeHernandez.mycloudstock.domain.ProductValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductEditUiState(
    val isEditMode: Boolean = false,
    val isLoadingInitial: Boolean = true,
    val isSaving: Boolean = false,
    val categories: List<Category> = emptyList(),
    val name: String = "",
    val selectedCategoryId: String = "",
    val selectedCategoryName: String = "",
    val stockText: String = "",
    val maxCapacityText: String = "",
    val errors: ProductValidationResult = ProductValidationResult(),
    val generalError: String? = null
)

class ProductEditViewModel(
    private val productId: String?,
    private val repository: ProductRepository = FirestoreProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProductEditUiState(isEditMode = !productId.isNullOrBlank())
    )
    val uiState: StateFlow<ProductEditUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val categoriesResult = repository.getCategories()
            val categories = categoriesResult.getOrElse { e ->
                _uiState.update {
                    it.copy(
                        isLoadingInitial = false,
                        generalError = e.localizedMessage ?: "Failed to load categories"
                    )
                }
                return@launch
            }

            if (productId.isNullOrBlank()) {
                val firstCategory = categories.firstOrNull()
                _uiState.update {
                    it.copy(
                        isLoadingInitial = false,
                        categories = categories,
                        selectedCategoryId = firstCategory?.id ?: "",
                        selectedCategoryName = firstCategory?.name ?: ""
                    )
                }
            } else {
                repository.getProduct(productId)
                    .onSuccess { product ->
                        _uiState.update {
                            it.copy(
                                isLoadingInitial = false,
                                categories = categories,
                                name = product.name,
                                selectedCategoryId = product.categoryId,
                                selectedCategoryName = product.categoryName,
                                stockText = product.stock.toString(),
                                maxCapacityText = product.maxCapacity.toString()
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoadingInitial = false,
                                categories = categories,
                                generalError = e.localizedMessage ?: "Failed to load product"
                            )
                        }
                    }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errors = it.errors.copy(nameError = null), generalError = null) }
    }

    fun onCategorySelect(category: Category) {
        _uiState.update {
            it.copy(
                selectedCategoryId = category.id,
                selectedCategoryName = category.name,
                errors = it.errors.copy(categoryError = null),
                generalError = null
            )
        }
    }

    fun onStockChange(value: String) {
        _uiState.update { it.copy(stockText = value, errors = it.errors.copy(stockError = null), generalError = null) }
    }

    fun onMaxCapacityChange(value: String) {
        _uiState.update {
            it.copy(
                maxCapacityText = value,
                errors = it.errors.copy(maxCapacityError = null, stockError = null),
                generalError = null
            )
        }
    }

    fun save(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.isSaving) return

        val validation = ProductValidator.validateAll(
            name = current.name,
            categoryId = current.selectedCategoryId,
            stockText = current.stockText,
            maxCapacityText = current.maxCapacityText
        )

        if (!validation.isValid) {
            _uiState.update { it.copy(errors = validation) }
            return
        }

        val input = ProductInput(
            name = current.name.trim(),
            categoryId = current.selectedCategoryId,
            categoryName = current.selectedCategoryName,
            stock = current.stockText.trim().toInt(),
            maxCapacity = current.maxCapacityText.trim().toInt()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null) }
            val result = if (productId.isNullOrBlank()) {
                repository.addProduct(input).map { Unit }
            } else {
                repository.updateProduct(productId, input)
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            generalError = e.localizedMessage ?: "Error saving product"
                        )
                    }
                }
        }
    }

    fun delete(onDeleted: (String) -> Unit) {
        val id = productId ?: return
        val current = _uiState.value
        if (current.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null) }
            repository.deleteProduct(id)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onDeleted(current.name)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            generalError = e.localizedMessage ?: "Error deleting product"
                        )
                    }
                }
        }
    }
}