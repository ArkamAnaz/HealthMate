package com.healthmate.app.features.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthmate.app.data.model.HealthRecord
import com.healthmate.app.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How the user is searching the records. */
enum class SearchMode(val label: String) {
    SINGLE_DATE("Date"),
    DATE_RANGE("Date range")
}

/** Current search: one date, or a from–to range (dates are yyyy-MM-dd). */
data class RecordFilter(
    val mode: SearchMode = SearchMode.SINGLE_DATE,
    val date: String? = null,
    val from: String? = null,
    val to: String? = null
) {
    val isActive: Boolean
        get() = when (mode) {
            SearchMode.SINGLE_DATE -> date != null
            SearchMode.DATE_RANGE -> from != null && to != null
        }

    val hasAnyValue: Boolean
        get() = date != null || from != null || to != null
}

class RecordListViewModel(private val repository: HealthRepository) : ViewModel() {

    private val _filter = MutableStateFlow(RecordFilter())
    val filter: StateFlow<RecordFilter> = _filter.asStateFlow()

    /**
     * The list shown on screen. All records when no search is set, otherwise
     * only the records for the chosen date or date range (SQL BETWEEN).
     */
    val records: StateFlow<List<HealthRecord>> =
        combine(repository.records, _filter) { allRecords, filter ->
            when {
                !filter.isActive -> allRecords
                filter.mode == SearchMode.SINGLE_DATE ->
                    repository.getRecordsBetween(filter.date!!, filter.date)
                else -> repository.getRecordsBetween(filter.from!!, filter.to!!)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch { repository.refresh() }
    }

    fun setMode(mode: SearchMode) {
        _filter.value = _filter.value.copy(mode = mode)
    }

    fun setDate(date: String) {
        _filter.value = _filter.value.copy(date = date)
    }

    /** If the new start date is after the end date, the end date is cleared. */
    fun setFrom(date: String) {
        val current = _filter.value
        val to = current.to?.takeIf { it >= date }
        _filter.value = current.copy(from = date, to = to)
    }

    fun setTo(date: String) {
        _filter.value = _filter.value.copy(to = date)
    }

    fun clearFilter() {
        _filter.value = RecordFilter(mode = _filter.value.mode)
    }

    fun deleteRecord(record: HealthRecord) {
        viewModelScope.launch { repository.deleteRecord(record.id) }
    }
}
