package com.jorgeHernandez.mycloudstock.domain

import com.jorgeHernandez.mycloudstock.data.model.Product

enum class ProductSortOrder(val label: String) {
    NAME_ASC("Name (A–Z)"),
    STOCK_ASC("Lowest stock first"),
    STOCK_DESC("Highest stock first")
}

data class InventoryStats(
    val totalProducts: Int,
    val lowStockCount: Int
)

object ProductListLogic {

    fun filterProducts(products: List<Product>, query: String): List<Product> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return products
        return products.filter { product ->
            product.name.contains(cleanQuery, ignoreCase = true) ||
                    product.categoryName.contains(cleanQuery, ignoreCase = true)
        }
    }


    fun sortProducts(products: List<Product>, sortOrder: ProductSortOrder): List<Product> =
        when (sortOrder) {
            ProductSortOrder.NAME_ASC -> products.sortedBy { it.name.lowercase() }
            ProductSortOrder.STOCK_ASC -> products.sortedBy { it.stock }
            ProductSortOrder.STOCK_DESC -> products.sortedByDescending { it.stock }
        }

    fun computeStats(products: List<Product>): InventoryStats {
        val lowStock = products.count { p ->
            p.maxCapacity > 0 && p.stock.toDouble() / p.maxCapacity <= 0.25
        }
        return InventoryStats(
            totalProducts = products.size,
            lowStockCount = lowStock
        )
    }
}