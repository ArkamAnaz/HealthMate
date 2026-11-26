package com.healthmate.app.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.healthmate.app.HealthMateApp
import com.healthmate.app.features.addrecord.AddEditRecordViewModel
import com.healthmate.app.features.dashboard.DashboardViewModel
import com.healthmate.app.features.records.RecordListViewModel
import com.healthmate.app.features.settings.SettingsViewModel

/** Factory that gives every ViewModel the repositories it needs. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            DashboardViewModel(healthMateApp().repository, healthMateApp().goalsRepository)
        }
        initializer {
            RecordListViewModel(healthMateApp().repository)
        }
        initializer {
            AddEditRecordViewModel(createSavedStateHandle(), healthMateApp().repository)
        }
        initializer {
            SettingsViewModel(healthMateApp().goalsRepository)
        }
    }
}

fun CreationExtras.healthMateApp(): HealthMateApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HealthMateApp
