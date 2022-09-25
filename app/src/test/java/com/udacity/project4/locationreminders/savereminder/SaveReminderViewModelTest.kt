package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.rule.MainCoroutineRule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var saveViewModel : SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

     @get:Rule
     var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupViewModel(){
        fakeDataSource = FakeDataSource()
        // Given fresh view model
        saveViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    @Test
    fun check_loading() = runBlockingTest {
      mainCoroutineRule.pauseDispatcher()
        // When click save
        saveViewModel.saveReminder(createFakeReminderDataItem())
        // Then the loading is showed
        MatcherAssert.assertThat(saveViewModel.showLoading.value, CoreMatchers.`is`(true))

       // mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(saveViewModel.showLoading.value, CoreMatchers.`is`(false))
    }
    private fun createFakeReminderDataItem(): ReminderDataItem {
        return ReminderDataItem(
            "title abc",
            "description abc",
            "location abc",
            77.00,
            77.00)
    }
}