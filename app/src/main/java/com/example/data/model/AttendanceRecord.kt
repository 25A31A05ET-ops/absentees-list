package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "absentee_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val sessionName: String = "Daily Attendance",
    val absenteeRolls: String, // Comma separated roll numbers
    val totalStudents: Int = ClassRoster.DEFAULT_ROLL_NUMBERS.size,
    val absentCount: Int = 0,
    val presentCount: Int = totalStudents - absentCount,
    val remarks: String = "",
    val editCount: Int = 0, // Number of modifications made to this day's attendance (Max 3)
    val createdAtTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val MAX_EDITS = 3
    }

    val absenteeList: List<String>
        get() = ClassRoster.parseAbsentees(absenteeRolls)

    val attendancePercentage: Float
        get() = if (totalStudents > 0) {
            ((totalStudents - absentCount).toFloat() / totalStudents) * 100f
        } else {
            100f
        }

    val remainingEdits: Int
        get() = (MAX_EDITS - editCount).coerceAtLeast(0)

    val isLocked: Boolean
        get() = editCount >= MAX_EDITS
}
