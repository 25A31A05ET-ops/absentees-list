package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassRoster
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.AbsentRedBorder
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.SmokeWhite
import com.example.ui.theme.SmokeWhiteBorder
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MarkAbsenteesScreen(
    date: LocalDate,
    currentRecord: AttendanceRecord?,
    absentRolls: Set<String>,
    onToggleAbsent: (String) -> Unit,
    onClearAll: () -> Unit,
    remarks: String,
    onRemarksChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    // Request focus so physical keyboard arrow keys, Page Down/Up immediately scroll the grid
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    // Roster groups in the exact order:
    // DV..DZ (5), EA..EZ (26 with EM), FA..FZ (26 with FJ), GA..GI (9), 19..24 (6) added down after GI
    val dRolls = remember { ClassRoster.DEFAULT_ROLL_NUMBERS.filter { it.startsWith("D") } }
    val eRolls = remember { ClassRoster.DEFAULT_ROLL_NUMBERS.filter { it.startsWith("E") } }
    val fRolls = remember { ClassRoster.DEFAULT_ROLL_NUMBERS.filter { it.startsWith("F") } }
    val gRolls = remember { ClassRoster.DEFAULT_ROLL_NUMBERS.filter { it.startsWith("G") } }
    val numRolls = remember { ClassRoster.DEFAULT_ROLL_NUMBERS.filter { it.all { ch -> ch.isDigit() } } }

    val allRolls = ClassRoster.DEFAULT_ROLL_NUMBERS
    val totalStudents = allRolls.size
    val absentCount = absentRolls.size
    val presentCount = totalStudents - absentCount

    val isLocked = currentRecord?.isLocked == true
    val remainingEdits = currentRecord?.remainingEdits ?: AttendanceRecord.MAX_EDITS
    val editsUsed = currentRecord?.editCount ?: 0

    var showRemarksInput by remember { mutableStateOf(remarks.isNotBlank()) }

    // Grid section indices
    val dHeaderIndex = if (isLocked) 2 else 1
    val eHeaderIndex = dHeaderIndex + 1 + dRolls.size
    val fHeaderIndex = eHeaderIndex + 1 + eRolls.size
    val gHeaderIndex = fHeaderIndex + 1 + fRolls.size
    val numHeaderIndex = gHeaderIndex + 1 + gRolls.size
    val notesIndex = numHeaderIndex + 1 + numRolls.size
    val totalGridItems = notesIndex + 2

    // Derived active section based on current firstVisibleItemIndex
    val activeSection by remember {
        derivedStateOf {
            val idx = gridState.firstVisibleItemIndex
            when {
                idx < eHeaderIndex -> "D-Series"
                idx < fHeaderIndex -> "E-Series"
                idx < gHeaderIndex -> "F-Series"
                idx < numHeaderIndex -> "G-Series"
                idx < notesIndex -> "Numbers (19-24)"
                else -> "Notes & Remarks"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SmokeWhite)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionDown, Key.PageDown, Key.Spacebar -> {
                            coroutineScope.launch {
                                val next = (gridState.firstVisibleItemIndex + 8).coerceAtMost(totalGridItems - 1)
                                gridState.animateScrollToItem(next)
                            }
                            true
                        }
                        Key.DirectionUp, Key.PageUp -> {
                            coroutineScope.launch {
                                val prev = (gridState.firstVisibleItemIndex - 8).coerceAtLeast(0)
                                gridState.animateScrollToItem(prev)
                            }
                            true
                        }
                        Key.MoveEnd -> {
                            coroutineScope.launch { gridState.animateScrollToItem(totalGridItems - 1) }
                            true
                        }
                        Key.Home -> {
                            coroutineScope.launch { gridState.animateScrollToItem(0) }
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // AppSheet Style Top Form Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Navy900,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("form_cancel_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Absentees",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                )
                                Text(
                                    text = if (editsUsed > 0) "Editing Day Attendance ($remainingEdits changes left)" else "New Day Attendance (Auto Date)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Top Save Button
                        Button(
                            onClick = onSave,
                            enabled = !isLocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("form_top_save_btn")
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Check,
                                contentDescription = "Save",
                                tint = if (isLocked) Color.Gray else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLocked) "Locked" else "Save",
                                color = if (isLocked) Color.Gray else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Series Jump & Quick Navigation Bar (Horizontal Scroll)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jump:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )

                        // Top / All chip
                        FilterChip(
                            selected = gridState.firstVisibleItemIndex == 0,
                            onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(0) }
                            },
                            label = { Text("Top / All (${totalStudents})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Navy800,
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )

                        // DV-DZ chip
                        FilterChip(
                            selected = activeSection == "D-Series",
                            onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(dHeaderIndex) }
                            },
                            label = { Text("DV-DZ (${dRolls.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Navy800,
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )

                        // EA-EZ chip (Middle roll numbers)
                        FilterChip(
                            selected = activeSection == "E-Series",
                            onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(eHeaderIndex) }
                            },
                            label = { Text("EA-EZ (${eRolls.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Navy800,
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )

                        // FA-FZ chip (Middle roll numbers)
                        FilterChip(
                            selected = activeSection == "F-Series",
                            onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(fHeaderIndex) }
                            },
                            label = { Text("FA-FZ (${fRolls.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Navy800,
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )

                        // GA-GI chip
                        FilterChip(
                            selected = activeSection == "G-Series",
                            onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(gHeaderIndex) }
                            },
                            label = { Text("GA-GI (${gRolls.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Navy800,
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )

                        // 19-24 chip (Added down to GI)
                        FilterChip(
                            selected = activeSection == "Numbers (19-24)",
                            onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(numHeaderIndex) }
                            },
                            label = { Text("19-24 (${numRolls.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Navy800,
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )

                        // Scroll to Bottom / Notes chip
                        FilterChip(
                            selected = activeSection == "Notes & Remarks",
                            onClick = {
                                coroutineScope.launch { gridState.animateScrollToItem(notesIndex) }
                            },
                            label = { Text("Bottom ⤓", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF334155),
                                labelColor = Color.White,
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }

            // Grid Container with Draggable Vertical Scrollbar and Manual Scroll Assist
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Continuous Manual Scrolling Grid with ALL Roll Numbers
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("mark_absentees_grid"),
                    contentPadding = PaddingValues(start = 12.dp, end = 26.dp, top = 8.dp, bottom = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Item 0: Auto-populated Date Card
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Date",
                                        tint = Navy800,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Today's Date (Auto-detected)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        Text(
                                            text = date.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Navy900,
                                                fontSize = 15.sp
                                            )
                                        )
                                    }
                                }

                                // 3 Edits Limit Indicator Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isLocked -> AbsentRedBg
                                        editsUsed > 0 -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFEFF6FF)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when {
                                            isLocked -> AbsentRedBorder
                                            editsUsed > 0 -> Color(0xFFFDE68A)
                                            else -> Color(0xFFBFDBFE)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = if (isLocked) "🔒 Locked (3/3 used)" else "$remainingEdits / 3 changes left",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLocked) AbsentRed else if (editsUsed > 0) Color(0xFF92400E) else Navy800,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Locked Alert Banner
                    if (isLocked) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AbsentRedBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AbsentRedBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = AbsentRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Attendance Locked: Maximum 3 changes used for this day. Further changes are restricted.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = AbsentRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Header D-Series
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RollSectionHeader(
                            title = "D-Series",
                            subtitle = "DV to DZ • 5 Students",
                            absentCount = dRolls.count { absentRolls.contains(it) },
                            totalCount = dRolls.size
                        )
                    }
                    items(dRolls, key = { it }) { roll ->
                        RollNumberItem(
                            roll = roll,
                            isAbsent = absentRolls.contains(roll),
                            isLocked = isLocked,
                            onToggle = { onToggleAbsent(roll) }
                        )
                    }

                    // Header E-Series (EA through EZ with EM)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RollSectionHeader(
                            title = "E-Series",
                            subtitle = "EA to EZ • 26 Students (including EM)",
                            absentCount = eRolls.count { absentRolls.contains(it) },
                            totalCount = eRolls.size
                        )
                    }
                    items(eRolls, key = { it }) { roll ->
                        RollNumberItem(
                            roll = roll,
                            isAbsent = absentRolls.contains(roll),
                            isLocked = isLocked,
                            onToggle = { onToggleAbsent(roll) }
                        )
                    }

                    // Header F-Series (FA through FZ with FJ)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RollSectionHeader(
                            title = "F-Series",
                            subtitle = "FA to FZ • 26 Students (including FJ)",
                            absentCount = fRolls.count { absentRolls.contains(it) },
                            totalCount = fRolls.size
                        )
                    }
                    items(fRolls, key = { it }) { roll ->
                        RollNumberItem(
                            roll = roll,
                            isAbsent = absentRolls.contains(roll),
                            isLocked = isLocked,
                            onToggle = { onToggleAbsent(roll) }
                        )
                    }

                    // Header G-Series (GA through GI)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RollSectionHeader(
                            title = "G-Series",
                            subtitle = "GA to GI • 9 Students",
                            absentCount = gRolls.count { absentRolls.contains(it) },
                            totalCount = gRolls.size
                        )
                    }
                    items(gRolls, key = { it }) { roll ->
                        RollNumberItem(
                            roll = roll,
                            isAbsent = absentRolls.contains(roll),
                            isLocked = isLocked,
                            onToggle = { onToggleAbsent(roll) }
                        )
                    }

                    // Header Numbers: 19 to 24 Added Down to GI
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RollSectionHeader(
                            title = "Numbers (19 to 24)",
                            subtitle = "19, 20, 21, 22, 23, 24 • Added down to GI",
                            absentCount = numRolls.count { absentRolls.contains(it) },
                            totalCount = numRolls.size
                        )
                    }
                    items(numRolls, key = { it }) { roll ->
                        RollNumberItem(
                            roll = roll,
                            isAbsent = absentRolls.contains(roll),
                            isLocked = isLocked,
                            onToggle = { onToggleAbsent(roll) },
                            badgeLabel = "Lateral"
                        )
                    }

                    // Optional Notes / Remarks
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Notes / Remarks (Optional)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Navy900
                                        )
                                    )
                                    IconButton(
                                        onClick = { showRemarksInput = !showRemarksInput },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = "Edit Notes",
                                            tint = Navy800
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = showRemarksInput) {
                                    OutlinedTextField(
                                        value = remarks,
                                        onValueChange = onRemarksChange,
                                        enabled = !isLocked,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .testTag("attendance_remarks_input"),
                                        placeholder = { Text("e.g. Leave letter submitted for DW, on duty for 19", fontSize = 12.sp) },
                                        minLines = 2,
                                        maxLines = 3,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Navy800,
                                            unfocusedBorderColor = SmokeWhiteBorder
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // End of list confirmation
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "— All ${allRolls.size} roll numbers loaded (DV down to 24) —",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Interactive Draggable Vertical Scrollbar (Right Edge)
                // Allows direct click-anywhere to jump, and drag up/down to scroll manually
                BoxWithConstraints(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(22.dp)
                        .padding(top = 8.dp, bottom = 140.dp, end = 2.dp)
                ) {
                    val trackHeightPx = constraints.maxHeight.toFloat()
                    val thumbHeightDp = 48.dp

                    val scrollFraction by remember {
                        derivedStateOf {
                            val maxIndex = (totalGridItems - 6).coerceAtLeast(1)
                            (gridState.firstVisibleItemIndex.toFloat() / maxIndex).coerceIn(0f, 1f)
                        }
                    }

                    // Background track
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(6.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .pointerInput(totalGridItems) {
                                detectTapGestures { offset ->
                                    val tapFraction = (offset.y / trackHeightPx).coerceIn(0f, 1f)
                                    val targetIndex = (tapFraction * totalGridItems).toInt().coerceIn(0, totalGridItems - 1)
                                    coroutineScope.launch {
                                        gridState.scrollToItem(targetIndex)
                                    }
                                }
                            }
                    )

                    // Draggable Scrollbar Thumb
                    val thumbOffset = ((trackHeightPx - 100f).coerceAtLeast(0f) * scrollFraction).toInt()
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(0, thumbOffset) }
                            .width(10.dp)
                            .height(thumbHeightDp)
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Navy800)
                            .pointerInput(totalGridItems) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    change.consume()
                                    val currentY = thumbOffset + dragAmount
                                    val dragFraction = (currentY / (trackHeightPx - 100f).coerceAtLeast(1f)).coerceIn(0f, 1f)
                                    val targetIndex = (dragFraction * totalGridItems).toInt().coerceIn(0, totalGridItems - 1)
                                    coroutineScope.launch {
                                        gridState.scrollToItem(targetIndex)
                                    }
                                }
                            }
                    )
                }

                // Floating Manual Scroll Control Pad (Bottom Right, above save bar)
                // Allows effortless 1-tap manual scrolling down/up without needing to drag
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 80.dp)
                        .shadow(6.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SmokeWhiteBorder)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        // Scroll Up button
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val prev = (gridState.firstVisibleItemIndex - 8).coerceAtLeast(0)
                                    gridState.animateScrollToItem(prev)
                                }
                            },
                            enabled = gridState.firstVisibleItemIndex > 0,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_scroll_up")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Scroll Up",
                                tint = if (gridState.firstVisibleItemIndex > 0) Navy800 else Color(0xFFCBD5E1)
                            )
                        }

                        // Active section pill / badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (activeSection) {
                                    "D-Series" -> "DV-DZ"
                                    "E-Series" -> "EA-EZ"
                                    "F-Series" -> "FA-FZ"
                                    "G-Series" -> "GA-GI"
                                    "Numbers (19-24)" -> "19-24"
                                    else -> "End"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        // Scroll Down button
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val next = (gridState.firstVisibleItemIndex + 8).coerceAtMost(totalGridItems - 1)
                                    gridState.animateScrollToItem(next)
                                }
                            },
                            enabled = gridState.firstVisibleItemIndex < totalGridItems - 6,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_scroll_down")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Scroll Down",
                                tint = if (gridState.firstVisibleItemIndex < totalGridItems - 6) Navy800 else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // Sticky Bottom Save & Summary Bar (AppSheet Form Style)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (absentCount > 0) "$absentCount Absentees" else "All $totalStudents Present",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (absentCount > 0) AbsentRed else PresentGreen,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = if (isLocked) "Locked (3/3 used)" else "$remainingEdits / 3 changes left",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isLocked) AbsentRed else Color(0xFF64748B),
                            fontWeight = if (isLocked) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isLocked && absentCount > 0) {
                        OutlinedButton(
                            onClick = onClearAll,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("form_clear_all_btn")
                        ) {
                            Text("Clear", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("form_bottom_cancel_btn")
                    ) {
                        Text("Cancel", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onSave,
                        enabled = !isLocked,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("save_attendance_record_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Navy800,
                            disabledContainerColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Save,
                            contentDescription = "Save Attendance",
                            tint = if (isLocked) Color.Gray else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLocked) "Locked" else "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) Color.Gray else Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RollSectionHeader(
    title: String,
    subtitle: String,
    absentCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE2E8F0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (absentCount > 0) AbsentRedBg else PresentGreenBg
            ) {
                Text(
                    text = if (absentCount > 0) "$absentCount absent" else "All $totalCount present",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (absentCount > 0) AbsentRed else PresentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RollNumberItem(
    roll: String,
    isAbsent: Boolean,
    isLocked: Boolean,
    onToggle: () -> Unit,
    badgeLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isAbsent) AbsentRed else Color.White
            )
            .border(
                width = if (isAbsent) 1.5.dp else 1.dp,
                color = when {
                    isAbsent -> Color(0xFFB91C1C)
                    else -> SmokeWhiteBorder
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !isLocked) { onToggle() }
            .testTag("roll_item_$roll"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = roll,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isAbsent) Color.White else Navy900,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (isAbsent) {
                Surface(
                    shape = CircleShape,
                    color = Color(0x33000000)
                ) {
                    Text(
                        text = "ABSENT",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp
                        )
                    )
                }
            } else {
                Text(
                    text = badgeLabel ?: "Roll",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 8.sp
                    )
                )
            }
        }
    }
}
