package edu.nitt.delta.helpers

import java.text.SimpleDateFormat
import java.util.*

internal fun Date.getDateString(format: String) : String{
    val dateFormat = SimpleDateFormat(format)
    return dateFormat.format(this)
}

internal fun getDateFromString(dateString: String, format: String) : Date{
    val dateFormat = SimpleDateFormat(format)
    return dateFormat.parse(dateString)
}