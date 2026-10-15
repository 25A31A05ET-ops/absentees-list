package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ClassRoster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Absentees", appName)
    }

    @Test
    fun `roster contains all required roll numbers`() {
        val rolls = ClassRoster.DEFAULT_ROLL_NUMBERS
        assertEquals(72, rolls.size)

        // Verify specific roll numbers from the user prompt
        assertTrue(rolls.contains("DV"))
        assertTrue(rolls.contains("DW"))
        assertTrue(rolls.contains("DX"))
        assertTrue(rolls.contains("DY"))
        assertTrue(rolls.contains("DZ"))
        assertTrue(rolls.contains("EA"))
        assertTrue(rolls.contains("EZ"))
        assertTrue(rolls.contains("EM"))
        assertTrue(rolls.contains("FA"))
        assertTrue(rolls.contains("FJ"))
        assertTrue(rolls.contains("FZ"))
        assertTrue(rolls.contains("GA"))
        assertTrue(rolls.contains("GI"))
        assertTrue(rolls.contains("19"))
        assertTrue(rolls.contains("20"))
        assertTrue(rolls.contains("21"))
        assertTrue(rolls.contains("22"))
        assertTrue(rolls.contains("23"))
        assertTrue(rolls.contains("24"))

        // Verify 19 to 24 are added right down to the GI roll number
        val giIndex = rolls.indexOf("GI")
        val num19Index = rolls.indexOf("19")
        assertEquals(giIndex + 1, num19Index)

        // Verify corrections requested by user
        assertTrue(!rolls.contains("EME"))
        assertTrue(!rolls.contains("FJF"))
    }

    @Test
    fun `test three edits limit on attendance record`() {
        val initial = com.example.data.model.AttendanceRecord(
            date = "2026-10-15",
            absenteeRolls = "DV, DW",
            editCount = 0
        )
        assertEquals(3, initial.remainingEdits)
        assertTrue(!initial.isLocked)

        val editedTwice = initial.copy(editCount = 2)
        assertEquals(1, editedTwice.remainingEdits)
        assertTrue(!editedTwice.isLocked)

        val editedThreeTimes = initial.copy(editCount = 3)
        assertEquals(0, editedThreeTimes.remainingEdits)
        assertTrue(editedThreeTimes.isLocked)
    }
}
