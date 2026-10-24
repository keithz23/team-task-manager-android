package com.example.taskmanager.core.di

import android.content.Context
import androidx.room.Room
import com.example.taskmanager.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DbModule {
    @Provides @Singleton
    fun db(@ApplicationContext c: Context) =
        Room.databaseBuilder(c, AppDatabase::class.java, "taskmanager.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun userDao(db: AppDatabase) = db.userDao()
    @Provides fun teamDao(db: AppDatabase) = db.teamDao()
    @Provides fun projectDao(db: AppDatabase) = db.projectDao()
    @Provides fun taskDao(db: AppDatabase) = db.taskDao()
    @Provides fun commentDao(db: AppDatabase) = db.commentDao()
}