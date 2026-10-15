package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassRoster
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppTab(val label: String) {
    DECK("Deck"),
    CALENDAR("Daily Calendar"),
    MARK("Mark Absentees"),
    STATS("Student Stats")
}

data class StudentAttendanceStat(
    val rollNo: String,
    val group: String,
    val absentCount: Int,
    val totalDays: Int,
    val attendancePercentage: Float,
    val absentDates: List<String>
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AttendanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).attendanceDao()
        repository = AttendanceRepository(dao)
    }

    // Active Tab
    private val _currentTab = MutableStateFlow(AppTab.DECK)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        if (tab == AppTab.MARK) {
            // Automatically set to today's date
            initNewRecord(LocalDate.now())
        } else {
            _currentTab.value = tab
        }
    }

    // Sort order (Ascending vs Descending) - Default to Ascending matching AppSheet URL reference
    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

    fun toggleSortOrder() {
        _isAscending.value = !_isAscending.value
    }

    // Search query for records
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // All records with sorting
    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawRecords = _isAscending.flatMapLatest { asc ->
        repository.getAllRecords(asc)
    }

    // Filtered records by search query
    val records: StateFlow<List<AttendanceRecord>> = combine(rawRecords, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter { rec ->
                rec.date.lowercase().contains(q) ||
                rec.absenteeRolls.lowercase().contains(q) ||
                rec.remarks.lowercase().contains(q)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calendar state
    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    fun setSelectedCalendarDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    // Records for the selected calendar date
    val recordsForSelectedDate: StateFlow<List<AttendanceRecord>> = combine(rawRecords, _selectedCalendarDate) { list, date ->
        val dateStr = date.toString()
        list.filter { it.date == dateStr }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dates that have recorded attendance
    val recordedDates: StateFlow<List<String>> = repository.getAllRecordedDates().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Feedback message for user (e.g. lock limit alerts)
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    // MARK ABSENTEES STATE (AUTOMATIC TODAY'S DATE & MAX 3 EDITS)
    private val _editingRecordId = MutableStateFlow<Long?>(null)
    val editingRecordId: StateFlow<Long?> = _editingRecordId.asStateFlow()

    // Automatically initialized to today
    private val _markDate = MutableStateFlow(LocalDate.now())
    val markDate: StateFlow<LocalDate> = _markDate.asStateFlow()

    private val _absentRolls = MutableStateFlow<Set<String>>(emptySet())
    val absentRolls: StateFlow<Set<String>> = _absentRolls.asStateFlow()

    private val _markRemarks = MutableStateFlow("")
    val markRemarks: StateFlow<String> = _markRemarks.asStateFlow()

    private val _selectedRollCategory = MutableStateFlow(0)
    val selectedRollCategory: StateFlow<Int> = _selectedRollCategory.asStateFlow()

    // Current record for the mark date (tracks remaining edits and lock status)
    val currentMarkRecord: StateFlow<AttendanceRecord?> = combine(records, _markDate) { list, date ->
        list.firstOrNull { it.date == date.toString() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setMarkDate(date: LocalDate) {
        _markDate.value = date
        loadExistingForDate(date)
    }

    private fun loadExistingForDate(date: LocalDate) {
        val existing = records.value.firstOrNull { it.date == date.toString() }
        if (existing != null) {
            _editingRecordId.value = existing.id
            _absentRolls.value = existing.absenteeList.toSet()
            _markRemarks.value = existing.remarks
        } else {
            _editingRecordId.value = null
            _absentRolls.value = emptySet()
            _markRemarks.value = ""
        }
    }

    fun setMarkRemarks(remarks: String) {
        _markRemarks.value = remarks
    }

    fun setSelectedRollCategory(index: Int) {
        _selectedRollCategory.value = index
    }

    fun toggleAbsent(roll: String) {
        val existing = currentMarkRecord.value
        if (existing != null && existing.isLocked) {
            _feedbackMessage.value = "Attendance is locked. The maximum limit of 3 changes has been reached."
            return
        }

        val current = _absentRolls.value.toMutableSet()
        if (current.contains(roll)) {
            current.remove(roll)
        } else {
            current.add(roll)
        }
        _absentRolls.value = current
    }

    fun clearAllAbsentees() {
        val existing = currentMarkRecord.value
        if (existing != null && existing.isLocked) {
            _feedbackMessage.value = "Attendance is locked. The maximum limit of 3 changes has been reached."
            return
        }
        _absentRolls.value = emptySet()
    }

    // Automatically sets date to Today
    fun initNewRecord(preselectedDate: LocalDate? = null) {
        val targetDate = preselectedDate ?: LocalDate.now()
        _markDate.value = targetDate
        loadExistingForDate(targetDate)
        _selectedRollCategory.value = 0
        _currentTab.value = AppTab.MARK
    }

    fun initEditRecord(record: AttendanceRecord) {
        if (record.isLocked) {
            _feedbackMessage.value = "Cannot edit: This day's attendance is locked (3 changes used)."
            return
        }
        _editingRecordId.value = record.id
        try {
            _markDate.value = LocalDate.parse(record.date)
        } catch (e: Exception) {
            _markDate.value = LocalDate.now()
        }
        _absentRolls.value = record.absenteeList.toSet()
        _markRemarks.value = record.remarks
        _selectedRollCategory.value = 0
        _currentTab.value = AppTab.MARK
    }

    fun saveAttendance(onSaved: () -> Unit) {
        viewModelScope.launch {
            val dateStr = _markDate.value.toString()
            val rollsCsv = ClassRoster.formatAbsentees(_absentRolls.value)
            val total = ClassRoster.DEFAULT_ROLL_NUMBERS.size
            val absent = _absentRolls.value.size
            val present = total - absent
            val remarks = _markRemarks.value.trim()

            val existing = _editingRecordId.value?.let { id -> repository.getRecordById(id) }
                ?: repository.getRecordForDate(dateStr)

            if (existing != null) {
                // Check 3 edits limit
                if (existing.isLocked) {
                    _feedbackMessage.value = "Cannot save: Maximum limit of 3 changes reached for this day."
                    return@launch
                }

                val newEditCount = existing.editCount + 1
                val updated = AttendanceRecord(
                    id = existing.id,
                    date = dateStr,
                    sessionName = "Daily Attendance",
                    absenteeRolls = rollsCsv,
                    totalStudents = total,
                    absentCount = absent,
                    presentCount = present,
                    remarks = remarks,
                    editCount = newEditCount
                )
                repository.updateRecord(updated)
                val remaining = (AttendanceRecord.MAX_EDITS - newEditCount).coerceAtLeast(0)
                _feedbackMessage.value = if (remaining == 0) {
                    "Attendance updated. Final change used (Now locked)."
                } else {
                    "Attendance updated ($remaining changes remaining)."
                }
            } else {
                // Initial record created
                val newRecord = AttendanceRecord(
                    date = dateStr,
                    sessionName = "Daily Attendance",
                    absenteeRolls = rollsCsv,
                    totalStudents = total,
                    absentCount = absent,
                    presentCount = present,
                    remarks = remarks,
                    editCount = 0 // Initial creation: 3 changes remaining
                )
                repository.insertRecord(newRecord)
                _feedbackMessage.value = "Daily attendance recorded successfully (3 changes remaining)."
            }
            onSaved()
            _currentTab.value = AppTab.DECK
        }
    }

    fun deleteRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    // STUDENT STATS COMPUTATION (DAY-WISE)
    val studentStats: StateFlow<List<StudentAttendanceStat>> = rawRecords.combine(_searchQuery) { allRecs, _ ->
        val totalDays = allRecs.size
        ClassRoster.DEFAULT_ROLL_NUMBERS.map { roll ->
            val absentRecords = allRecs.filter { it.absenteeList.contains(roll) }
            val absentCount = absentRecords.size
            val percentage = if (totalDays > 0) {
                ((totalDays - absentCount).toFloat() / totalDays) * 100f
            } else {
                100f
            }
            StudentAttendanceStat(
                rollNo = roll,
                group = ClassRoster.getGroupForRoll(roll),
                absentCount = absentCount,
                totalDays = totalDays,
                attendancePercentage = percentage,
                absentDates = absentRecords.map { it.date }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Formatted report for sharing
    fun generateShareableReport(record: AttendanceRecord): String {
        val formattedDate = try {
            val d = LocalDate.parse(record.date)
            d.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
        } catch (e: Exception) {
            record.date
        }

        val absentees = if (record.absenteeRolls.isBlank()) {
            "None (All 72 Students Present 🎉)"
        } else {
            record.absenteeRolls
        }

        val remarksPart = if (record.remarks.isNotBlank()) {
            "\n📝 Remarks: ${record.remarks}"
        } else ""

        val editStatus = if (record.isLocked) {
            "\n🔒 Status: Locked (3/3 changes used)"
        } else {
            "\n✏️ Changes Remaining: ${record.remainingEdits}/3"
        }

        return """
📋 DAILY ABSENTEE REPORT
━━━━━━━━━━━━━━━━━━
📅 Date: $formattedDate
👥 Total Strength: ${record.totalStudents}
❌ Total Absentees: ${record.absentCount}
✅ Total Present: ${record.presentCount}
📊 Attendance: ${String.format("%.1f", record.attendancePercentage)}%$editStatus$remarksPart

🚫 Absent Roll Numbers:
$absentees
━━━━━━━━━━━━━━━━━━
Generated via Absentees App (Offline)
        """.trimIndent()
    }

    fun generateAllRecordsCsv(recordsList: List<AttendanceRecord>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Total,Absent,Present,Attendance_Percentage,Edits_Used,Absent_Rolls,Remarks\n")
        recordsList.forEach { rec ->
            val escapedRolls = "\"${rec.absenteeRolls.replace("\"", "\"\"")}\""
            val escapedRemarks = "\"${rec.remarks.replace("\"", "\"\"")}\""
            sb.append("${rec.id},${rec.date},${rec.totalStudents},${rec.absentCount},${rec.presentCount},${String.format("%.1f", rec.attendancePercentage)}%,${rec.editCount}/3,$escapedRolls,$escapedRemarks\n")
        }
        return sb.toString()
    }
}
