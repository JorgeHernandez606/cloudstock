package com.jorgeHernandez.mycloudstock.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.jorgeHernandez.mycloudstock.data.model.Category
import com.jorgeHernandez.mycloudstock.data.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreProductRepository(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth
) : ProductRepository {

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not signed in")

    override fun observeMyProducts(): Flow<List<Product>> = callbackFlow {
        val uid = try {
            requireUid()
        } catch (e: Exception) {
            close(e)
            return@callbackFlow
        }

        val registration = db.collection("products")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Product::class.java))
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getProduct(id: String): Result<Product> = runCatching {
        val doc = db.collection("products").document(id).get().await()
        doc.toObject(Product::class.java) ?: throw NoSuchElementException("Product not found")
    }

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        val snapshot = db.collection("categories").get().await()
        snapshot.toObjects(Category::class.java).sortedBy { it.id }
    }

    override suspend fun addProduct(input: ProductInput): Result<String> = runCatching {
        val uid = requireUid()
        val data = hashMapOf(
            "userId" to uid,
            "name" to input.name.trim(),
            "categoryId" to input.categoryId,
            "categoryName" to input.categoryName,
            "stock" to input.stock,
            "maxCapacity" to input.maxCapacity,
            "status" to "pending",
            "rejectReason" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val docRef = db.collection("products").add(data).await()
        docRef.id
    }

    override suspend fun updateProduct(id: String, input: ProductInput): Result<Unit> = runCatching {
        val updates = hashMapOf<String, Any>(
            "name" to input.name.trim(),
            "categoryId" to input.categoryId,
            "categoryName" to input.categoryName,
            "stock" to input.stock,
            "maxCapacity" to input.maxCapacity,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("products").document(id).update(updates).await()
    }

    override suspend fun deleteProduct(id: String): Result<Unit> = runCatching {
        db.collection("products").document(id).delete().await()
    }
}