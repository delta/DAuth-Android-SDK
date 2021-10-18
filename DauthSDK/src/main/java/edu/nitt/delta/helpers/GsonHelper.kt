package edu.nitt.delta.helpers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal val gson = Gson()

/**
 * Convert an object of Type [T] to a map of name and value of type [R]
 */
internal fun <T, R> T.toMap(): Map<String, R> = convert()

/**
 * Convert an object of type [T] to type [R] using [Gson]
 */
private inline fun <T, reified R> T.convert(): R {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<R>() {}.type)
}
