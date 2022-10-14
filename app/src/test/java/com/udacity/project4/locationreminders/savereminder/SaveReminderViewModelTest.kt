package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.rule.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {
    private lateinit var saveViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupViewModel() {
        fakeDataSource = FakeDataSource()
        // Given fresh view model
        saveViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun check_loading  () = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        // When click save
        saveViewModel.saveReminder(ReminderDataItem("title","description","location",0.0,0.0))
        // Then the loading is showed
        MatcherAssert.assertThat(saveViewModel.showLoading.value, CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(saveViewModel.showLoading.value, CoreMatchers.`is`(false))
    }



    @Test
    fun shouldReturnError () = mainCoroutineRule.runBlockingTest {
        var item = ReminderDataItem("","description","location",0.0,0.0)
        val isResponseValid = saveViewModel.validateEnteredData(item)
        MatcherAssert.assertThat(isResponseValid, CoreMatchers.`is`(false))
        MatcherAssert.assertThat(saveViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }




}