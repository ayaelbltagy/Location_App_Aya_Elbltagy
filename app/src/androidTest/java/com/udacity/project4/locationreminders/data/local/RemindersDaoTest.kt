package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

@get:Rule
var role = InstantTaskExecutorRule()
    private lateinit var database : RemindersDatabase

    @Before
    fun initilization_of_db(){
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java).build()
    }
    @After
    fun close_data_base(){
        database.close()
    }
    @Test
      fun test_insert_and_retrive_data_from_db() = runBlockingTest{
        val data = ReminderDTO("test","desc","location",100.00,100.00)
        database.reminderDao().saveReminder(data)
        val list = database.reminderDao().getReminders()
         assertThat(list[0].location, `is`("location"))
    }
    @Test
    fun test_delete_from_db() = runBlockingTest{
        val data = ReminderDTO("test","desc","location",100.00,100.00)
        database.reminderDao().saveReminder(data)
        database.reminderDao().deleteAllReminders()
        assertThat(database.reminderDao().getReminders().isEmpty(), `is`(true))
    }
    @Test
    fun test_id_not_found() = runBlockingTest{
        assertThat(database.reminderDao().getReminderById("5600"), CoreMatchers.nullValue())

    }
}