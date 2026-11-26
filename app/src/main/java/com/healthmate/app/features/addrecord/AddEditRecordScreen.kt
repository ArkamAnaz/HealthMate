package com.healthmate.app.features.addrecord

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthmate.app.di.AppViewModelProvider
import com.healthmate.app.ui.components.NumberField
import com.healthmate.app.ui.components.showDatePicker
import com.healthmate.app.ui.theme.CaloriesRed
import com.healthmate.app.ui.theme.StepsGreen
import com.healthmate.app.ui.theme.WaterBlue
import com.healthmate.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditRecordViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val form = viewModel.formState
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isEditMode) "Edit Record" else "Add Record",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding() // keep fields visible above the keyboard
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Enter your daily activity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ---- Date (chosen from a calendar) ----
            OutlinedTextField(
                value = DateUtils.toDisplay(form.date),
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date *") },
                leadingIcon = { Text("📅") },
                trailingIcon = {
                    IconButton(onClick = {
                        showDatePicker(context, form.date) { viewModel.onDateChange(it) }
                    }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Choose date")
                    }
                },
                isError = form.dateError != null,
                supportingText = {
                    Text(form.dateError ?: "Tap the calendar icon to change the date")
                }
            )

            NumberField(
                value = form.steps,
                onValueChange = viewModel::onStepsChange,
                label = "Steps walked *",
                emoji = "👣",
                unit = "steps",
                accent = StepsGreen,
                error = form.stepsError
            )
            NumberField(
                value = form.calories,
                onValueChange = viewModel::onCaloriesChange,
                label = "Calories burned *",
                emoji = "🔥",
                unit = "kcal",
                accent = CaloriesRed,
                error = form.caloriesError
            )
            NumberField(
                value = form.water,
                onValueChange = viewModel::onWaterChange,
                label = "Water intake *",
                emoji = "💧",
                unit = "ml",
                accent = WaterBlue,
                error = form.waterError,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    viewModel.saveRecord {
                        val msg = if (viewModel.isEditMode) "Record updated" else "Record saved"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                },
                enabled = !form.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (viewModel.isEditMode) "Update Record" else "Save Record")
            }

            if (viewModel.isEditMode) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Record")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete record?") },
            text = { Text("This record will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteRecord {
                        Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
