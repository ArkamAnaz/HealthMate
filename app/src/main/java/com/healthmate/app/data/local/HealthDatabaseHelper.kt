package com.healthmate.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.healthmate.app.data.model.DailySummary
import com.healthmate.app.data.model.DailyTotal
import com.healthmate.app.data.model.HealthRecord
import com.healthmate.app.utils.DateUtils
import java.time.LocalDate

/**
 * Creates and manages the local SQLite database (healthmate.db).
 * All SQL for the health_records table lives in this one class.
 */
class HealthDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "healthmate.db"
        const val DATABASE_VERSION = 2 // v2: 30 days of dummy data for week/month charts

        const val TABLE_HEALTH_RECORDS = "health_records"
        const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_STEPS = "steps"
        const val COLUMN_CALORIES = "calories"
        const val COLUMN_WATER = "water"

        private const val CREATE_TABLE_SQL = """
            CREATE TABLE $TABLE_HEALTH_RECORDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_STEPS INTEGER NOT NULL,
                $COLUMN_CALORIES INTEGER NOT NULL,
                $COLUMN_WATER INTEGER NOT NULL
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SQL)
        insertDummyRecords(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HEALTH_RECORDS")
        onCreate(db)
    }

    /**
     * Adds sample data for the last 30 days so the day, week and month
     * views can be tested straight away. Today matches the example in the brief.
     */
    private fun insertDummyRecords(db: SQLiteDatabase) {
        val today = LocalDate.now()
        for (daysAgo in 0 until 30) {
            val steps = if (daysAgo == 0) 7850 else 4500 + (daysAgo * 1373) % 8000
            val calories = if (daysAgo == 0) 420 else 250 + (daysAgo * 97) % 400
            val water = if (daysAgo == 0) 2000 else 1250 + (daysAgo * 250) % 1750
            val values = ContentValues().apply {
                put(COLUMN_DATE, DateUtils.format(today.minusDays(daysAgo.toLong())))
                put(COLUMN_STEPS, steps)
                put(COLUMN_CALORIES, calories)
                put(COLUMN_WATER, water)
            }
            db.insert(TABLE_HEALTH_RECORDS, null, values)
        }
    }

    // ---------------- CREATE ----------------

    fun insertRecord(record: HealthRecord): Long {
        return writableDatabase.insert(TABLE_HEALTH_RECORDS, null, record.toContentValues())
    }

    // ---------------- READ ----------------

    fun getAllRecords(): List<HealthRecord> {
        val cursor = readableDatabase.query(
            TABLE_HEALTH_RECORDS, null, null, null, null, null,
            "$COLUMN_DATE DESC, $COLUMN_ID DESC"
        )
        return cursor.toRecordList()
    }

    /**
     * Records between two dates (inclusive). Used for "search by date"
     * (start = end) and "search by date range". Dates are yyyy-MM-dd text,
     * so SQLite BETWEEN compares them in the correct order.
     */
    fun getRecordsBetween(startDate: String, endDate: String): List<HealthRecord> {
        val cursor = readableDatabase.query(
            TABLE_HEALTH_RECORDS, null,
            "$COLUMN_DATE BETWEEN ? AND ?", arrayOf(startDate, endDate),
            null, null,
            "$COLUMN_DATE DESC, $COLUMN_ID DESC"
        )
        return cursor.toRecordList()
    }

    fun getRecordById(id: Long): HealthRecord? {
        val cursor = readableDatabase.query(
            TABLE_HEALTH_RECORDS, null,
            "$COLUMN_ID = ?", arrayOf(id.toString()),
            null, null, null
        )
        return cursor.toRecordList().firstOrNull()
    }

    /** Uses SQL SUM() to total all entries in a date range (used by the dashboard). */
    fun getSummaryBetween(startDate: String, endDate: String): DailySummary {
        val sql = """
            SELECT IFNULL(SUM($COLUMN_STEPS), 0),
                   IFNULL(SUM($COLUMN_CALORIES), 0),
                   IFNULL(SUM($COLUMN_WATER), 0),
                   COUNT(*)
            FROM $TABLE_HEALTH_RECORDS
            WHERE $COLUMN_DATE BETWEEN ? AND ?
        """
        readableDatabase.rawQuery(sql, arrayOf(startDate, endDate)).use { cursor ->
            return if (cursor.moveToFirst()) {
                DailySummary(
                    totalSteps = cursor.getInt(0),
                    totalCalories = cursor.getInt(1),
                    totalWater = cursor.getInt(2),
                    entryCount = cursor.getInt(3)
                )
            } else {
                DailySummary()
            }
        }
    }

    /** One total per day (GROUP BY date), used by the weekly / monthly bar chart. */
    fun getDailyTotalsBetween(startDate: String, endDate: String): List<DailyTotal> {
        val sql = """
            SELECT $COLUMN_DATE, SUM($COLUMN_STEPS), SUM($COLUMN_CALORIES), SUM($COLUMN_WATER)
            FROM $TABLE_HEALTH_RECORDS
            WHERE $COLUMN_DATE BETWEEN ? AND ?
            GROUP BY $COLUMN_DATE
            ORDER BY $COLUMN_DATE ASC
        """
        val totals = mutableListOf<DailyTotal>()
        readableDatabase.rawQuery(sql, arrayOf(startDate, endDate)).use { cursor ->
            while (cursor.moveToNext()) {
                totals.add(
                    DailyTotal(
                        date = cursor.getString(0),
                        steps = cursor.getInt(1),
                        calories = cursor.getInt(2),
                        water = cursor.getInt(3)
                    )
                )
            }
        }
        return totals
    }

    // ---------------- UPDATE ----------------

    fun updateRecord(record: HealthRecord): Int {
        return writableDatabase.update(
            TABLE_HEALTH_RECORDS,
            record.toContentValues(),
            "$COLUMN_ID = ?",
            arrayOf(record.id.toString())
        )
    }

    // ---------------- DELETE ----------------

    fun deleteRecord(id: Long): Int {
        return writableDatabase.delete(
            TABLE_HEALTH_RECORDS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    // ---------------- Helpers ----------------

    private fun HealthRecord.toContentValues() = ContentValues().apply {
        put(COLUMN_DATE, date)
        put(COLUMN_STEPS, steps)
        put(COLUMN_CALORIES, calories)
        put(COLUMN_WATER, water)
    }

    private fun Cursor.toRecordList(): List<HealthRecord> {
        val records = mutableListOf<HealthRecord>()
        use { cursor ->
            while (cursor.moveToNext()) {
                records.add(
                    HealthRecord(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS)),
                        calories = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                        water = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WATER))
                    )
                )
            }
        }
        return records
    }
}
