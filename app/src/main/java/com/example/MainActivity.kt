package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AttendanceTopBar
import com.example.ui.screens.DailyCalendarScreen
import com.example.ui.screens.DeckViewScreen
import com.example.ui.screens.MarkAbsenteesScreen
import com.example.ui.screens.StudentStatsScreen
import com.example.ui.theme.AbsenteesTheme
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SmokeWhite
import com.example.ui.theme.SmokeWhiteLight
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.AttendanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbsenteesTheme {
                AbsenteesApp()
            }
        }
    }
}

@Composable
fun AbsenteesApp(viewModel: AttendanceViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val records by viewModel.records.collectAsStateWithLifecycle()
    val isAscending by viewModel.isAscending.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val recordsForSelectedDate by viewModel.recordsForSelectedDate.collectAsStateWithLifecycle()
    val recordedDates by viewModel.recordedDates.collectAsStateWithLifecycle()

    val markDate by viewModel.markDate.collectAsStateWithLifecycle()
    val absentRolls by viewModel.absentRolls.collectAsStateWithLifecycle()
    val markRemarks by viewModel.markRemarks.collectAsStateWithLifecycle()
    val currentMarkRecord by viewModel.currentMarkRecord.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val studentStats by viewModel.studentStats.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearFeedbackMessage()
        }
    }

    // Handle back button to return to Deck view if on another tab
    BackHandler(enabled = currentTab != AppTab.DECK) {
        viewModel.setTab(AppTab.DECK)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentTab != AppTab.MARK) {
                when (currentTab) {
                    AppTab.DECK -> AttendanceTopBar(
                        title = "Absentees",
                        subtitle = "Attendance Deck • ${com.example.data.model.ClassRoster.DEFAULT_ROLL_NUMBERS.size} Students",
                        isAscending = isAscending,
                        onToggleSort = { viewModel.toggleSortOrder() }
                    )
                    AppTab.CALENDAR -> AttendanceTopBar(
                        title = "Daily Calendar",
                        subtitle = "Track daily attendance history"
                    )
                    AppTab.STATS -> AttendanceTopBar(
                        title = "Student Directory",
                        subtitle = "Roster attendance & shortage tracking"
                    )
                    AppTab.MARK -> Unit
                }
            }
        },
        bottomBar = {
            if (currentTab != AppTab.MARK) {
                NavigationBar(
                    containerColor = Navy900,
                    contentColor = Color.White,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_navigation_bar")
                ) {
                // Deck Item
                NavigationBarItem(
                    selected = currentTab == AppTab.DECK,
                    onClick = { viewModel.setTab(AppTab.DECK) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.DECK) Icons.Filled.ViewAgenda else Icons.Outlined.ViewAgenda,
                            contentDescription = "Deck View"
                        )
                    },
                    label = {
                        Text(
                            text = "Deck",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.DECK) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_deck")
                )

                // Calendar Item
                NavigationBarItem(
                    selected = currentTab == AppTab.CALENDAR,
                    onClick = { viewModel.setTab(AppTab.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Calendar View"
                        )
                    },
                    label = {
                        Text(
                            text = "Calendar",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.CALENDAR) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_calendar")
                )

                // Mark Absentees Item
                NavigationBarItem(
                    selected = currentTab == AppTab.MARK,
                    onClick = { viewModel.initNewRecord() },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.MARK) Icons.Filled.PersonAddAlt1 else Icons.Outlined.PersonAddAlt1,
                            contentDescription = "Mark Absentees"
                        )
                    },
                    label = {
                        Text(
                            text = "Mark",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.MARK) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_mark")
                )

                // Stats Item
                NavigationBarItem(
                    selected = currentTab == AppTab.STATS,
                    onClick = { viewModel.setTab(AppTab.STATS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.STATS) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "Stats View"
                        )
                    },
                    label = {
                        Text(
                            text = "Stats",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.STATS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Navy900,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_stats")
                )
            }
        }
    }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DECK -> DeckViewScreen(
                    records = records,
                    isAscending = isAscending,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onToggleSort = { viewModel.toggleSortOrder() },
                    onEditRecord = { rec -> viewModel.initEditRecord(rec) },
                    onDeleteRecord = { rec -> viewModel.deleteRecord(rec) },
                    onNewRecord = { viewModel.initNewRecord() },
                    generateShareReport = { rec -> viewModel.generateShareableReport(rec) }
                )

                AppTab.CALENDAR -> DailyCalendarScreen(
                    selectedDate = selectedCalendarDate,
                    onSelectDate = { date -> viewModel.setSelectedCalendarDate(date) },
                    recordsForSelectedDate = recordsForSelectedDate,
                    allRecords = records,
                    recordedDates = recordedDates,
                    onMarkForDate = { date -> viewModel.initNewRecord(preselectedDate = date) },
                    onEditRecord = { rec -> viewModel.initEditRecord(rec) },
                    onDeleteRecord = { rec -> viewModel.deleteRecord(rec) },
                    generateShareReport = { rec -> viewModel.generateShareableReport(rec) }
                )

                AppTab.MARK -> MarkAbsenteesScreen(
                    date = markDate,
                    currentRecord = currentMarkRecord,
                    absentRolls = absentRolls,
                    onToggleAbsent = { roll -> viewModel.toggleAbsent(roll) },
                    onClearAll = { viewModel.clearAllAbsentees() },
                    remarks = markRemarks,
                    onRemarksChange = { r -> viewModel.setMarkRemarks(r) },
                    onCancel = { viewModel.setTab(AppTab.DECK) },
                    onSave = {
                        viewModel.saveAttendance {
                            // saved successfully
                        }
                    }
                )

                AppTab.STATS -> StudentStatsScreen(
                    stats = studentStats,
                    allRecords = records,
                    generateCsv = { recs -> viewModel.generateAllRecordsCsv(recs) }
                )
            }
        }
    }
}
