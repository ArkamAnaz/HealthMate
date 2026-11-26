package com.healthmate.app.data.repository

import com.healthmate.app.data.local.HealthDatabaseHelper
import com.healthmate.app.data.model.DailySummary
import com.healthmate.app.data.model.DailyTotal
import com.healthmate.app.data.model.HealthRecord
import com.healthmate.app.utils.DateRange
import com.healthmate.app.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Single source of truth for health records.
 * ViewModels talk only to this class, never to SQLite directly.
 * Database work runs on Dispatchers.IO so the UI never freezes.
 */
class HealthRepository(private val dbHelper: HealthDatabaseHelper) {

    // Shared app state: every screen observes this list and updates automatically.
    private val _records = MutableStateFlow<List<HealthRecord>>(emptyList())
    val records: StateFlow<List<HealthRecord>> = _records.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        _records.value = dbHelper.getAllRecords()
    }

    suspend fun addRecord(record: HealthRecord) {
        withContext(Dispatchers.IO) { dbHelper.insertRecord(record) }
        refresh()
    }

    suspend fun updateRecord(record: HealthRecord) {
        withContext(Dispatchers.IO) { dbHelper.updateRecord(record) }
        refresh()
    }

    suspend fun deleteRecord(id: Long) {
        withContext(Dispatchers.IO) { dbHelper.deleteRecord(id) }
        refresh()
    }

    suspend fun getRecordById(id: Long): HealthRecord? = withContext(Dispatchers.IO) {
        dbHelper.getRecordById(id)
    }

    /** Records from [startDate] to [endDate] (inclusive, yyyy-MM-dd). */
    suspend fun getRecordsBetween(startDate: String, endDate: String): List<HealthRecord> =
        withContext(Dispatchers.IO) { dbHelper.getRecordsBetween(startDate, endDate) }

    suspend fun getSummary(range: DateRange): DailySummary = withContext(Dispatchers.IO) {
        dbHelper.getSummaryBetween(DateUtils.format(range.start), DateUtils.format(range.end))
    }

    /** One total for every day in [range]; days without records are returned as 0. */
    suspend fun getDailyTotals(range: DateRange): List<DailyTotal> = withContext(Dispatchers.IO) {
        val byDate = dbHelper
            .getDailyTotalsBetween(DateUtils.format(range.start), DateUtils.format(range.end))
            .associateBy { it.date }
        range.dates().map { day ->
            val key = DateUtils.format(day)
            byDate[key] ?: DailyTotal(date = key)
        }
    }
}
