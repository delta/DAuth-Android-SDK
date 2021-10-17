package edu.nitt.delta.helpers

import java.text.SimpleDateFormat
import java.util.*

internal fun Date.toFormatString(format: String) = SimpleDateFormat(format).format(this)

internal fun String.toDate(format: String): Date = SimpleDateFormat(format).parse(this)
