package edu.nitt.delta.helpers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal val gson = Gson()

// Convert an object of Type T to a map of name and value of type V
internal fun <T, V> T.toMap(): Map<String, V> = convert()

// Convert an object of type T to type R
internal inline fun <T, reified R> T.convert(): R {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<R>() {}.type)
}
