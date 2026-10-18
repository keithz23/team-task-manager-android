package com.example.taskmanager

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Runner cho androidTest: thay TaskApp bằng HiltTestApplication để test dùng được
 * @HiltAndroidTest và các module giả trong di/TestModules.kt.
 * Khai báo ở app/build.gradle.kts (testInstrumentationRunner).
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
