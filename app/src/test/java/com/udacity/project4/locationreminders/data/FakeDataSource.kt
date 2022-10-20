package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false


//    override suspend fun getReminders(): Result<List<ReminderDTO>> = withContext(Dispatchers.IO){
//        try {
//            if (shouldReturnError) {
//                return@withContext Result.Error("Test Exception")
//            }
//            reminders.let {
//                return@let Result.Success(ArrayList(it))
//            }
//        }
//        catch (ex:Exception){
//              Result.Error(ex.localizedMessage)
//
//        }
//    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            if (shouldReturnError) {
                return Result.Error("Test Exception")
            }
            reminders.let {
                return@let Result.Success(ArrayList(it))
            }
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError)
            return Result.Error("Reminder not found!")
        val reminder = reminders?.first { it.id == id }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder $id not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun setErrorValue(value: Boolean) {
        shouldReturnError = value
    }
}