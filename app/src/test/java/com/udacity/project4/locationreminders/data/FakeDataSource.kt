package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var list = mutableListOf<ReminderDTO>()
    var error = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        list?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error("Location not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        list?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return try {
            val reminder = list.find { it.id == id }
            if (reminder == null || error) {
                throw Exception("Exception happened")
            } else {
                Result.Success(reminder)
            }
        } catch (ex : Exception) {
            Result.Error(ex.toString())
        }
    }

    override suspend fun deleteAllReminders() {
        list.clear()
    }



}