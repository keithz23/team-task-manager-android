package com.example.taskmanager.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionStore by preferencesDataStore("session")

@Singleton
class DataStoreSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SessionManager {
    private val key = stringPreferencesKey("user_id")

    override val authState: Flow<AuthState> = context.sessionStore.data
        .map { p -> p[key]?.let { AuthState.LoggedIn(it) } ?: AuthState.LoggedOut }
        .distinctUntilChanged()
        .onStart { emit(AuthState.Loading) }

    override suspend fun currentUserId(): String? = context.sessionStore.data.first()[key]
    override suspend fun save(userId: String) { context.sessionStore.edit { it[key] = userId } }
    override suspend fun clear() { context.sessionStore.edit { it.remove(key) } }
}

