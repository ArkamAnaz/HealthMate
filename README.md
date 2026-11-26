# HealthMate – Personal Health & Wellness Tracker

HealthMate is a native Android app (Kotlin + Jetpack Compose) for recording daily
health activities: **steps walked, calories burned and water intake**.

## Features

- **Dashboard** – Day / Week / Month views; a "Summary vs goal" bar chart for steps 👣, calories 🔥 and water 💧; a daily bar chart (one bar per day) for the week and month views; totals and daily averages
- **Records page** – all records newest first, search by a **single date** or by a **date range** (From / To calendar), edit and delete with confirmation, + button to add a record
- **Add / Edit record** – form with validation (date from a calendar, whole numbers only, no negatives, no future dates, sensible maximums)
- **Settings** – save daily goals for steps, calories burned and water intake (validated, stored with SharedPreferences, "Reset to defaults")
- **Local SQLite database** with 30 days of dummy records created on first launch

## Tech stack

| Item | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (feature-based packages) |
| State management | ViewModel + Kotlin `StateFlow` / Compose state |
| Database | SQLite via Android `SQLiteOpenHelper` (table `health_records`); daily goals in SharedPreferences |
| Navigation | Navigation Compose |
| Async | Kotlin Coroutines (`Dispatchers.IO` for database work) |
| Testing | JUnit 4 unit tests |

## Project structure

```
app/src/main/java/com/healthmate/app/
├── HealthMateApp.kt            # Application: creates database + repository once
├── MainActivity.kt             # Entry point, sets the Compose theme + navigation
├── data/
│   ├── local/HealthDatabaseHelper.kt   # SQLite table, dummy data, CRUD, BETWEEN, SUM, GROUP BY
│   ├── local/GoalsPreferences.kt       # Daily goals in SharedPreferences
│   ├── model/HealthRecord.kt           # HealthRecord, DailySummary, DailyTotal, DailyGoals
│   └── repository/                     # HealthRepository + GoalsRepository (StateFlow)
├── di/AppViewModelProvider.kt  # ViewModel factory (gives ViewModels the repository)
├── features/
│   ├── dashboard/   # DashboardScreen + DashboardViewModel (day/week/month, bar charts)
│   ├── records/     # RecordListScreen + RecordListViewModel (list, date / date-range search, delete)
│   ├── addrecord/   # AddEditRecordScreen + AddEditRecordViewModel (form, validation)
│   └── settings/    # SettingsScreen + SettingsViewModel (daily goals)
├── navigation/      # NavHost routes + bottom navigation bar
├── ui/
│   ├── components/  # Bar charts, StatCard, HealthRecordItem, NumberField, DateSelectField, date picker
│   └── theme/       # Colours, typography, Material theme
└── utils/           # DateUtils, PeriodUtils, RecordValidator, number formatting
```

## Architecture (MVVM)

```
 UI (Compose Screens)
        │  user events ↓        ↑ state (StateFlow)
 ViewModels (Dashboard / RecordList / AddEditRecord)
        │  suspend calls ↓      ↑ StateFlow<List<HealthRecord>>
 HealthRepository
        │  ↓                    ↑
 HealthDatabaseHelper (SQLiteOpenHelper)
        │
 SQLite database: healthmate.db → table health_records
```

## Database

Table `health_records`

| Field | Type | Description |
|---|---|---|
| id | INTEGER PRIMARY KEY AUTOINCREMENT | Unique record id |
| date | TEXT | Date of the entry (`yyyy-MM-dd`) |
| steps | INTEGER | Steps walked |
| calories | INTEGER | Calories burned (kcal) |
| water | INTEGER | Water intake (ml) |

## How to run

1. Open the project folder in **Android Studio** (File → Open).
2. Wait for Gradle sync to finish.
3. Run on an emulator or phone (Android 8.0 / API 26 or higher).

## Build the release APK

`Build → Build App Bundle(s) / APK(s) → Build APK(s)` builds the debug APK.
For the release APK, open the **Gradle** panel → `app → Tasks → build → assembleRelease`,
or run in the terminal:

```
./gradlew assembleRelease
```

The APK is created at `app/build/outputs/apk/release/app-release.apk`.

## Run the unit tests

Right-click `app/src/test/java` → **Run 'Tests in ...'**, or:

```
./gradlew test
```
