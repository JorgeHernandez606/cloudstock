package com.jorgeHernandez.mycloudstock.data

import com.jorgeHernandez.mycloudstock.data.model.Category
import com.jorgeHernandez.mycloudstock.data.model.Product
import kotlinx.coroutines.flow.Flow

data class ProductInput(
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val stock: Int,
    val maxCapacity: Int
)

interface ProductRepository {
    fun observeMyProducts(): Flow<List<Product>>
    suspend fun getProduct(id: String): Result<Product>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun addProduct(input: ProductInput): Result<String>
    suspend fun updateProduct(id: String, input: ProductInput): Result<Unit>
    suspend fun deleteProduct(id: String): Result<Unit>
}