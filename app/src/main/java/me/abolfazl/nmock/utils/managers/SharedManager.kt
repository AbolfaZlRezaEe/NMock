package me.abolfazl.nmock.utils.managers

import android.content.SharedPreferences
import me.abolfazl.nmock.utils.SharedParametersType

object SharedManager {

    fun putLong(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        value: Long
    ) {
        sharedPreferences
            .edit()
            .putLong(key, value)
            .apply()
    }

    fun getLong(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        defaultValue: Long
    ): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun deleteLong(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String
    ) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun putBoolean(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        value: Boolean
    ) {
        sharedPreferences
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun getBoolean(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        defaultValue: Boolean
    ): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

}