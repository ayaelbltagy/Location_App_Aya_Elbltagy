package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {


    private lateinit var repo: ReminderDataSource
    private lateinit var application: Application

    @Before
    fun init() {
        stopKoin()
        application = getApplicationContext()
        val modules = module {
            // call view model that you want to test
            viewModel {

                RemindersListViewModel(application, get() as ReminderDataSource)
            }
            single {
                SaveReminderViewModel(application, get() as ReminderDataSource)
            }
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(application)
            }
        }
        startKoin { modules(listOf(modules)) }
        repo = get()
        runBlocking {
            repo.deleteAllReminders()
        }
    }


    @Test
    fun add_reminder_and_display_in_ui() {
        val reminder = ReminderDTO("title", "description", "location", 550.0, 60.0)
        runBlocking {
            repo.saveReminder(reminder)
        }
        // add fragment that you want to test
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText(reminder.title)).check(matches(isDisplayed()))
        Thread.sleep(2000)
    }

    @Test
    fun test_navigate_to_add_reminder() {
        Thread.sleep(2000)
        val senario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val nav = mock(NavController::class.java)
        senario.onFragment {
            Navigation.setViewNavController(it.view!!, nav)
        }
        // to check that view is clickable
        onView(withId(R.id.addReminderFAB)).perform(click())
        // to check that the other view is showed
        verify(nav).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}