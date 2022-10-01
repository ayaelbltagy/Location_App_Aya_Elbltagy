package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.rule.MainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    val rule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase
    private lateinit var repo: RemindersLocalRepository

    @get:Rule
    val mainRule = MainCoroutineRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)

    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun test_add_and_retrive_from_db() = mainRule.runBlockingTest {
        //Given
        val damyData = ReminderDTO("test title", "descripition", "location", 50.100, 20.00)
        // dave data to db
        // When
        repo.saveReminder(damyData)
        // get data from db
        //Then
        val result = repo.getReminder(damyData.id) as Result.Success<ReminderDTO>
        MatcherAssert.assertThat(result.data, Matchers.notNullValue())
    }

    @Test
    fun test_insert_and_delete_from_db() = mainRule.runBlockingTest {
        // add damy data to db
        val test_data = ReminderDTO("title", "description", "location", 600.00, 800.00)
        // save damy data
        repo.saveReminder(test_data)
        // delete damy data
        repo.deleteAllReminders()
        // get all reminders
        val result = repo.getReminders() as Result.Success<List<ReminderDTO>>
        MatcherAssert.assertThat(result.data.isEmpty(), CoreMatchers.`is`(true))

    }

    @Test
    fun test_error_if_reminder_not_found() = runBlockingTest {
        val result = repo.getReminder("50000") as Result.Error
        MatcherAssert.assertThat(result.message, CoreMatchers.`is`("Reminder not found!"))

    }
}
