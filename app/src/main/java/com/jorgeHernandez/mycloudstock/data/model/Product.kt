package com.jorgeHernandez.mycloudstock.data.model
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId val productId: String = "",
    val userId: String = "",
    val name: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val stock: Int = 0,
    val maxCapacity: Int = 0,
    val status: String = "pending",
    val rejectReason: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)