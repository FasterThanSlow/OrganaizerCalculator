package com.greenkey.nehaj.organaizercalculator2.utils

import android.content.SharedPreferences

fun SharedPreferences.Editor.putStringArrayList(key: String, value: ArrayList<String>): SharedPreferences.Editor {
    return putString(key, value.joinToString(","))
}

fun SharedPreferences.getStringArrayList(key: String, defaultValue: ArrayList<String>?): ArrayList<String>? {
    getString(key, null)?.run {
        if(isNotEmpty())
            return@getStringArrayList split(',') as ArrayList<String>
    }
    return defaultValue
}
