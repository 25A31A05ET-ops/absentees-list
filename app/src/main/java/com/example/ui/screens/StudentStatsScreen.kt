package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.AbsentRedBorder
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.SmokeWhite
import com.example.ui.theme.SmokeWhiteBorder
import com.example.ui.theme.SmokeWhiteLight
import com.example.ui.theme.SmokeWhiteMuted
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.StudentAttendanceStat

@Composable
fun StudentStatsScreen(
    stats: List<StudentAttendanceStat>,
    allRecords: List<AttendanceRecord>,
    generateCsv: (List<AttendanceRecord>) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var sortByMostAbsent by remember { mutableStateOf(false) }
    var expandedRoll by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(stats, searchQuery, sortByMostAbsent) {
        val list = if (searchQuery.isBlank()) {
            stats
        } else {
            val q = searchQuery.trim().lowercase()
            stats.filter { it.rollNo.lowercase().contains(q) || it.group.lowercase().contains(q) }
        }

        if (sortByMostAbsent) {
            list.sortedByDescending { it.absentCount }
        } else {
            list
        }
    }

    val totalSessions = allRecords.size
    val totalAbsencesAcrossClass = stats.sumOf { it.absentCount }
    val averageAttendance = if (totalSessions > 0 && stats.isNotEmpty()) {
        val totalPossible = totalSessions * stats.size
        (((totalPossible - totalAbsencesAcrossClass).toFloat() / totalPossible) * 100f)
    } else {
        100f
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SmokeWhite)
            .testTag("student_stats_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Class Roster Overview",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Roster: ${stats.size} Students (DV..DZ, EA..EZ, FA..FZ, GA..GI, 19..24)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TOTAL SESSIONS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "$totalSessions Conducted",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "AVG ATTENDANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${String.format("%.1f", averageAttendance)}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (averageAttendance >= 75f) Color(0xFFA7F3D0) else Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Export CSV Button
                    Button(
                        onClick = {
                            val csvData = generateCsv(allRecords)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvData)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Attendance CSV"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2D4A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export CSV",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Attendance Data (CSV)", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }

        // Search and Sort Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stats_search_input"),
                        placeholder = { Text("Search roll number...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Navy800,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy800,
                            unfocusedBorderColor = SmokeWhiteBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${filteredList.size} students",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = sortByMostAbsent,
                                onClick = { sortByMostAbsent = !sortByMostAbsent },
                                label = {
                                    Text(
                                        text = if (sortByMostAbsent) "Most Absences First" else "Standard Roll Order",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Navy800,
                                    selectedLabelColor = Color.White,
                                    containerColor = SmokeWhiteMuted,
                                    labelColor = Navy900
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("sort_stats_button")
                            )
                        }
                    }
                }
            }
        }

        // Student List Items
        items(filteredList, key = { it.rollNo }) { student ->
            val isExpanded = expandedRoll == student.rollNo
            val hasShortage = totalSessions > 0 && student.attendancePercentage < 75f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_stat_${student.rollNo}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (hasShortage) 1.5.dp else 1.dp,
                    color = if (hasShortage) Color(0xFFFCA5A5) else SmokeWhiteBorder
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedRoll = if (isExpanded) null else student.rollNo
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (student.absentCount > 0) Color(0xFFFEF2F2) else PresentGreenBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (student.absentCount > 0) Color(0xFFFECACA) else Color(0xFFA7F3D0)
                                ),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = student.rollNo,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (student.absentCount > 0) AbsentRed else PresentGreen,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Roll ${student.rollNo}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "${student.group} • ${student.absentCount} absences",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (student.absentCount > 0) AbsentRed else Color(0xFF64748B),
                                        fontWeight = if (student.absentCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Attendance Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    student.attendancePercentage >= 85f -> PresentGreenBg
                                    student.attendancePercentage >= 75f -> Color(0xFFFEF3C7)
                                    else -> AbsentRedBg
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (hasShortage) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Attendance Shortage",
                                            tint = AbsentRed,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = "${String.format("%.0f", student.attendancePercentage)}%",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                student.attendancePercentage >= 85f -> PresentGreen
                                                student.attendancePercentage >= 75f -> WarningAmber
                                                else -> AbsentRed
                                            },
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    expandedRoll = if (isExpanded) null else student.rollNo
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = Navy800
                                )
                            }
                        }
                    }

                    // Expanded History of Absent Dates
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFF1F5F9))
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (student.absentDates.isEmpty()) {
                                Text(
                                    text = "🎉 Excellent! Never absent in any session.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = PresentGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            } else {
                                Text(
                                    text = "Recorded Absent on (${student.absentDates.size} sessions):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF475569),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                student.absentDates.forEach { d ->
                                    Text(
                                        text = "• $d",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF334155),
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
