package com.example.taskmanager.core.session

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds abstract fun bindSession(impl: DataStoreSessionManager): SessionManager
}
