package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM absentee_records ORDER BY date DESC, createdAtTimestamp DESC")
    fun getAllRecordsDesc(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM absentee_records ORDER BY date ASC, createdAtTimestamp ASC")
    fun getAllRecordsAsc(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM absentee_records WHERE date = :date ORDER BY createdAtTimestamp DESC")
    fun getRecordsByDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT DISTINCT date FROM absentee_records")
    fun getAllRecordedDates(): Flow<List<String>>

    @Query("SELECT * FROM absentee_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): AttendanceRecord?

    @Query("SELECT * FROM absentee_records WHERE date = :date LIMIT 1")
    suspend fun getRecordForDate(date: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("DELETE FROM absentee_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COUNT(*) FROM absentee_records")
    suspend fun getRecordCount(): Int
}
