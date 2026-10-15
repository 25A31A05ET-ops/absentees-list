package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.ui.components.ReportShareDialog
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.SmokeWhite
import com.example.ui.theme.SmokeWhiteBorder
import com.example.ui.theme.SmokeWhiteLight
import com.example.ui.theme.SmokeWhiteMuted
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DailyCalendarScreen(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    recordsForSelectedDate: List<AttendanceRecord>,
    allRecords: List<AttendanceRecord>,
    recordedDates: List<String>,
    onMarkForDate: (LocalDate) -> Unit,
    onEditRecord: (AttendanceRecord) -> Unit,
    onDeleteRecord: (AttendanceRecord) -> Unit,
    generateShareReport: (AttendanceRecord) -> String,
    modifier: Modifier = Modifier
) {
    var displayMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var recordToShare by remember { mutableStateOf<AttendanceRecord?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SmokeWhite)
            .testTag("daily_calendar_screen")
    ) {
        // Calendar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${displayMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayMonth.year}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900,
                                    fontSize = 19.sp
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Jump to Today
                            OutlinedButton(
                                onClick = {
                                    val today = LocalDate.now()
                                    displayMonth = YearMonth.from(today)
                                    onSelectDate(today)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("today_quick_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = "Today",
                                    tint = Navy800,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Today", fontSize = 12.sp, color = Navy800, fontWeight = FontWeight.SemiBold)
                            }

                            IconButton(
                                onClick = { displayMonth = displayMonth.minusMonths(1) },
                                modifier = Modifier.testTag("prev_month_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Month",
                                    tint = Navy800
                                )
                            }

                            IconButton(
                                onClick = { displayMonth = displayMonth.plusMonths(1) },
                                modifier = Modifier.testTag("next_month_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Month",
                                    tint = Navy800
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days of Week Header
                    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { dayName ->
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid
                    val firstDayOfMonth = displayMonth.atDay(1)
                    val daysInMonth = displayMonth.lengthOfMonth()
                    // 1 = Monday, 7 = Sunday
                    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1
                    val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

                    val today = LocalDate.now()

                    for (week in 0 until (totalCells / 7)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (dayCol in 0..6) {
                                val cellIndex = week * 7 + dayCol
                                val dayNumber = cellIndex - dayOfWeekOffset + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val cellDate = displayMonth.atDay(dayNumber)
                                    val isSelected = cellDate == selectedDate
                                    val isToday = cellDate == today
                                    val dateStr = cellDate.toString()
                                    val hasRecord = recordedDates.contains(dateStr)

                                    // Check absentees count for this date
                                    val dateRecords = allRecords.filter { it.date == dateStr }
                                    val totalAbsentOnDate = dateRecords.sumOf { it.absentCount }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> Navy900
                                                    isToday -> SmokeWhiteMuted
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                color = if (isToday && !isSelected) Navy800 else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onSelectDate(cellDate) }
                                            .testTag("calendar_day_$dayNumber"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> Navy900
                                                        else -> Color(0xFF1E293B)
                                                    },
                                                    fontSize = 13.sp
                                                )
                                            )

                                            // Dot indicator if recorded
                                            if (hasRecord) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) {
                                                                if (totalAbsentOnDate > 0) Color(0xFFFCA5A5) else Color(0xFFA7F3D0)
                                                            } else {
                                                                if (totalAbsentOnDate > 0) AbsentRed else PresentGreen
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Details Section Header
        item {
            val formattedSelected = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = SmokeWhiteLight,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = formattedSelected,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = if (recordsForSelectedDate.isEmpty()) "No attendance recorded yet" else "Daily Attendance Logged",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        )
                    }

                    val existingRec = recordsForSelectedDate.firstOrNull()
                    val isDayLocked = existingRec?.isLocked == true

                    Button(
                        onClick = { onMarkForDate(selectedDate) },
                        enabled = !isDayLocked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Navy800,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("mark_for_date_button")
                    ) {
                        Icon(
                            imageVector = if (isDayLocked) Icons.Default.Add else Icons.Default.Add,
                            contentDescription = "Take Attendance",
                            tint = if (isDayLocked) Color.Gray else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isDayLocked -> "🔒 Locked"
                                existingRec != null -> "Edit (${existingRec.remainingEdits} left)"
                                else -> "Mark Day"
                            },
                            color = if (isDayLocked) Color.Gray else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Sessions on Selected Date
        if (recordsForSelectedDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = "Empty Day",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Absentees Logged for this Day",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Take attendance for Period 1, Morning or Daily session now.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(recordsForSelectedDate, key = { it.id }) { record ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    DeckRecordCard(
                        record = record,
                        onShare = { recordToShare = record },
                        onEdit = { onEditRecord(record) },
                        onDelete = { onDeleteRecord(record) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Share Dialog
    recordToShare?.let { record ->
        ReportShareDialog(
            reportText = generateShareReport(record),
            onDismiss = { recordToShare = null }
        )
    }
}
