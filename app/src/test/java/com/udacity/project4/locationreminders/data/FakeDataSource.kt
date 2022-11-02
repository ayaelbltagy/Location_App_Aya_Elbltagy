package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return try {
            if (shouldReturnError) {
                 throw Exception("Error happened during getting reminders")
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
        return try {
            if (shouldReturnError)
                throw Exception("Error happened during getting reminder")
           try {
               val reminder = reminders?.first { it.id == id }
               if (reminder != null) {
                   Result.Success(reminder)
               } else {
                   Result.Error("Reminder $id not found")
               }
           }catch (exception :NoSuchElementException){
               Result.Error(exception.localizedMessage)
           }
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun setErrorValue(value: Boolean) {
        shouldReturnError = value
    }
}