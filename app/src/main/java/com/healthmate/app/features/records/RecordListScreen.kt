package com.healthmate.app.features.records

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthmate.app.data.model.HealthRecord
import com.healthmate.app.di.AppViewModelProvider
import com.healthmate.app.ui.components.DateSelectField
import com.healthmate.app.ui.components.EmptyState
import com.healthmate.app.ui.components.HealthRecordItem
import com.healthmate.app.ui.components.showDatePicker
import com.healthmate.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    onAddRecord: () -> Unit,
    onRecordClick: (Long) -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: RecordListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val records by viewModel.records.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val context = LocalContext.current

    // Record waiting for delete confirmation (null = no dialog)
    var recordToDelete by remember { mutableStateOf<HealthRecord?>(null) }

    // When a new record is added (or the search changes) jump back to the top,
    // otherwise the list keeps its old position and the new record is hidden above.
    val listState = rememberLazyListState()
    LaunchedEffect(records.firstOrNull()?.id, filter) {
        listState.scrollToItem(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Records", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecord) {
                Icon(Icons.Filled.Add, contentDescription = "Add record")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ---- Search type: single date or date range ----
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchMode.entries.forEach { mode ->
                    FilterChip(
                        selected = filter.mode == mode,
                        onClick = { viewModel.setMode(mode) },
                        label = { Text(mode.label) },
                        leadingIcon = if (filter.mode == mode) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            // ---- Date pickers ----
            when (filter.mode) {
                SearchMode.SINGLE_DATE -> DateSelectField(
                    label = "Show records for",
                    date = filter.date,
                    onClick = {
                        showDatePicker(context, filter.date) { viewModel.setDate(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                SearchMode.DATE_RANGE -> Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateSelectField(
                        label = "From",
                        date = filter.from,
                        onClick = {
                            showDatePicker(context, filter.from ?: filter.to, maxDate = filter.to) {
                                viewModel.setFrom(it)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DateSelectField(
                        label = "To",
                        date = filter.to,
                        onClick = {
                            showDatePicker(context, filter.to, minDate = filter.from) {
                                viewModel.setTo(it)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ---- Result text + clear ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resultText(records.size, filter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (filter.hasAnyValue) {
                    TextButton(onClick = viewModel::clearFilter) { Text("Clear") }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // ---- List ----
            if (records.isEmpty()) {
                EmptyState(
                    message = if (filter.isActive) "No records found for the selected date(s)."
                    else "No health records yet."
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        HealthRecordItem(
                            record = record,
                            onClick = { onRecordClick(record.id) },
                            onDelete = { recordToDelete = record }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    // ---- Delete confirmation dialog ----
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete record?") },
            text = { Text("The record for ${DateUtils.toDisplay(record.date)} will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord(record)
                    recordToDelete = null
                    Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

private fun resultText(count: Int, filter: RecordFilter): String = when {
    !filter.isActive && filter.mode == SearchMode.DATE_RANGE && filter.hasAnyValue ->
        "Select both a From and a To date"
    !filter.isActive -> "All records ($count)"
    filter.mode == SearchMode.SINGLE_DATE ->
        "$count record(s) on ${DateUtils.toMediumDisplay(filter.date!!)}"
    else ->
        "$count record(s) from ${DateUtils.toMediumDisplay(filter.from!!)} to ${DateUtils.toMediumDisplay(filter.to!!)}"
}
