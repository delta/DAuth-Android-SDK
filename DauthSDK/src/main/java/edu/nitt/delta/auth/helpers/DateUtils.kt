package edu.nitt.delta.auth.helpers

import java.text.SimpleDateFormat
import java.util.*

/**
 * An extension function to format a [Date] into a date/time string
 * @param pattern the pattern describing the date and time format
 * @return The formatted time string
 */
internal fun Date.toFormatString(pattern: String) = SimpleDateFormat(pattern).format(this)

/**
 * An extension function to parse a [Date] from a given date/time string
 * @param pattern the pattern describing the date and time format
 * @return The Date parsed from the string
 */
internal fun String.toDate(pattern: String): Date = SimpleDateFormat(pattern).parse(this)
