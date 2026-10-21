package com.example.taskmanager

import android.app.Application
import com.example.taskmanager.core.database.DebugSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TaskApp : Application() {

    @Inject lateinit var debugSeeder: DebugSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Bản debug: có sẵn dữ liệu demo. Khi chạy androidTest app là HiltTestApplication nên không chạy đoạn này.
        if (BuildConfig.DEBUG) appScope.launch { debugSeeder.seedIfNeeded() }
    }
}
