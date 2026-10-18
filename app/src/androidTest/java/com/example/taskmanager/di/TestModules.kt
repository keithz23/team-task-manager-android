package com.example.taskmanager.di

import android.content.Context
import androidx.room.Room
import com.example.taskmanager.core.database.AppDatabase
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.di.DbModule
import com.example.taskmanager.core.security.PasswordHasher
import com.example.taskmanager.core.session.SessionManager
import com.example.taskmanager.core.session.SessionModule
import com.example.taskmanager.util.FakeSessionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

/**
 * Thay DbModule trong mọi @HiltAndroidTest: Room in-memory (mất khi test kết thúc),
 * đã có sẵn dữ liệu SeedData.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DbModule::class])
object TestDbModule {
    @Provides @Singleton
    fun db(@ApplicationContext c: Context, hasher: PasswordHasher): AppDatabase =
        Room.inMemoryDatabaseBuilder(c, AppDatabase::class.java).build()
            .also { db -> runBlocking { SeedData.seed(db, hasher) } }

    @Provides fun userDao(db: AppDatabase) = db.userDao()
    @Provides fun teamDao(db: AppDatabase) = db.teamDao()
    @Provides fun projectDao(db: AppDatabase) = db.projectDao()
    @Provides fun taskDao(db: AppDatabase) = db.taskDao()
    @Provides fun commentDao(db: AppDatabase) = db.commentDao()
}

/** Thay SessionModule: dùng FakeSessionManager (trong bộ nhớ) thay cho DataStore. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SessionModule::class])
abstract class TestSessionModule {
    @Binds abstract fun bindSession(impl: FakeSessionManager): SessionManager
}
