package com.yogeshpaliyal.comrade.utils

import java.text.DateFormat
import java.util.Date

object DateTimeUtils {

    /**
     * Formats a Long timestamp (milliseconds since epoch) into a localized date and time string.
     * Compatible with older Android versions.
     *
     * @param timestamp The timestamp in milliseconds since epoch. Can be null.
     * @return A formatted date/time string (e.g., "Jan 1, 2023, 10:00:00 AM") or an empty string if the timestamp is null or invalid.
     */
    fun formatDateTime(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0) { // Also check for invalid timestamp
            return ""
        }
        return try {
            val date = Date(timestamp)
            // Use default locale's medium date and time format
            val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
            dateFormat.format(date)
        } catch (e: Exception) {
            // Handle potential exceptions during formatting
            "" // Return empty string on error
        }
    }
}
