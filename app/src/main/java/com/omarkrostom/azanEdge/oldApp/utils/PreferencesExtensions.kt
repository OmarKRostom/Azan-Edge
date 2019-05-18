package com.omarkrostom.azanEdge.oldApp.utils

import android.content.SharedPreferences

fun SharedPreferences.set(key: String, value: Any) {
    when (value) {
        is String -> edit { it.putString(key, value) }
        is Int -> edit { it.putInt(key, value) }
        is Float -> edit { it.putFloat(key, value) }
        is Boolean -> edit { it.putBoolean(key, value) }
        is Long -> edit { it.putLong(key, value) }
        else -> throw UnsupportedOperationException("This type is not supported")
    }
}

@Suppress("UNCHECKED_CAST")
fun SharedPreferences.get(key: String, default: Any = ""): Any = when (default) {
    is String -> getString(key, default)
    is Int -> getInt(key, default)
    is Float -> getFloat(key, default)
    is Boolean -> getBoolean(key, default)
    is Long -> getLong(key, default)
    else -> throw UnsupportedOperationException("This type is not supported")
}

fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
    val editor = this.edit()
    operation(editor)
    editor.apply()
}