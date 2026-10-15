package com.example.data.model

object ClassRoster {
    // The exact roll numbers specified by user:
    // DV, DW, DX, DY, DZ (5)
    // EA through EZ (26) with EM
    // FA through FZ (26) with FJ
    // GA through GI (9)
    // 19, 20, 21, 22, 23, 24 (6) added down from GI - Total: 72 students
    val DEFAULT_ROLL_NUMBERS: List<String> = listOf(
        // D-Series
        "DV", "DW", "DX", "DY", "DZ",
        // E-Series
        "EA", "EB", "EC", "ED", "EE", "EF", "EG", "EH", "EI", "EJ", "EK", "EL", "EM",
        "EN", "EO", "EP", "EQ", "ER", "ES", "ET", "EU", "EV", "EW", "EX", "EY", "EZ",
        // F-Series
        "FA", "FB", "FC", "FD", "FE", "FF", "FG", "FH", "FI", "FJ", "FK", "FL", "FM",
        "FN", "FO", "FP", "FQ", "FR", "FS", "FT", "FU", "FV", "FW", "FX", "FY", "FZ",
        // G-Series
        "GA", "GB", "GC", "GD", "GE", "GF", "GG", "GH", "GI",
        // Number Series
        "19", "20", "21", "22", "23", "24"
    )

    fun getGroupForRoll(roll: String): String {
        return when {
            roll.startsWith("D") -> "D-Series"
            roll.startsWith("E") -> "E-Series"
            roll.startsWith("F") -> "F-Series"
            roll.startsWith("G") -> "G-Series"
            roll.all { it.isDigit() } -> "Numbers"
            else -> "Other"
        }
    }

    val CATEGORIES = listOf(
        "All (${DEFAULT_ROLL_NUMBERS.size})",
        "D-Series (${DEFAULT_ROLL_NUMBERS.count { it.startsWith("D") }})",
        "E-Series (${DEFAULT_ROLL_NUMBERS.count { it.startsWith("E") }})",
        "F-Series (${DEFAULT_ROLL_NUMBERS.count { it.startsWith("F") }})",
        "G-Series (${DEFAULT_ROLL_NUMBERS.count { it.startsWith("G") }})",
        "Numbers (${DEFAULT_ROLL_NUMBERS.count { it.all { ch -> ch.isDigit() } }})"
    )

    fun filterByCategory(categoryIndex: Int): List<String> {
        return when (categoryIndex) {
            1 -> DEFAULT_ROLL_NUMBERS.filter { it.startsWith("D") }
            2 -> DEFAULT_ROLL_NUMBERS.filter { it.startsWith("E") }
            3 -> DEFAULT_ROLL_NUMBERS.filter { it.startsWith("F") }
            4 -> DEFAULT_ROLL_NUMBERS.filter { it.startsWith("G") }
            5 -> DEFAULT_ROLL_NUMBERS.filter { it.all { ch -> ch.isDigit() } }
            else -> DEFAULT_ROLL_NUMBERS
        }
    }

    fun parseAbsentees(csv: String): List<String> {
        if (csv.isBlank()) return emptyList()
        return csv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun formatAbsentees(rolls: Collection<String>): String {
        // Keep them in the order of the roster
        val sorted = rolls.sortedWith(Comparator { a, b ->
            val idxA = DEFAULT_ROLL_NUMBERS.indexOf(a)
            val idxB = DEFAULT_ROLL_NUMBERS.indexOf(b)
            if (idxA != -1 && idxB != -1) idxA.compareTo(idxB)
            else a.compareTo(b)
        })
        return sorted.joinToString(", ")
    }
}
