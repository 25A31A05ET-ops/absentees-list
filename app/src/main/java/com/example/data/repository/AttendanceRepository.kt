package com.example.data.repository

import com.example.data.db.AttendanceDao
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val dao: AttendanceDao) {
    fun getAllRecords(ascending: Boolean): Flow<List<AttendanceRecord>> {
        return if (ascending) dao.getAllRecordsAsc() else dao.getAllRecordsDesc()
    }

    fun getRecordsByDate(date: String): Flow<List<AttendanceRecord>> {
        return dao.getRecordsByDate(date)
    }

    fun getAllRecordedDates(): Flow<List<String>> {
        return dao.getAllRecordedDates()
    }

    suspend fun getRecordById(id: Long): AttendanceRecord? {
        return dao.getRecordById(id)
    }

    suspend fun getRecordForDate(date: String): AttendanceRecord? {
        return dao.getRecordForDate(date)
    }

    suspend fun insertRecord(record: AttendanceRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun updateRecord(record: AttendanceRecord) {
        dao.updateRecord(record)
    }

    suspend fun deleteRecord(record: AttendanceRecord) {
        dao.deleteRecord(record)
    }

    suspend fun deleteRecordById(id: Long) {
        dao.deleteRecordById(id)
    }
}
