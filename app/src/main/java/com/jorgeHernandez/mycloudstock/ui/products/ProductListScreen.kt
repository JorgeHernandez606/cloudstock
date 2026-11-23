package com.jorgeHernandez.mycloudstock.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jorgeHernandez.mycloudstock.data.model.Product
import com.jorgeHernandez.mycloudstock.domain.ProductSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    snackbarMessage: String? = null,
    onSnackbarShown: () -> Unit = {},
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        if (!snackbarMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CloudStock") },
                actions = {
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Log out"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add product")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Total products: ${state.stats.totalProducts} · Low stock: ${state.stats.lowStockCount}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search by name or category") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductSortOrder.entries.forEach { order ->
                    FilterChip(
                        selected = state.sortOrder == order,
                        onClick = { viewModel.onSortOrderChange(order) },
                        label = { Text(order.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                state.visibleProducts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.searchQuery.isBlank()) {
                                "No products yet. Tap + to add your first item"
                            } else {
                                "No products match \"${state.searchQuery}\""
                            }
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(state.visibleProducts, key = { it.productId }) { product ->
                            ProductRow(
                                product = product,
                                onClick = { onEditClick(product.productId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = product.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${product.stock} / ${product.maxCapacity}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}