package com.example.taskmanager.core.di

import com.example.taskmanager.core.data.TaskRepository
import com.example.taskmanager.core.data.TaskRepositoryImpl
import com.example.taskmanager.core.data.TeamRepository
import com.example.taskmanager.core.data.TeamRepositoryImpl
import com.example.taskmanager.feature.auth.AuthRepository
import com.example.taskmanager.feature.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): TeamRepository

    @Binds
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
}