package com.jorgeHernandez.mycloudstock.ui.products

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorgeHernandez.mycloudstock.data.FirestoreProductRepository
import com.jorgeHernandez.mycloudstock.data.ProductRepository
import com.jorgeHernandez.mycloudstock.data.model.Product
import com.jorgeHernandez.mycloudstock.domain.InventoryStats
import com.jorgeHernandez.mycloudstock.domain.ProductListLogic
import com.jorgeHernandez.mycloudstock.domain.ProductSortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProductListUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val visibleProducts: List<Product> = emptyList(),
    val stats: InventoryStats = InventoryStats(0, 0),
    val searchQuery: String = "",
    val sortOrder: ProductSortOrder = ProductSortOrder.NAME_ASC
)

class ProductListViewModel(
    private val repository: ProductRepository = FirestoreProductRepository(),
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(loadSavedSortOrder())

    val uiState: StateFlow<ProductListUiState> = combine(
        repository.observeMyProducts(),
        searchQuery,
        sortOrder
    ) { allProducts, query, order ->
        val filtered = ProductListLogic.filterProducts(allProducts, query)
        val sorted = ProductListLogic.sortProducts(filtered, order)
        val stats = ProductListLogic.computeStats(allProducts)
        ProductListUiState(
            isLoading = false,
            errorMessage = null,
            visibleProducts = sorted,
            stats = stats,
            searchQuery = query,
            sortOrder = order
        )
    }.catch { e ->
        emit(
            ProductListUiState(
                isLoading = false,
                errorMessage = e.localizedMessage ?: "Error loading products"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProductListUiState(isLoading = true)
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onSortOrderChange(newOrder: ProductSortOrder) {
        sortOrder.value = newOrder
        prefs?.edit()?.putString("sort_order", newOrder.name)?.apply()
    }

    private fun loadSavedSortOrder(): ProductSortOrder {
        val saved = prefs?.getString("sort_order", null) ?: return ProductSortOrder.NAME_ASC
        return runCatching { ProductSortOrder.valueOf(saved) }.getOrDefault(ProductSortOrder.NAME_ASC)
    }
}