package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassRoster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(entities = [AttendanceRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "absentees_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial sample attendance records so the user immediately sees the Deck view
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).attendanceDao()
                                val today = LocalDate.now().toString()
                                val yesterday = LocalDate.now().minusDays(1).toString()

                                dao.insertRecord(
                                    AttendanceRecord(
                                        date = today,
                                        sessionName = "Daily Attendance",
                                        absenteeRolls = "DV, DW, EA, 19, 24",
                                        totalStudents = ClassRoster.DEFAULT_ROLL_NUMBERS.size,
                                        absentCount = 5,
                                        presentCount = ClassRoster.DEFAULT_ROLL_NUMBERS.size - 5,
                                        remarks = "DV reported sick; EA sports duty",
                                        createdAtTimestamp = System.currentTimeMillis()
                                    )
                                )

                                dao.insertRecord(
                                    AttendanceRecord(
                                        date = yesterday,
                                        sessionName = "Daily Attendance",
                                        absenteeRolls = "EB, EK, FJ, 22",
                                        totalStudents = ClassRoster.DEFAULT_ROLL_NUMBERS.size,
                                        absentCount = 4,
                                        presentCount = ClassRoster.DEFAULT_ROLL_NUMBERS.size - 4,
                                        remarks = "Medical leave submitted for FJ",
                                        createdAtTimestamp = System.currentTimeMillis() - 86400000L
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
