package com.example.taskmanager

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskmanager.core.database.AppDatabase
import com.example.taskmanager.core.database.DebugSeeder
import com.example.taskmanager.core.database.SeedData
import com.example.taskmanager.core.security.PasswordHasher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** DebugSeeder trên DB rỗng (không dùng Hilt, vì DB của Hilt test đã seed sẵn). */
@RunWith(AndroidJUnit4::class)
class DebugSeederTest {

    private lateinit var db: AppDatabase
    private lateinit var seeder: DebugSeeder

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        seeder = DebugSeeder(db, PasswordHasher())
    }

    @After fun tearDown() = db.close()

    @Test
    fun emptyDb_getsDemoData() = runTest {
        assertNull(db.userDao().findById(SeedData.USER_AN))

        seeder.seedIfNeeded()

        assertEquals(SeedData.USER_AN, db.userDao().findByEmail(SeedData.EMAIL_AN)?.id)
        assertEquals(2, db.commentDao().observe(SeedData.TASK_TODO).first().size)
    }

    @Test
    fun calledTwice_doesNotDuplicateOrCrash() = runTest {
        seeder.seedIfNeeded()
        seeder.seedIfNeeded()

        assertEquals(2, db.commentDao().observe(SeedData.TASK_TODO).first().size)
    }
}
