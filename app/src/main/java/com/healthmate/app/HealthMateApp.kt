package com.healthmate.app

import android.app.Application
import com.healthmate.app.data.local.GoalsPreferences
import com.healthmate.app.data.local.HealthDatabaseHelper
import com.healthmate.app.data.repository.GoalsRepository
import com.healthmate.app.data.repository.HealthRepository

/** Creates the database, the settings storage and the repositories once for the whole app. */
class HealthMateApp : Application() {

    lateinit var repository: HealthRepository
        private set

    lateinit var goalsRepository: GoalsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = HealthRepository(HealthDatabaseHelper(this))
        goalsRepository = GoalsRepository(GoalsPreferences(this))
    }
}
