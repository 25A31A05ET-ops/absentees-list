package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.ui.components.ReportShareDialog
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.AbsentRedBorder
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.SmokeWhite
import com.example.ui.theme.SmokeWhiteBorder
import com.example.ui.theme.SmokeWhiteLight
import com.example.ui.theme.SmokeWhiteMuted
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeckViewScreen(
    records: List<AttendanceRecord>,
    isAscending: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleSort: () -> Unit,
    onEditRecord: (AttendanceRecord) -> Unit,
    onDeleteRecord: (AttendanceRecord) -> Unit,
    onNewRecord: () -> Unit,
    generateShareReport: (AttendanceRecord) -> String,
    modifier: Modifier = Modifier
) {
    var recordToShare by remember { mutableStateOf<AttendanceRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<AttendanceRecord?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SmokeWhite)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search and Controls Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SmokeWhiteLight,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deck_search_input"),
                        placeholder = { Text("Search by roll no, date, subject...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Navy800
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy800,
                            unfocusedBorderColor = SmokeWhiteBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${records.size} attendance records found",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleSort() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("sort_deck_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Sort",
                                tint = Navy800,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAscending) "Date: Oldest first" else "Date: Newest first",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Navy800,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            // Records List (Deck view)
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "No records",
                                tint = Navy800,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching records found" else "No Attendance Records Yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap '+ Note Absentees' below to take class attendance.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        DeckRecordCard(
                            record = record,
                            onShare = { recordToShare = record },
                            onEdit = { onEditRecord(record) },
                            onDelete = { recordToDelete = record }
                        )
                    }
                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to create new absentee record
        FloatingActionButton(
            onClick = onNewRecord,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_attendance_fab"),
            containerColor = Navy800,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Attendance"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Note Absentees",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // Share Dialog
    recordToShare?.let { record ->
        ReportShareDialog(
            reportText = generateShareReport(record),
            onDismiss = { recordToShare = null }
        )
    }

    // Delete Confirmation Dialog
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = {
                Text(
                    text = "Delete Attendance Record?",
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the record for ${record.date} (${record.sessionName})? This cannot be undone.",
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteRecord(record)
                        recordToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = AbsentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel", color = Navy800)
                }
            },
            containerColor = SmokeWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeckRecordCard(
    record: AttendanceRecord,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parsedDate = try {
        LocalDate.parse(record.date)
    } catch (e: Exception) {
        null
    }
    val monthStr = parsedDate?.format(DateTimeFormatter.ofPattern("MMM"))?.uppercase() ?: "DATE"
    val dayStr = parsedDate?.format(DateTimeFormatter.ofPattern("dd")) ?: "--"
    val dayOfWeekStr = parsedDate?.format(DateTimeFormatter.ofPattern("EEE"))?.uppercase() ?: ""
    val formattedDate = parsedDate?.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")) ?: record.date

    val absentees = record.absenteeList

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .testTag("record_card_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // AppSheet Deck View Row: Left Thumbnail Date Badge + Date Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Thumbnail (AppSheet deck style)
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Navy800)
                                .padding(vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monthStr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Navy900,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = dayOfWeekStr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                fontSize = 8.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                fontSize = 15.sp
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (record.isLocked) AbsentRedBg else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (record.isLocked) "🔒 Locked" else "✏️ ${record.remainingEdits}/3 left",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (record.isLocked) AbsentRed else Color(0xFF92400E),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Daily Attendance • Tap to view/edit",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attendance Stat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Absentees Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (record.absentCount > 0) AbsentRedBg else PresentGreenBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (record.absentCount > 0) AbsentRedBorder else Color(0xFFA7F3D0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (record.absentCount > 0) AbsentRed else PresentGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${record.absentCount} Absent",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (record.absentCount > 0) AbsentRed else PresentGreen
                            )
                        )
                    }
                }

                // Present Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SmokeWhiteMuted
                ) {
                    Text(
                        text = "${record.presentCount} Present / ${record.totalStudents}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Percentage Badge
                Surface(
                    shape = CircleShape,
                    color = Navy900
                ) {
                    Text(
                        text = "${String.format("%.0f", record.attendancePercentage)}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage Progress Bar
            LinearProgressIndicator(
                progress = { record.attendancePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (record.attendancePercentage >= 85f) PresentGreen else if (record.attendancePercentage >= 70f) Color(0xFFEAB308) else AbsentRed,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Absentee Roll Numbers Chips Section
            Text(
                text = "Absentees (${absentees.size}):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (absentees.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PresentGreenBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 Full attendance! No absentees in this session.",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PresentGreen,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    absentees.forEach { roll ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Text(
                                text = roll,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AbsentRed,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // Optional Remarks
            if (record.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SmokeWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: ${record.remarks}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF475569),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF1F5F9))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Share WhatsApp Button
                TextButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("card_share_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Navy800,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = Navy800, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // Edit Button
                if (record.isLocked) {
                    TextButton(
                        onClick = onEdit,
                        enabled = false,
                        modifier = Modifier.testTag("card_edit_${record.id}")
                    ) {
                        Text("🔒 Locked", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("card_edit_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Navy700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", color = Navy700, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("card_delete_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
